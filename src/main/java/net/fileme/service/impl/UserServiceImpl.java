package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.fileme.domain.EmailDto;
import net.fileme.domain.mapper.UserMapper;
import net.fileme.domain.pojo.EmailTemplate;
import net.fileme.enums.EmailTemplateEnum;
import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.InternalErrorException;
import net.fileme.exception.UnauthorizedException;
import net.fileme.service.EmailService;
import net.fileme.service.EmailTemplateService;
import net.fileme.service.UserService;
import net.fileme.domain.pojo.User;
import net.fileme.utils.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.util.StringUtils;

import javax.servlet.ServletContext;
import java.util.concurrent.TimeUnit;

@Service
//@PropertySource("classpath:credentials.properties")
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ServletContext context;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private EmailService emailService;
    @Autowired
    private EmailTemplateService templateService;
    @Autowired
    private RedisCache redisCache;
    @Value("${mail.token.exp}")
    private int exp;
    @Value("${mail.token.time-unit}")
    private TimeUnit timeUnit;

    private Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public User createUser(User guest){
        String username = guest.getUsername();
        String tmpPwd = guest.getPwd();
        String email = guest.getEmail();
        String pwd = passwordEncoder.encode(tmpPwd);

        User user = new User();
        user.setUsername(username);
        user.setPwd(pwd);
        user.setEmail(email);
        save(user);
        return user;
    }
    @Async
    @Override
    public void prepareTokenEmail(EmailTemplateEnum templateEnum, String emailReceiver){
        EmailTemplate template = templateService.findTemplate(templateEnum);
        createAndSendToken(template, emailReceiver);
    }

    public void createAndSendToken(EmailTemplate template, String emailReceiver){
        String subject = template.getTitle();
        String token = redisCache.setUniqueKey(emailReceiver, exp, timeUnit);
        log.info("token generated for " + emailReceiver);

        Context ctx = new Context();
        ctx.setVariable("token", token);
        ctx.setVariable("text", template.getContent());
        ctx.setVariable("endpoint", template.getEndpoint());
        String textDto = templateEngine.process("basicEmail", ctx);

        EmailDto dto = new EmailDto();
        dto.setReceiver(emailReceiver);
        dto.setSubject(subject);
        dto.setText(textDto);

        emailService.sendHtmlMail(dto);
        log.info("email has been sent to " + emailReceiver + ", subject: " + subject);
    }
    @Override
    public void processToken(String token, boolean doClear){
        String email = lookUpToken(token);
        resetUserState(email);
        if(doClear){
            clearToken(token);
        }
    }

    @Override
    public String lookUpToken(String token){
        boolean hasKey = redisCache.hasKey(token);
        if(!hasKey){
           throw new UnauthorizedException(ExceptionEnum.INVALID_TOKEN);
        }else{
            return redisCache.getCacheObject(token).toString();
        }
    }

    public void resetUserState(String email){
        LambdaUpdateWrapper<User> luw = new LambdaUpdateWrapper<>();
        luw.set(User::getState, 0).eq(User::getEmail, email);
        update(luw);
        log.info("user state reset by email: " + email);
    };
    
    @Async
    public void clearToken(String token){
        String value = redisCache.getCacheObject(token);
        if(!StringUtils.isEmpty(value)){
            boolean success = redisCache.deleteObject(token);
            if(!success){
                throw new InternalErrorException(ExceptionEnum.TOKEN_DEL_ERROR);
            }else{
                log.info("token cleared !");
            }
        }
    }
}
