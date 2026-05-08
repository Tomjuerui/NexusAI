package com.moyz.nexus.common.helper;

import com.moyz.nexus.common.vo.RequestRateLimit;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimitHelper {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 按固定时间窗口计算请求次�?
     *
     * @param requestTimesKey redis key
     * @param rateLimitConfig 请求频率限制配置
     * @return
     */
    public boolean checkRequestTimes(String requestTimesKey, RequestRateLimit rateLimitConfig) {
        int requestCountInTimeWindow = 0;
        String rateLimitVal = stringRedisTemplate.opsForValue().get(requestTimesKey);
        if (StringUtils.isNotBlank(rateLimitVal)) {
            requestCountInTimeWindow = Integer.parseInt(rateLimitVal);
        }
        return requestCountInTimeWindow < rateLimitConfig.getTimes();
    }

    public void increaseRequestTimes(String requestTimesKey, RequestRateLimit rateLimitConfig) {
        long expireTime = stringRedisTemplate.getExpire(requestTimesKey).longValue();
        if (expireTime == -1) {
            stringRedisTemplate.opsForValue().increment(requestTimesKey);
            stringRedisTemplate.opsForValue().set(requestTimesKey, String.valueOf(1), rateLimitConfig.getMinutes(), TimeUnit.MINUTES);
        } else if (expireTime > 3) {
            //If expireTime <= 3, too short to cache
            stringRedisTemplate.opsForValue().increment(requestTimesKey);
        }
    }

}
