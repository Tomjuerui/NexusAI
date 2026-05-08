package com.moyz.nexus.common.aop;

import com.moyz.nexus.common.annotation.DistributeLock;
import com.moyz.nexus.common.util.RedisTemplateUtil;
import com.moyz.nexus.common.util.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;


/**
 * 通用分布式锁
 *
 * @author moyz
 */
@Slf4j
@Aspect
@Component
public class DistributeLockAspect {

    private final RedisTemplateUtil redisTemplateUtil;

    public DistributeLockAspect(RedisTemplateUtil redisTemplateUtil) {
        this.redisTemplateUtil = redisTemplateUtil;
    }

    @Around("@annotation(distributeLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributeLock distributeLock) throws Throwable {
        String key = distributeLock.redisKey();
        int expireInSeconds = distributeLock.expireInSeconds();
        boolean continueIfAcquireFail = distributeLock.continueIfAcquireFail();

        String clientId = distributeLock.clientId();
        if (StringUtils.isBlank(clientId)) {
            clientId = UuidUtil.createShort();
        }

        boolean lockAndContinue = checkAndLock(key, clientId, expireInSeconds, continueIfAcquireFail);
        if (!lockAndContinue) {
            log.warn("该次请求忽略");
            return false;
        }
        try {
            return joinPoint.proceed();
        } finally {
            boolean unlockResult = redisTemplateUtil.unlock(key, clientId);
            log.info("unlock:{},key:{},clientId:{}", unlockResult, key, clientId);
        }
    }

    /**
     * 校验参数及加�?
     *
     * @param key                   redis key
     * @param clientId              加锁方标�?
     * @param expireInSeconds       超时时间 （秒�?
     * @param continueIfAcquireFail 获取锁失败是否继续执行后面的业务逻辑
     * @return 是否成功加锁
     */
    private boolean checkAndLock(String key, String clientId, int expireInSeconds, boolean continueIfAcquireFail) {
        log.info("lock info,key:{},clientId:{},expireInSecond:{},continueIfAcquireFail:{}", key, clientId, expireInSeconds, continueIfAcquireFail);
        if (StringUtils.isBlank(key) || expireInSeconds < 1) {
            log.warn("加锁参数有误，请确认后再操作");
            throw new IllegalArgumentException("加锁参数有误，请确认后再操作");
        }
        boolean lock = redisTemplateUtil.lock(key, clientId, expireInSeconds);
        if (!lock && !continueIfAcquireFail) {
            log.warn("由于参数continueIfAcquireFail为false并且获取锁失败，此次请求忽略");
            return false;
        }
        return lock;
    }
}
