package net.fileme.logging;

import net.fileme.domain.dto.EmailDto;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class EmailLogAspect {
    private Logger log = LoggerFactory.getLogger(EmailLogAspect.class);

    @Pointcut("execution(public * net.fileme.service.impl.EmailServiceImpl.*(..))")
    public void sendSysEmail(){
    }

    @AfterReturning("sendSysEmail()")
    public void logSendEmail(JoinPoint jp){
        Object[] args = jp.getArgs();
        EmailDto dto = (EmailDto) args[0];
        log.info("email has been sent to " + dto.getReceiver() + ", subject: " + dto.getSubject());
    }
}
