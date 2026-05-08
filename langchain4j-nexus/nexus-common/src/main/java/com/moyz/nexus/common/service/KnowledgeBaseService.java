package com.moyz.nexus.common.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.moyz.nexus.common.base.ThreadContext;
import com.moyz.nexus.common.cosntant.NexusConstant;
import com.moyz.nexus.common.cosntant.RedisKeyConstant;
import com.moyz.nexus.common.dto.KbEditReq;
import com.moyz.nexus.common.dto.KbInfoResp;
import com.moyz.nexus.common.dto.KbSearchReq;
import com.moyz.nexus.common.entity.*;
import com.moyz.nexus.common.exception.BaseException;
import com.moyz.nexus.common.file.FileOperatorContext;
import com.moyz.nexus.common.helper.LLMContext;
import com.moyz.nexus.common.helper.SSEEmitterHelper;
import com.moyz.nexus.common.mapper.KnowledgeBaseMapper;
import com.moyz.nexus.common.rag.*;
import com.moyz.nexus.common.service.embedding.IKnowledgeEmbeddingService;
import com.moyz.nexus.common.util.*;
import com.moyz.nexus.common.vo.*;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.moyz.nexus.common.cosntant.NexusConstant.RetrieveContentFrom.KNOWLEDGE_BASE;
import static com.moyz.nexus.common.cosntant.NexusConstant.SSE_TIMEOUT;
import static com.moyz.nexus.common.cosntant.NexusConstant.SysConfigKey.QUOTA_BY_QA_ASK_DAILY;
import static com.moyz.nexus.common.cosntant.RedisKeyConstant.KB_STATISTIC_RECALCULATE_SIGNAL;
import static com.moyz.nexus.common.cosntant.RedisKeyConstant.USER_INDEXING;
import static com.moyz.nexus.common.enums.ErrorEnum.*;
import static com.moyz.nexus.common.util.LocalDateTimeUtil.PATTERN_YYYY_MM_DD;

@Slf4j
@Service
public class KnowledgeBaseService extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> {

    @Lazy
    @Resource
    private KnowledgeBaseService self;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private KnowledgeBaseItemService knowledgeBaseItemService;

    @Resource
    private KnowledgeBaseQaService knowledgeBaseQaRecordService;

    @Resource
    private KnowledgeBaseStarService knowledgeBaseStarRecordService;

    @Resource
    private FileService fileService;

    @Resource
    private SSEEmitterHelper sseEmitterHelper;

    @Resource
    private UserDayCostService userDayCostService;

    @Resource
    private AiModelService aiModelService;

    @Resource
    private IKnowledgeEmbeddingService embeddingService;

    public KnowledgeBase saveOrUpdate(KbEditReq kbEditReq) {
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        BeanUtils.copyProperties(kbEditReq, knowledgeBase, "id", "uuid", "ingestTokenizer", "ingestEmbeddingModel");
        if (null != kbEditReq.getIngestModelId() && kbEditReq.getIngestModelId() > 0) {
            knowledgeBase.setIngestModelName(aiModelService.getByIdOrThrow(kbEditReq.getIngestModelId()).getName());
        } else {
            //没有指定抽取图谱知识时的LLM时，自动指定第一个可用的
            LLMContext.getFirstEnableAndFree().ifPresent(llmService -> {
                knowledgeBase.setIngestModelName(llmService.getAiModel().getName());
                knowledgeBase.setIngestModelId(llmService.getAiModel().getId());
            });
        }
        if (StringUtils.isNotBlank(kbEditReq.getIngestTokenEstimator()) && NexusConstant.TokenEstimator.ALL.contains(kbEditReq.getIngestTokenEstimator())) {
            knowledgeBase.setIngestTokenEstimator(kbEditReq.getIngestTokenEstimator());
        }
        if (null == kbEditReq.getId() || kbEditReq.getId() < 1) {
            User user = ThreadContext.getCurrentUser();
            knowledgeBase.setUuid(UuidUtil.createShort());
            knowledgeBase.setOwnerId(user.getId());
            knowledgeBase.setOwnerUuid(user.getUuid());
            knowledgeBase.setOwnerName(user.getName());
            baseMapper.insert(knowledgeBase);
        } else {
            checkPrivilege(kbEditReq.getId(), null);
            knowledgeBase.setId(kbEditReq.getId());
            baseMapper.updateById(knowledgeBase);
        }
        return knowledgeBase;
    }

