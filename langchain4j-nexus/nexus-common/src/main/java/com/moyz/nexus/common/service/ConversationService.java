package com.moyz.nexus.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.moyz.nexus.common.base.ThreadContext;
import com.moyz.nexus.common.cosntant.NexusConstant;
import com.moyz.nexus.common.dto.*;
import com.moyz.nexus.common.entity.*;
import com.moyz.nexus.common.exception.BaseException;
import com.moyz.nexus.common.mapper.ConversationMapper;
import com.moyz.nexus.common.util.JsonUtil;
import com.moyz.nexus.common.util.LocalCache;
import com.moyz.nexus.common.util.MPPageUtil;
import com.moyz.nexus.common.util.UuidUtil;
import com.moyz.nexus.common.vo.AudioConfig;
import com.moyz.nexus.common.vo.TtsSetting;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.moyz.nexus.common.enums.ErrorEnum.*;
import static com.moyz.nexus.common.util.LocalCache.MODEL_ID_TO_OBJ;

@Slf4j
@Service
public class ConversationService extends ServiceImpl<ConversationMapper, Conversation> {

    @Lazy
    @Resource
    private ConversationService self;

    @Resource
    private SysConfigService sysConfigService;

    @Resource
    private ConversationMessageService conversationMessageService;

    @Resource
    private ConversationPresetService conversationPresetService;

    @Resource
    private ConversationPresetRelService conversationPresetRelService;

    @Resource
    private UserMcpService userMcpService;

    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    @Resource
    private FileService fileService;

    @Resource
    private AiModelService aiModelService;

    public Page<ConvDto> search(ConvSearchReq convSearchReq, int currentPage, int pageSize) {
        Page<Conversation> page = this.lambdaQuery()
                .eq(Conversation::getIsDeleted, false)
                .like(!StringUtils.isBlank(convSearchReq.getTitle()), Conversation::getTitle, convSearchReq.getTitle())
                .orderByDesc(Conversation::getId)
                .page(new Page<>(currentPage, pageSize));
        return MPPageUtil.convertToPage(page, ConvDto.class);
    }

    public List<ConvDto> listByUser() {
        User user = ThreadContext.getCurrentUser();
        List<Conversation> list = this.lambdaQuery()
                .eq(Conversation::getUserId, user.getId())
                .eq(Conversation::getIsDeleted, false)
                .orderByDesc(Conversation::getId)
                .last("limit " + sysConfigService.getConversationMaxNum())
                .list();
        return MPPageUtil.convertToList(list, ConvDto.class, (source, target) -> {
            setMcpToDto(source, target);
            setKbInfoToDto(source, target);
            return target;
        });
    }

