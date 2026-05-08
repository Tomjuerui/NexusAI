package com.moyz.nexus.common.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 打印controller的请求参�?
 *
 * @author moyz
 * date:2021-07-15 03:16:59
 */
@Aspect
@Component
public class ControllerParamsLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ControllerParamsLogAspect.class);

    @Pointcut("execution(public * com.nexus.*.controller..*.*(..))")
    public void controllerMethods() {
    }

    @Before("controllerMethods()")
    public void before(JoinPoint joinPoint) {
        ParamsLogAspect.paramsLog(joinPoint, logger);
    }


}