    public List<NexusFile> uploadDocs(String kbUuid, Boolean embedding, MultipartFile[] docs, List<String> indexTypes) {
        if (ArrayUtils.isEmpty(docs)) {
            return Collections.emptyList();
        }
        checkPrivilege(null, kbUuid);
        List<NexusFile> result = new ArrayList<>();
        KnowledgeBase knowledgeBase = ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(KnowledgeBase::getUuid, kbUuid)
                .eq(KnowledgeBase::getIsDeleted, false)
                .oneOpt()
                .orElseThrow(() -> new BaseException(A_DATA_NOT_FOUND));
        for (MultipartFile doc : docs) {
            try {
                result.add(uploadDoc(knowledgeBase, doc, embedding, indexTypes));
            } catch (Exception e) {
                log.warn("uploadDocs fail,fileName:{}", doc.getOriginalFilename(), e);
            }
        }
        return result;
    }

    public NexusFile uploadDoc(String kbUuid, Boolean indexAfterUpload, MultipartFile doc, List<String> indexTypes) {
        KnowledgeBase knowledgeBase = ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(KnowledgeBase::getUuid, kbUuid)
                .eq(KnowledgeBase::getIsDeleted, false)
                .oneOpt()
                .orElseThrow(() -> new BaseException(A_DATA_NOT_FOUND));
        return uploadDoc(knowledgeBase, doc, indexAfterUpload, indexTypes);
    }

    private NexusFile uploadDoc(KnowledgeBase knowledgeBase, MultipartFile doc, Boolean indexAfterUpload, List<String> indexTypes) {
        try {
            String fileName = doc.getOriginalFilename();
            NexusFile NexusFile = fileService.saveFile(doc, false);

            //解析文档
            Document document = FileOperatorContext.loadDocument(NexusFile);
            if (null == document) {
                log.warn("该文件类�?{}无法解析，忽�?, NexusFile.getExt());
                return NexusFile;
            }
            //创建知识库条�?
            String uuid = UuidUtil.createShort();
            //postgresql不支持\u0000
            String content = document.text().replace("\u0000", "");
            KnowledgeBaseItem knowledgeBaseItem = new KnowledgeBaseItem();
            knowledgeBaseItem.setUuid(uuid);
            knowledgeBaseItem.setKbId(knowledgeBase.getId());
            knowledgeBaseItem.setKbUuid(knowledgeBase.getUuid());
            knowledgeBaseItem.setSourceFileId(NexusFile.getId());
            knowledgeBaseItem.setTitle(fileName);
            knowledgeBaseItem.setBrief(StringUtils.substring(content, 0, 200));
            knowledgeBaseItem.setRemark(content);
            boolean success = knowledgeBaseItemService.save(knowledgeBaseItem);
            if (success && Boolean.TRUE.equals(indexAfterUpload)) {
                indexItems(List.of(uuid), indexTypes);
            }

            //Replace file path with url
            NexusFile.setPath(FileOperatorContext.getFileUrl(NexusFile));
            return NexusFile;
        } catch (Exception e) {
            log.error("upload error", e);
            throw new BaseException(A_UPLOAD_FAIL);
        }
    }

    /**
     * 索引（向量化、图谱化�?
     *
     * @param kbUuid     知识库uuid
     * @param indexTypes 索引类型，如embedding,graphical
     * @return 成功或失�?
     */
    public boolean indexing(String kbUuid, List<String> indexTypes) {
        checkPrivilege(null, kbUuid);
        KnowledgeBase knowledgeBase = this.getOrThrow(kbUuid);
        LambdaQueryWrapper<KnowledgeBaseItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBaseItem::getIsDeleted, false);
        wrapper.eq(KnowledgeBaseItem::getUuid, kbUuid);
        BizPager.oneByOneWithAnchor(wrapper, knowledgeBaseItemService, KnowledgeBaseItem::getId, kbItem -> knowledgeBaseItemService.asyncIndex(ThreadContext.getCurrentUser(), knowledgeBase, kbItem, indexTypes));
        return true;
    }