    /**
     * 查询对话{@code uuid}的消息列�?
     *
     * @param uuid       对话的uuid
     * @param maxMsgUuid 最大uuid（转换成id进行判断�?
     * @param pageSize   每页数量
     * @return 列表
     */
    public ConvMsgListResp detail(String uuid, String maxMsgUuid, int pageSize) {
        Conversation conversation = this.lambdaQuery().eq(Conversation::getUuid, uuid).one();
        if (null == conversation) {
            log.error("conversation not exist, uuid: {}", uuid);
            throw new BaseException(A_CONVERSATION_NOT_EXIST);
        }

        long maxId = Long.MAX_VALUE;
        if (StringUtils.isNotBlank(maxMsgUuid)) {
            ConversationMessage maxMsg = conversationMessageService.lambdaQuery()
                    .select(ConversationMessage::getId)
                    .eq(ConversationMessage::getUuid, maxMsgUuid)
                    .eq(ConversationMessage::getIsDeleted, false)
                    .one();
            if (null == maxMsg) {
                throw new BaseException(A_DATA_NOT_FOUND);
            }
            maxId = maxMsg.getId();
        }

        List<ConversationMessage> questions = conversationMessageService.listQuestionsByConvId(conversation.getId(), maxId, pageSize);
        if (questions.isEmpty()) {
            return new ConvMsgListResp(StringUtils.EMPTY, Collections.emptyList());
        }
        String minUuid = questions.stream().reduce(questions.get(0), (a, b) -> {
            if (a.getId() < b.getId()) {
                return a;
            }
            return b;
        }).getUuid();
        //Wrap question content
        List<ConvMsgDto> userMessages = MPPageUtil.convertToList(questions, ConvMsgDto.class, (source, target) -> {
            if (StringUtils.isNotBlank(source.getAttachments())) {
                List<String> urls = fileService.getUrls(Arrays.stream(source.getAttachments().split(",")).toList());
                target.setAttachmentUrls(urls);
            } else {
                target.setAttachmentUrls(Collections.emptyList());
            }
            if (StringUtils.isNotBlank(source.getAudioUuid())) {
                target.setAudioUrl(fileService.getUrl(source.getAudioUuid()));
            } else {
                target.setAudioUrl("");
            }
            return target;
        });
        ConvMsgListResp result = new ConvMsgListResp(minUuid, userMessages);

        //Wrap answer content
        List<Long> parentIds = questions.stream().map(ConversationMessage::getId).toList();
        List<ConversationMessage> childMessages = conversationMessageService
                .lambdaQuery()
                .in(ConversationMessage::getParentMessageId, parentIds)
                .eq(ConversationMessage::getIsDeleted, false)
                .list();
        Map<Long, List<ConversationMessage>> idToMessages = childMessages.stream().collect(Collectors.groupingBy(ConversationMessage::getParentMessageId));

        //Fill AI answer to the request of user
        result.getMsgList().forEach(item -> {
            List<ConvMsgDto> children = MPPageUtil.convertToList(idToMessages.get(item.getId()), ConvMsgDto.class);
            if (children.size() > 1) {
                children = children.stream().sorted(Comparator.comparing(ConvMsgDto::getCreateTime).reversed()).toList();
            }

            for (ConvMsgDto convMsgDto : children) {
                AiModel aiModel = MODEL_ID_TO_OBJ.get(convMsgDto.getAiModelId());
                convMsgDto.setAiModelPlatform(null == aiModel ? "" : aiModel.getPlatform());
                if (StringUtils.isNotBlank(convMsgDto.getAudioUuid())) {
                    convMsgDto.setAudioUrl(fileService.getUrl(convMsgDto.getAudioUuid()));
                } else {
                    convMsgDto.setAudioUrl("");
                }
            }
            item.setChildren(children);
        });
        return result;
    }

    public int createDefault(Long userId) {
        Conversation conversation = new Conversation();
        conversation.setUuid(UuidUtil.createShort());
        conversation.setUserId(userId);
        conversation.setTitle(NexusConstant.ConversationConstant.DEFAULT_NAME);
        return baseMapper.insert(conversation);
    }

    public Conversation createByFirstMessage(Long userId, String uuid, String title) {
        Conversation conversation = new Conversation();
        conversation.setUuid(uuid);
        conversation.setUserId(userId);
        conversation.setTitle(StringUtils.substring(title, 0, 45));
        baseMapper.insert(conversation);

        return this.lambdaQuery().eq(Conversation::getUuid, uuid).oneOpt().orElse(null);
    }

    public ConvDto add(ConvAddReq convAddReq) {
        Conversation conversation = this.lambdaQuery()
                .eq(Conversation::getUserId, ThreadContext.getCurrentUserId())
                .eq(Conversation::getTitle, convAddReq.getTitle())
                .eq(Conversation::getIsDeleted, false)
                .one();
        if (null != conversation) {
            throw new BaseException(A_CONVERSATION_TITLE_EXIST);
        }

        List<Long> filteredMcpIds = filterEnableMcpIds(convAddReq.getMcpIds());
        List<Long> filteredKbIds = filterEnableKbIds(ThreadContext.getCurrentUser(), convAddReq.getKbIds());

        String uuid = UuidUtil.createShort();
        Conversation one = new Conversation();
        BeanUtils.copyProperties(convAddReq, one);
        one.setUuid(uuid);
        one.setUserId(ThreadContext.getCurrentUserId());
        one.setMcpIds(StringUtils.join(filteredMcpIds, ","));
        one.setKbIds(StringUtils.join(filteredKbIds, ","));
        if (null != convAddReq.getAudioConfig()) {
            one.setAudioConfig(convAddReq.getAudioConfig());
        }
        baseMapper.insert(one);

        Conversation conv = this.lambdaQuery().eq(Conversation::getUuid, uuid).one();
        ConvDto dto = MPPageUtil.convertTo(conv, ConvDto.class);
        setMcpToDto(conv, dto);
        setKbInfoToDto(conv, dto);
        return dto;
    }

