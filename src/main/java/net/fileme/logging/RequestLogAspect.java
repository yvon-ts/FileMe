package net.fileme.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Aspect
public class RequestLogAspect {

    private Logger log = LoggerFactory.getLogger(RequestLogAspect.class);
    @Pointcut("execution(public * net.fileme.controller.*.*(..))")
    public void reqLog(){}

//    @Before("reqLog()")
//    public void doBefore(JoinPoint jointPoint) throws Throwable{
//    }
//
//    @AfterReturning(value = "reqLog()", returning = "ret")
//    public void doAfterReturning(Object ret) throws Throwable{
//    }

    @Around("reqLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable{
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod(); // 取得核心方法

        RequestLog reqLog = new RequestLog();
        reqLog.setRoute(request.getRequestURI());
        reqLog.setParam(getParameter(method, joinPoint.getArgs()));
        log.info(reqLog.toString());

        Object result = joinPoint.proceed(); // 核心方法執行

        return result;
    }
    // 取得方法參數名稱&數值
    private Object getParameter(Method method, Object[] args){
        List<Object> argList = new ArrayList<>();
        Parameter[] params = method.getParameters();
        for(int i = 0; i < params.length; i++){
            RequestBody requestBody = params[i].getAnnotation(RequestBody.class);
            if(requestBody != null){
                argList.add(args[i]);
            }
            RequestParam requestParam = params[i].getAnnotation(RequestParam.class);
            if(requestParam != null){
                Map<String, Object> map = new HashMap<>();
                String key = params[i].getName();
                if(!StringUtils.isEmpty(requestParam.value())){
                    key = requestParam.value();
                }
                map.put(key, args[i]);
                argList.add(map);
            }
        }
        if(argList.isEmpty()) return null;
        if(argList.size() == 1) return argList.get(0);

        return argList;
    }
}