    /**
     * 索引知识点（同一知识库下�?
     *
     * @param itemUuids  知识点uuid列表
     * @param indexTypes 索引类型，如embedding,graphical
     * @return 成功或失�?
     */
    public boolean indexItems(List<String> itemUuids, List<String> indexTypes) {
        if (CollectionUtils.isEmpty(itemUuids)) {
            return false;
        }
        KnowledgeBase knowledgeBase = baseMapper.getByItemUuid(itemUuids.get(0));
        String userIndexKey = MessageFormat.format(USER_INDEXING, knowledgeBase.getOwnerId());
        Boolean exist = stringRedisTemplate.hasKey(userIndexKey);
        if (Boolean.TRUE.equals(exist)) {
            log.warn("文档正在索引�?请忽频繁操作,userId:{}", knowledgeBase.getOwnerId());
            throw new BaseException(A_DOC_INDEX_DOING);
        }
        try {
            return knowledgeBaseItemService.checkAndIndexing(knowledgeBase, itemUuids, indexTypes);
        } catch (Exception e) {
            log.error("indexItems error", e);
            throw e;
        }
    }

    /**
     * 检查当前用户下的索引任务是否已经结�?
     *
     * @return 成功或失�?
     */
    public boolean checkIndexIsFinish() {
        String userIndexKey = MessageFormat.format(USER_INDEXING, ThreadContext.getCurrentUserId());
        return Boolean.FALSE.equals(stringRedisTemplate.hasKey(userIndexKey));
    }

    public Page<KbInfoResp> searchMine(String keyword, Boolean includeOthersPublic, Integer currentPage, Integer pageSize) {
        Page<KbInfoResp> result = new Page<>();
        User user = ThreadContext.getCurrentUser();
        Page<KnowledgeBase> knowledgeBasePage;
        if (Boolean.TRUE.equals(user.getIsAdmin())) {
            knowledgeBasePage = baseMapper.searchByAdmin(new Page<>(currentPage, pageSize), keyword);
        } else {
            knowledgeBasePage = baseMapper.searchByUser(new Page<>(currentPage, pageSize), user.getId(), keyword, includeOthersPublic);
        }
        return MPPageUtil.convertToPage(knowledgeBasePage, result, KbInfoResp.class, null);
    }