    /**
     * 组装MCP信息
     *
     * @param conversation 对话信息
     * @param dto          对话DTO
     */
    private void setMcpToDto(Conversation conversation, ConvDto dto) {
        if (StringUtils.isNotBlank(conversation.getMcpIds())) {
            dto.setMcpIds(Arrays.stream(conversation.getMcpIds().split(","))
                    .map(Long::parseLong)
                    .toList());
        } else {
            dto.setMcpIds(new ArrayList<>());
        }
    }

    /**
     * 组装已关联的知识库信�?
     *
     * @param conv 对话信息
     * @param dto  对话DTO
     */
    private void setKbInfoToDto(Conversation conv, ConvDto dto) {
        //组装已关联的知识库信�?
        List<Long> kids = new ArrayList<>();
        List<ConvKnowledge> convKnowledgeList = new ArrayList<>();
        if (StringUtils.isNotBlank(conv.getKbIds())) {
            List<Long> kbIds = Arrays.stream(conv.getKbIds().split(","))
                    .map(Long::parseLong)
                    .toList();
            knowledgeBaseService.listByIds(kbIds).forEach(kb -> {
                ConvKnowledge convKnowledge = convertToConvKbDto(ThreadContext.getCurrentUser(), kb);
                // Skip if not mine and not public
                if (!convKnowledge.getIsMine() && !convKnowledge.getIsPublic()) {
                    convKnowledge.setKbInfo(null);
                    convKnowledge.setIsEnable(false);
                }
                convKnowledgeList.add(convKnowledge);
                kids.add(kb.getId());
            });
        }
        dto.setKbIds(kids);
        dto.setConvKnowledgeList(convKnowledgeList);
    }

    /**
     * 根据预设会话创建当前用户会话
     *
     * @param presetConvUuid 预设会话uuid
     */
    public ConvDto addByPresetConv(String presetConvUuid) {
        ConversationPreset presetConv = this.conversationPresetService.lambdaQuery()
                .eq(ConversationPreset::getUuid, presetConvUuid)
                .eq(ConversationPreset::getIsDeleted, false)
                .oneOpt()
                .orElseThrow(() -> new BaseException(A_PRESET_CONVERSATION_NOT_EXIST));
        ConversationPresetRel presetRel = this.conversationPresetRelService.lambdaQuery()
                .eq(ConversationPresetRel::getUserId, ThreadContext.getCurrentUserId())
                .eq(ConversationPresetRel::getPresetConvId, presetConv.getId())
                .eq(ConversationPresetRel::getIsDeleted, false)
                .oneOpt()
                .orElse(null);
        if (null != presetRel) {
            Conversation conv = this.getById(presetRel.getUserConvId());
            return MPPageUtil.convertTo(conv, ConvDto.class);
        }
        ConvAddReq convAddReq = ConvAddReq.builder()
                .title(presetConv.getTitle())
                .remark(presetConv.getRemark())
                .aiSystemMessage(presetConv.getAiSystemMessage())
                .build();
        ConvDto convDto = self.add(convAddReq);
        conversationPresetRelService.save(
                ConversationPresetRel.builder()
                        .presetConvId(presetConv.getId())
                        .userConvId(convDto.getId())
                        .userId(ThreadContext.getCurrentUserId())
                        .build()
        );
        return convDto;
    }

