package net.fileme.handler;

import net.fileme.logging.RequestLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
public class AccessDeniedExceptionHandler implements AccessDeniedHandler {

    private Logger log = LoggerFactory.getLogger(AccessDeniedExceptionHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        RequestLog reqLog = new RequestLog();
        Object userId = request.getAttribute("parsedJwt");
        if(userId != null){
            reqLog.setUserId(Long.valueOf(userId.toString()));
        }
        reqLog.setRoute(request.getRequestURI());
//        reqLog.setParam(getParameter(method, joinPoint.getArgs()));
        log.error("AccessDeniedException occurred:");
        log.error(reqLog.toString());
        request.getRequestDispatcher("/access-denied").forward(request, response);
    }
}
