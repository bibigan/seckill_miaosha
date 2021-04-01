//package com.java.aop;
//
//import com.java.controller.UsersController;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.Signature;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.servlet.http.HttpServletRequest;
//
//@Aspect
//@Component
//public class AopHandler {
//    private static final Logger LOGGER = LoggerFactory.getLogger(AopHandler.class);
////    请求参数是健值对的记录方法
//    @Around("@annotation(postMapping)")
//    public Object recordCallLog(ProceedingJoinPoint pjp, PostMapping postMapping) throws Throwable {
//        long start = System.currentTimeMillis();
//
//        Signature signature = pjp.getSignature();
//        String clazz = signature.getDeclaringTypeName();
//        String method = signature.getName();
//        Object[] parameterValues = pjp.getArgs();
//
//        StringBuilder builder = new StringBuilder();
//        MethodSignature methodSignature = (MethodSignature) signature;
//        String[] parameterNamess = methodSignature.getParameterNames();
//        for (int i = 0; i < parameterNamess.length; i++) {
//            if (i == 0) {
//                builder.append("?");
//            }
//            builder.append(parameterNamess[i] + "=" + parameterValues[i].toString());
//            if (i < parameterNamess.length - 1) {
//                builder.append("&");
//            }
//        }
//
//        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//        String path = request.getRequestURL().toString();
//
//        LOGGER.info(path + builder.toString());
//
//        Object result = pjp.proceed();
//
//        long usedTime = System.currentTimeMillis() - start;
//
//        LOGGER.info("buy," + method + ",耗时:" + usedTime + "毫秒");
//
//        return result;
//    }
//}
