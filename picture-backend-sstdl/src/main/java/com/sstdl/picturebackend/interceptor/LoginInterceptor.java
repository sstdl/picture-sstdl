//package com.sstdl.picturebackend.interceptor;
//
//import com.sstdl.picturebackend.common.ErrorCode;
//import com.sstdl.picturebackend.model.entity.User;
//import com.sstdl.picturebackend.service.UserService;
//import com.sstdl.picturebackend.utils.ThrowUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestAttributes;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//
//import static com.sstdl.picturebackend.constant.UserConstant.USER_LOGIN_STATE;
//
///**
// * @author SSTDL
// * @description 请求校验
// */
//@Aspect
//@Component
//@Slf4j
//public class LoginInterceptor {
//    @Resource
//    private UserService userService;
//
//    /**
//     * 执行拦截
//     * @param proceedingJoinPoint
//     * @return
//     * @throws Throwable
//     */
//    @Around("execution(* com.sstdl.picturebackend.controller..*(..))")
//    public Object doInterceptor(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
//        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
//        String requestURI = request.getRequestURI();
//        if (requestURI.contains("/login") || requestURI.contains("/user/login")) {
//            // 如果是登录接口，则跳过校验
//            return proceedingJoinPoint.proceed();
//        }
//        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
//        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
//        return proceedingJoinPoint.proceed();
//    }
//}
