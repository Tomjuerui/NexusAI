package com.moyz.nexus.common.util;

import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;

import static com.moyz.nexus.common.cosntant.RedisKeyConstant.TOKEN_USAGE_KEY;

@Slf4j
public class LLMTokenUtil {

    /**
     * 缓存token使用情况
     *
     * @param stringRedisTemplate stringRedisTemplate
     * @param uuid                唯一标识
     * @param tokenUsage          token使用�?
     */
    public static void cacheTokenUsage(StringRedisTemplate stringRedisTemplate, String uuid, TokenUsage tokenUsage) {
        String redisKey = MessageFormat.format(TOKEN_USAGE_KEY, uuid);
        stringRedisTemplate.expire(redisKey, Duration.ofMinutes(10));
        stringRedisTemplate.opsForList().rightPushAll(redisKey, String.valueOf(tokenUsage.inputTokenCount()), String.valueOf(tokenUsage.outputTokenCount()));
    }

    /**
     * 计算缓存在redis中的token使用情况
     *
     * @param stringRedisTemplate stringRedisTemplate
     * @param uuid                唯一标识
     * @return Pair<Integer, Integer> Pair<输入token数量, 输出token数量>
     */
    public static Pair<Integer, Integer> calAllTokenCostByUuid(StringRedisTemplate stringRedisTemplate, String uuid) {
        List<String> tokenCountList = stringRedisTemplate.opsForList().range(MessageFormat.format(TOKEN_USAGE_KEY, uuid), 0, -1);
        int inputTokenCount = 0;
        int outputTokenCount = 0;
        if (!CollectionUtils.isEmpty(tokenCountList) && tokenCountList.size() > 1) {
            int tokenCountListSize = tokenCountList.size();
            int i = 0;
            while (i < tokenCountListSize) {
                inputTokenCount += Integer.parseInt(tokenCountList.get(i));
                i++;
                if (i < tokenCountListSize) {
                    outputTokenCount += Integer.parseInt(tokenCountList.get(i));
                }
                i++;
            }
        }
        return Pair.of(inputTokenCount, outputTokenCount);
    }
}