    public boolean edit(String uuid, ConvEditReq convEditReq) {
        Conversation conversation = getOrThrow(uuid);
        Conversation one = new Conversation();
        BeanUtils.copyProperties(convEditReq, one);
        one.setId(conversation.getId());
        if (null != convEditReq.getUnderstandContextEnable()) {
            one.setUnderstandContextEnable(convEditReq.getUnderstandContextEnable());
        }
        if (null != convEditReq.getMcpIds()) {
            List<Long> filteredMcpIds = filterEnableMcpIds(convEditReq.getMcpIds());
            if (filteredMcpIds.isEmpty()) {
                one.setMcpIds(StringUtils.join(filteredMcpIds, ","));
            }
        }
        if (null != convEditReq.getKbIds()) {
            if (convEditReq.getKbIds().isEmpty()) {
                one.setKbIds("");
            } else {
                List<Long> filteredKbIds = filterEnableKbIds(ThreadContext.getCurrentUser(), convEditReq.getKbIds());
                one.setKbIds(StringUtils.join(filteredKbIds, ","));
            }
        }
        if (null != convEditReq.getAudioConfig()) {
            one.setAudioConfig(convEditReq.getAudioConfig());
        }
        return baseMapper.updateById(one) > 0;
    }

    @Transactional
    public boolean softDel(String uuid) {
        Conversation conversation = getOrThrow(uuid);
        conversationPresetRelService.softDelBy(conversation.getUserId(), conversation.getId());
        return this.lambdaUpdate()
                .eq(Conversation::getId, conversation.getId())
                .set(Conversation::getIsDeleted, true)
                .update();
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

    private Conversation getOrThrow(String uuid) {
        Conversation conversation = this.lambdaQuery()
                .eq(Conversation::getUuid, uuid)
                .eq(Conversation::getIsDeleted, false)
                .one();
        if (null == conversation) {
            throw new BaseException(A_CONVERSATION_NOT_EXIST);
        }
        if (!conversation.getUserId().equals(ThreadContext.getCurrentUserId()) && !ThreadContext.getCurrentUser().getIsAdmin()) {
            throw new BaseException(A_USER_NOT_AUTH);
        }
        return conversation;
    }

    /**
     * 过滤出有效的MCP服务id列表 | Filter the list of valid MCP service IDs
     *
     * @param mcpIdsInReq 请求中传入的MCP服务id列表 | List of MCP service IDs passed in the request
     * @return 有效的MCP服务id列表 | List of valid MCP service IDs
     */
    private List<Long> filterEnableMcpIds(List<Long> mcpIdsInReq) {
        List<Long> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(mcpIdsInReq)) {
            return result;
        }
        List<UserMcp> userMcpList = userMcpService.searchEnableByUserId(ThreadContext.getCurrentUserId());

        for (Long mcpIdInReq : mcpIdsInReq) {
            if (userMcpList.stream().anyMatch(item -> item.getMcpId().equals(mcpIdInReq))) {
                result.add(mcpIdInReq);
            } else {
                log.warn("User mcp id {} not found or disabled in user mcp list, userId: {}, mcpId:{}", mcpIdInReq, ThreadContext.getCurrentUserId(), mcpIdInReq);
            }
        }
        return result;
    }

    public List<Long> filterEnableKbIds(User user, List<Long> kbIdsInReq) {
        if (CollectionUtils.isEmpty(kbIdsInReq)) {
            return Collections.emptyList();
        }
        List<KbInfoResp> validKbList = filterEnableKb(user, kbIdsInReq);
        return validKbList.stream().map(KbInfoResp::getId).toList();
    }

    /**
     * 过滤出有效的知识库id列表 | Find the list of valid knowledge base IDs
     * 如果知识库是别人的且不是公开的，则不属于有效的可以关联的知识�?
     *
     * @param user 当前用户 | Current user
     * @param ids  知识库id列表 | List of knowledge base IDs
     * @return 有效的知识库列表 | List of valid knowledge base
     */
    public List<KbInfoResp> filterEnableKb(User user, List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return knowledgeBaseService.listByIds(ids).stream()
                .filter(item -> item.getIsPublic() || user.getUuid().equals(item.getOwnerUuid()))
                .toList();
    }

    private ConvKnowledge convertToConvKbDto(User user, KbInfoResp kbInfo) {
        ConvKnowledge result = new ConvKnowledge();
        BeanUtils.copyProperties(kbInfo, result);
        result.setKbInfo(kbInfo);
        result.setIsMine(user.getUuid().equals(kbInfo.getOwnerUuid()));
        return result;
    }
}
