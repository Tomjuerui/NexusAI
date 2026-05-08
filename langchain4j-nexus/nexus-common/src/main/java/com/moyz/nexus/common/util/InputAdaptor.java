package com.moyz.nexus.common.util;

import com.moyz.nexus.common.rag.TokenEstimatorThreadLocal;
import com.moyz.nexus.common.rag.TokenEstimatorFactory;
import com.moyz.nexus.common.vo.InputAdaptorMsg;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.rag.AugmentationRequest;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.query.Metadata;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * 根据模型设置的maxInputTokens自动调整携带的召回文档及历史记录数量
 */
@Slf4j
public class InputAdaptor {

    public static InputAdaptorMsg isQuestionValid(String userQuestion, int maxInputTokens) {
        return isQuestionValid(userQuestion, maxInputTokens, TokenEstimatorFactory.create(TokenEstimatorThreadLocal.getTokenEstimator()));
    }

    public static InputAdaptorMsg isQuestionValid(String userQuestion, int maxInputTokens, TokenCountEstimator tokenizer) {
        InputAdaptorMsg result = new InputAdaptorMsg();
        result.setTokenTooMuch(InputAdaptorMsg.TOKEN_TOO_MUCH_NOT);

        int questionLength = tokenizer.estimateTokenCountInText(userQuestion);
        result.setUserQuestionTokenCount(questionLength);
        if (questionLength > maxInputTokens) {
            log.warn("用户问题过长,已超过{}个token", maxInputTokens);
            result.setTokenTooMuch(InputAdaptorMsg.TOKEN_TOO_MUCH_QUESTION);
        }
        return result;
    }

    /**
     * 调整携带的历史记�?
     * 请求token可能超长场景之一(请求压缩)：用户原始问�?历史记录
     * @deprecated see dev.langchain4j.memory.chatTokenWindowChatMemory
     * @param augmentationRequest
     * @param maxInputTokens
     * @param tokenCostConsumer
     * @return
     */
    @Deprecated
    public static Metadata adjustMemory(AugmentationRequest augmentationRequest, int maxInputTokens, Consumer<InputAdaptorMsg> tokenCostConsumer) {
//        ChatMessage chatMessage = augmentationRequest.chatMessage();
//        Metadata metadata = augmentationRequest.metadata();
//
//        String tokenizerName = TokenEstimatorThreadLocal.getTokenEstimator();
//        TokenCountEstimator tokenizer = TokenEstimatorFactory.create(tokenizerName);
//        InputAdaptorMsg inputAdaptorMsg = isQuestionValid(chatMessage.toString(), maxInputTokens, tokenizer);
//        if (inputAdaptorMsg.getTokenTooMuch() == InputAdaptorMsg.TOKEN_TOO_MUCH_QUESTION) {
//            tokenCostConsumer.accept(inputAdaptorMsg);
//        }
//        // 计算准备待增强的用户原始问题及历史记录的长度,如果超额，则丢弃部分或全部历史记�?
//        // 对用户问题进行增强是为了更好地召回文�?
//        List<ChatMessage> validMemories = new ArrayList<>();
//        int allMemoryTokenCount = 0;
//        int tokenTooMuch = InputAdaptorMsg.TOKEN_TOO_MUCH_NOT;
//        for (int i = metadata.chatMemory().size() - 1; i >= 0; i--) {
//            String memory = metadata.chatMemory().get(i).text();
//            int currentMemoryTokenCount = tokenizer.estimateTokenCountInText(memory);
//            if (inputAdaptorMsg.getUserQuestionTokenCount() + allMemoryTokenCount + currentMemoryTokenCount < maxInputTokens) {
//                allMemoryTokenCount += currentMemoryTokenCount;
//                validMemories.add(metadata.chatMemory().get(i));
//            } else {
//                tokenTooMuch = InputAdaptorMsg.TOKEN_TOO_MUCH_MEMORY;
//                log.warn("记忆内容过长,丢弃\n>>>>> {} <<<<<", memory.substring(0, Math.min(memory.length(), 30)));
//            }
//        }
//        //重新排序及写入内容适量的记�?
//        Collections.reverse(validMemories);
//
//        inputAdaptorMsg.setTokenTooMuch(tokenTooMuch);
//        inputAdaptorMsg.setMemoryTokenCount(allMemoryTokenCount);
//        tokenCostConsumer.accept(inputAdaptorMsg);
//
//        return Metadata.from(metadata.userMessage(), metadata.chatMemoryId(), validMemories);

        return null;
    }

