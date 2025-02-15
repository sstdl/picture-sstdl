//package com.sstdl.picturebackend.interceptor;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StopWatch;
//import org.springframework.web.context.request.RequestAttributes;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.UUID;
//
///**
// * @author SSTDL
// * @description 日志拦截器
// */
//@Aspect
//@Component
//@Slf4j
//public class RequestInterceptor {
//    /**
//     * 执行拦截
//     * @param proceedingJoinPoint
//     * @return
//     * @throws Throwable
//     */
//    @Around("execution(* com.sstdl.picturebackend.controller..*(..))")
//    public Object doInterceptor(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
//        // 计时
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//        // 获取请求信息
//        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
//        // 生成请求id
//        String requestId = UUID.randomUUID().toString();
//        String url = request.getRequestURI();
//        // 获取请求参数
//        Object[] args = proceedingJoinPoint.getArgs();
//        String reqParam = "[" + StringUtils.join(args, ", ") + "]";
//        // 输出请求日志
//        // log.info("请求开始，请求id: {}, 请求路径: {}, 请求ip: {}, params: {}", requestId, url, request.getRemoteHost(), reqParam);
//        log.info("请求开始，请求路径:{}", url);
//        // 执行原方法
//        Object result = proceedingJoinPoint.proceed();
//        // 输出响应日志
//        stopWatch.stop();
//        long totalTimeMillis = stopWatch.getTotalTimeMillis();
//        // log.info("请求结束, 请求id: {}, 请求耗时: {}ms", requestId, totalTimeMillis);
//        log.info("请求结束, 请求耗时:{}ms", totalTimeMillis);
//        return result;
//    }
//}
