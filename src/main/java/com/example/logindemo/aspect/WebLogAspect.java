package com.example.logindemo.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class WebLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(WebLogAspect.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 定义切点：拦截所有 Controller 层的所有方法
     */
    @Pointcut("execution(* com.example.logindemo.controller.*.*(..))")
    public void controllerPointcut() {}

    /**
     * 环绕通知：在方法执行前后记录日志
     */
    @Around("controllerPointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethod = className + "." + methodName;

        // 2. 获取参数
        Object[] args = joinPoint.getArgs();
        String params = objectMapper.writeValueAsString(args);

        // 3. 记录开始时间
        long startTime = System.currentTimeMillis();

        // 4. 执行原方法
        Object result = joinPoint.proceed();

        // 5. 计算耗时
        long spendTime = System.currentTimeMillis() - startTime;

        // 6. 打印日志
        logger.info("[接口日志] 方法：{}", fullMethod);
        logger.info("[接口日志] 参数：{}", params);
        logger.info("[接口日志] 耗时：{}ms", spendTime);
        logger.info("[接口日志] 结果：{}", objectMapper.writeValueAsString(result));

        return result;
    }
}