    public Page<KbInfoResp> search(KbSearchReq req, Integer currentPage, Integer pageSize) {
        Page<KbInfoResp> result = new Page<>();
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(req.getTitle())) {
            wrapper.like(KnowledgeBase::getTitle, req.getTitle());
        }
        if (StringUtils.isNotBlank(req.getOwnerName())) {
            wrapper.like(KnowledgeBase::getOwnerName, req.getOwnerName());
        }
        if (null != req.getIsPublic()) {
            wrapper.eq(KnowledgeBase::getIsPublic, req.getIsPublic());
        }
        if (null != req.getMinItemCount()) {
            wrapper.ge(KnowledgeBase::getItemCount, req.getMinItemCount());
        }
        if (null != req.getMinEmbeddingCount()) {
            wrapper.ge(KnowledgeBase::getEmbeddingCount, req.getMinEmbeddingCount());
        }
        if (null != req.getCreateTime() && req.getCreateTime().length == 2) {
            wrapper.between(KnowledgeBase::getCreateTime, LocalDateTimeUtil.parse(req.getCreateTime()[0]), LocalDateTimeUtil.parse(req.getCreateTime()[1]));
        }
        if (null != req.getUpdateTime() && req.getUpdateTime().length == 2) {
            wrapper.between(KnowledgeBase::getUpdateTime, LocalDateTimeUtil.parse(req.getUpdateTime()[0]), LocalDateTimeUtil.parse(req.getUpdateTime()[1]));
        }
        wrapper.eq(KnowledgeBase::getIsDeleted, false);
        wrapper.orderByDesc(KnowledgeBase::getStarCount, KnowledgeBase::getUpdateTime);
        Page<KnowledgeBase> knowledgeBasePage = baseMapper.selectPage(new Page<>(currentPage, pageSize), wrapper);
        return MPPageUtil.convertToPage(knowledgeBasePage, result, KbInfoResp.class, null);
    }

    public List<KbInfoResp> listByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<KnowledgeBase> knowledgeBases = baseMapper.selectByIds(ids);
        return MPPageUtil.convertToList(knowledgeBases, KbInfoResp.class);
    }

    public boolean softDelete(String uuid) {
        checkPrivilege(null, uuid);
        return ChainWrappers.lambdaUpdateChain(baseMapper)
                .eq(KnowledgeBase::getUuid, uuid)
                .set(KnowledgeBase::getIsDeleted, true)
                .update();
    }

    public SseEmitter sseAsk(String qaRecordUuid) {
        checkRequestTimesOrThrow();
        SseEmitter sseEmitter = new SseEmitter(SSE_TIMEOUT);
        User user = ThreadContext.getCurrentUser();
        if (!sseEmitterHelper.checkOrComplete(user, sseEmitter)) {
            return sseEmitter;
        }
        sseEmitterHelper.startSse(user, sseEmitter);
        self.retrieveAndPushToLLM(user, sseEmitter, qaRecordUuid);
        return sseEmitter;
    }

    /**
     * Star or unstar
     *
     * @param user   用户
     * @param kbUuid 知识库uuid
     * @return true:star;false:unstar
     */
    @Transactional
    public boolean toggleStar(User user, String kbUuid) {

        KnowledgeBase knowledgeBase = self.getOrThrow(kbUuid);
        boolean star;
        KnowledgeBaseStar oldRecord = knowledgeBaseStarRecordService.getRecord(user.getId(), kbUuid);
        if (null == oldRecord) {
            KnowledgeBaseStar starRecord = new KnowledgeBaseStar();
            starRecord.setUserId(user.getId());
            starRecord.setUserUuid(user.getUuid());
            starRecord.setKbId(knowledgeBase.getId());
            starRecord.setKbUuid(kbUuid);
            knowledgeBaseStarRecordService.save(starRecord);

            star = true;
        } else {
            //Deleted means unstar
            knowledgeBaseStarRecordService.lambdaUpdate()
                    .eq(KnowledgeBaseStar::getId, oldRecord.getId())
                    .set(KnowledgeBaseStar::getIsDeleted, !oldRecord.getIsDeleted())
                    .update();
            star = oldRecord.getIsDeleted();
        }
        int starCount = star ? knowledgeBase.getStarCount() + 1 : knowledgeBase.getStarCount() - 1;
        ChainWrappers.lambdaUpdateChain(baseMapper)
                .eq(KnowledgeBase::getId, knowledgeBase.getId())
                .set(KnowledgeBase::getStarCount, starCount)
                .update();
        return star;
    }

    /**
     * 知识库问答限额判�?
     */
    private void checkRequestTimesOrThrow() {
        String key = MessageFormat.format(RedisKeyConstant.AQ_ASK_TIMES, ThreadContext.getCurrentUserId(), LocalDateTimeUtil.format(LocalDateTime.now(), PATTERN_YYYY_MM_DD));
        String askTimes = stringRedisTemplate.opsForValue().get(key);
        String askQuota = SysConfigService.getByKey(QUOTA_BY_QA_ASK_DAILY);
        if (null != askQuota && null != askTimes && Integer.parseInt(askTimes) >= Integer.parseInt(askQuota)) {
            throw new BaseException(A_QA_ASK_LIMIT);
        }
        stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, Duration.ofDays(1));
    }

    /**
     * 文档召回并将请求发送给LLM
     *
     * @param user         当前提问的用�?
     * @param sseEmitter   sse emitter
     * @param qaRecordUuid 知识库uuid
     */
    @Async
    public void retrieveAndPushToLLM(User user, SseEmitter sseEmitter, String qaRecordUuid) {
        log.info("retrieveAndPushToLLM,qaRecordUuid:{},userId:{}", qaRecordUuid, user.getId());
        KnowledgeBaseQa qaRecord = knowledgeBaseQaRecordService.getOrThrow(qaRecordUuid);
        KnowledgeBase knowledgeBase = getOrThrow(qaRecord.getKbUuid());
        AiModel aiModel = aiModelService.getByIdOrThrow(qaRecord.getAiModelId());

        TokenEstimatorThreadLocal.setTokenEstimator(knowledgeBase.getIngestTokenEstimator());

        try {
            int maxInputTokens = aiModel.getMaxInputTokens();
            int maxResults = knowledgeBase.getRetrieveMaxResults();
            //maxResults < 1 表示由系统根据设置的模型maxInputTokens自动计算大小
            if (maxResults < 1) {
                maxResults = EmbeddingRag.getRetrieveMaxResults(qaRecord.getQuestion(), maxInputTokens);
            }

            SseAskParams sseAskParams = new SseAskParams();
            sseAskParams.setUuid(qaRecord.getUuid());
            sseAskParams.setHttpRequestParams(
                    ChatModelRequestParams.builder()
                            .memoryId(qaRecord.getKbUuid() + "_" + user.getUuid())
                            .systemMessage(knowledgeBase.getQuerySystemMessage())
                            .userMessage(qaRecord.getQuestion())
                            .build()
            );
            sseAskParams.setModelProperties(
                    ChatModelBuilderProperties.builder()
                            .temperature(knowledgeBase.getQueryLlmTemperature())
                            .build()
            );
            sseAskParams.setSseEmitter(sseEmitter);
            sseAskParams.setModelName(aiModel.getName());
            sseAskParams.setUser(user);
            if (maxResults == 0) {
                log.info("用户问题过长，无需再召回文档，严格模式下直接返回异常提�?宽松模式下接着请求LLM");
                if (Boolean.TRUE.equals(knowledgeBase.getIsStrict())) {
                    sseEmitterHelper.sendErrorAndComplete(user.getId(), sseEmitter, "提问内容过长，最多不超过 " + maxInputTokens + " tokens");
                    TokenEstimatorThreadLocal.clearTokenEstimator();
                } else {
                    sseEmitterHelper.call(sseAskParams, (response, questionMeta, answerMeta) -> {
                                sseEmitterHelper.sendComplete(user.getId(), sseEmitter);
                                updateQaRecord(
                                        UpdateQaParams.builder()
                                                .user(user)
                                                .qaRecord(qaRecord)
                                                .retrievers(null)
                                                .sseAskParams(sseAskParams)
                                                .response(response.getContent())
                                                .isTokenFree(aiModel.getIsFree())
                                                .build());
                                TokenEstimatorThreadLocal.clearTokenEstimator();
                            }
                    );
                }
            } else {
                log.info("进行RAG请求,maxResults:{}", maxResults);
                ChatModel chatModel = LLMContext.getServiceById(knowledgeBase.getIngestModelId(), true).buildChatLLM(
                        ChatModelBuilderProperties.builder()
                                .temperature(knowledgeBase.getQueryLlmTemperature())
                                .build());
                RetrieverCreateParam createParam = RetrieverCreateParam.builder()
                        .chatModel(chatModel)
                        .filter(new IsEqualTo(NexusConstant.MetadataKey.KB_UUID, qaRecord.getKbUuid()))
                        .maxResults(maxResults)
                        .minScore(knowledgeBase.getRetrieveMinScore())
                        .breakIfSearchMissed(knowledgeBase.getIsStrict())
                        .build();
                CompositeRag compositeRag = new CompositeRag(KNOWLEDGE_BASE);
                List<RetrieverWrapper> retrieverWrappers = compositeRag.createRetriever(createParam);
                List<ContentRetriever> retrievers = retrieverWrappers.stream().map(RetrieverWrapper::getRetriever).toList();
                compositeRag.ragChat(retrievers, sseAskParams, (response, promptMeta, answerMeta) -> {
                            sseEmitterHelper.sendComplete(user.getId(), sseAskParams.getSseEmitter());
                            updateQaRecord(
                                    UpdateQaParams.builder()
                                            .user(user)
                                            .qaRecord(qaRecord)
                                            .retrievers(retrievers)
                                            .sseAskParams(sseAskParams)
                                            .response(response)
                                            .isTokenFree(aiModel.getIsFree())
                                            .build());
                            TokenEstimatorThreadLocal.clearTokenEstimator();
                        }
                );
            }
        } catch (Exception e) {
            TokenEstimatorThreadLocal.clearTokenEstimator();
            throw e;
        }
    }

    private void updateQaRecord(UpdateQaParams updateQaParams) {

        Pair<Integer, Integer> inputOutputTokenCost = LLMTokenUtil.calAllTokenCostByUuid(stringRedisTemplate, updateQaParams.getSseAskParams().getUuid());

        KnowledgeBaseQa qaRecord = updateQaParams.getQaRecord();
        User user = updateQaParams.getUser();

        KnowledgeBaseQa updateRecord = new KnowledgeBaseQa();
        updateRecord.setId(qaRecord.getId());
        updateRecord.setPrompt(updateQaParams.getSseAskParams().getHttpRequestParams().getUserMessage());
        updateRecord.setPromptTokens(inputOutputTokenCost.getLeft());
        updateRecord.setAnswer(updateQaParams.getResponse());
        updateRecord.setAnswerTokens(inputOutputTokenCost.getRight());
        knowledgeBaseQaRecordService.updateById(updateRecord);

        createRef(updateQaParams.getRetrievers(), user, qaRecord.getId());
        //用户本次请求消耗的token数指的是整个RAG过程中消耗的token数量，其中可能涉及到多次LLM请求
        int allToken = inputOutputTokenCost.getLeft() + inputOutputTokenCost.getRight();
        log.info("用户{}本次请示消耗总token:{}", user.getName(), allToken);
        if (allToken > 0) {
            userDayCostService.appendCostToUser(user, allToken, updateQaParams.isTokenFree());
        }
    }

    /**
     * 创建引用记录
     *
     * @param retrievers 召回�?
     * @param user       用户
     * @param qaId       问答id
     */
    private void createRef(List<ContentRetriever> retrievers, User user, Long qaId) {
        if (CollectionUtils.isEmpty(retrievers)) {
            return;
        }
        for (ContentRetriever retriever : retrievers) {
            if (retriever instanceof NexusEmbeddingStoreContentRetriever embeddingRetriever) {
                knowledgeBaseQaRecordService.createEmbeddingRefs(user, qaId, embeddingRetriever.getRetrievedEmbeddingToScore());
            } else if (retriever instanceof GraphStoreContentRetriever graphRetriever) {
                knowledgeBaseQaRecordService.createGraphRefs(user, qaId, graphRetriever.getGraphRef());
            }
        }
    }

    public KnowledgeBase getOrThrow(String kbUuid) {
        return ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(KnowledgeBase::getUuid, kbUuid)
                .eq(KnowledgeBase::getIsDeleted, false)
                .oneOpt().orElseThrow(() -> new BaseException(A_DATA_NOT_FOUND));
    }

    /**
     * Set update knowledge base stat signal
     *
     * @param kbUuid 知识库uuid
     */
    public void updateStatistic(String kbUuid) {
        stringRedisTemplate.opsForSet().add(KB_STATISTIC_RECALCULATE_SIGNAL, kbUuid);
    }

    public int countTodayCreated() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime beginTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0);
        LocalDateTime endTime = beginTime.plusDays(1);
        return baseMapper.countCreatedByTimePeriod(beginTime, endTime);
    }

    public int countAllCreated() {
        return baseMapper.countAllCreated();
    }

    /**
     * Update knowledge base stat
     */
    @Scheduled(fixedDelay = 60 * 1000)
    public void asyncUpdateStatistic() {
        Set<String> kbUuidList = stringRedisTemplate.opsForSet().members(KB_STATISTIC_RECALCULATE_SIGNAL);
        if (CollectionUtils.isEmpty(kbUuidList)) {
            return;
        }
        for (String kbUuid : kbUuidList) {
            int embeddingCount = embeddingService.countByKbUuid(kbUuid);
            baseMapper.updateStatByUuid(kbUuid, embeddingCount);
            stringRedisTemplate.opsForSet().remove(KB_STATISTIC_RECALCULATE_SIGNAL, kbUuid);
        }
    }

    private void checkPrivilege(Long kbId, String kbUuid) {
        if (null == kbId && StringUtils.isBlank(kbUuid)) {
            throw new BaseException(A_PARAMS_ERROR);
        }
        User user = ThreadContext.getCurrentUser();
        if (null == user) {
            throw new BaseException(A_USER_NOT_EXIST);
        }
        boolean privilege = user.getIsAdmin();
        if (privilege) {
            return;
        }
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getOwnerId, user.getId());
        if (null != kbId) {
            wrapper = wrapper.eq(KnowledgeBase::getId, kbId);
        } else if (StringUtils.isNotBlank(kbUuid)) {
            wrapper = wrapper.eq(KnowledgeBase::getUuid, kbUuid);
        }
        boolean exists = baseMapper.exists(wrapper);
        if (!exists) {
            throw new BaseException(A_USER_NOT_AUTH);
        }
    }

}