    /**
     * 调整召回文档
     * 请求token超长场景之二（召回文档成功后，准备结合历史记录前）：原始用户问题+召回文档
     *
     * @param questionLength 原始用户问题长度
     * @param contents       召回文档的内�?
     * @param maxInputTokens
     * @return 截取后的文档内容
     */
    public static List<Content> adjustRetrieveDocs(int questionLength, List<Content> contents, int maxInputTokens) {
        if (contents.isEmpty()) {
            log.info("文档数量�?");
            return Collections.emptyList();
        }
        String tokenizerName = TokenEstimatorThreadLocal.getTokenEstimator();
        TokenCountEstimator tokenizer = TokenEstimatorFactory.create(tokenizerName);
        //计算原始用户问题及召回文档的长度,如果太长，丢弃部分或全部文档
        int allRetrievedDocsTokenCount = 0;
        List<Content> validContents = new ArrayList<>();
        for (Content content : contents) {
            int currentDocTokenCount = tokenizer.estimateTokenCountInText(content.textSegment().text());
            if (questionLength + allRetrievedDocsTokenCount + currentDocTokenCount < maxInputTokens) {
                allRetrievedDocsTokenCount += currentDocTokenCount;
                validContents.add(content);
            } else {
                log.warn("召回文档太长,丢弃\n>>>>> {} <<<<<", content.textSegment().text().substring(0, Math.min(content.textSegment().text().length(), 30)));
            }
        }
        log.info("文档token�?{}", allRetrievedDocsTokenCount);
        return validContents;
    }

    /**
     * 调整准备向LLM请求的消息数量以便适应LLM的maxInputTokens
     * 请求token超长场景之三（召回成功后，结合用户问题、历史记录提交给LLM前）：原始用户问�?召回文档
     *
     * @param messages
     * @param maxInputTokens
     * @return
     */
    @Deprecated
    public static List<ChatMessage> adjustMessages(List<ChatMessage> messages, int maxInputTokens) {
        String tokenizerName = TokenEstimatorThreadLocal.getTokenEstimator();
        TokenCountEstimator tokenizer = TokenEstimatorFactory.create(tokenizerName);
        int messageSize = messages.size();
        ChatMessage latestMessage = messages.get(messageSize - 1);
        List<ChatMessage> result = new ArrayList<>();
//        //最新一条消息（即当前用户的提问）必须留�?
//        result.add(latestMessage);
//        int allTokenCount = 0;
//        if (latestMessage instanceof UserMessage userMessage && userMessage.contents().get(0) instanceof TextContent textContent) {
//            allTokenCount += tokenizer.estimateTokenCountInText(textContent.text());
//        }
//        for (int i = messageSize - 1 - 1; i >= 0; i--) {
//            log.info("messageSize i:{}", i);
//            ChatMessage curMsg = messages.get(i);
//            //多模态时，先不计算token
//            if (curMsg instanceof UserMessage && ((UserMessage) curMsg).contents().stream().anyMatch(item -> item instanceof ImageContent)) {
//                result.add(curMsg);
//            } else {
//
//                log.info("messageSize allTokenCount:{}", allTokenCount);
//                int currentMessageTokenCount = tokenizer.estimateTokenCountInText(curMsg.text());
//                if (allTokenCount + currentMessageTokenCount < maxInputTokens) {
//                    allTokenCount += currentMessageTokenCount;
//                    result.add(curMsg);
//                } else {
//                    log.warn("消息过长,丢弃\n>>>>> {} <<<<<", curMsg.text().substring(0, Math.min(curMsg.text().length(), 30)));
//                    //如果当前是AI的回复，把对应的用户提问也丢�?
//                    if (curMsg instanceof AiMessage) {
//                        i--;
//                        curMsg = messages.get(i);
//                        if (null != curMsg) {
//                            log.warn("对应的用户问题一并丢弃\n>>>>> {} <<<<<", curMsg.text().substring(0, Math.min(curMsg.text().length(), 30)));
//                        }
//                    }
//                }
//            }
//
//        }
//        Collections.reverse(result);
        return result;
    }
}
