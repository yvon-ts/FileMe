package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.fileme.domain.EmailDetails;
import net.fileme.domain.mapper.UserMapper;
import net.fileme.domain.token.BaseToken;
import net.fileme.domain.token.VerifyToken;
import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.UnauthorizedException;
import net.fileme.service.EmailService;
import net.fileme.service.UserService;
import net.fileme.domain.pojo.User;
import net.fileme.utils.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.ServletContext;

@Service
@PropertySource("classpath:credentials.properties")
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
    private RedisCache redisCache;
    @Autowired
    private VerifyToken verifyToken;

    private Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${jwt.exp.verify}")
    private Integer tokenExp;

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

    public void createAndSendToken(BaseToken config, String emailReceiver){
        String description = config.getDescription();
        String subject = config.getMailSubject();

        String token = redisCache.setUniqueKey(emailReceiver, config.getExp(), config.getTimeUnit());
        log.info(description + " token generated for " + emailReceiver);

        Context thymeleafContext = new Context();
        thymeleafContext.setVariable("token", token);

        // set emailDetails
        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setReceiver(emailReceiver);
        emailDetails.setSubject(subject);
        String emailText = templateEngine.process(description, thymeleafContext);
        emailDetails.setText(emailText);

        // send
        emailService.sendHtmlMail(emailDetails);
        log.info("email has been sent to " + emailReceiver + ", subject: " + subject);
    }

    @Async
    @Override
    public void sendVerifyToken(User user){
       createAndSendToken(verifyToken, user.getEmail());
    }

    @Override
    public String verifyToken(String token){
        boolean hasKey = redisCache.hasKey(token);
        if(!hasKey){
           throw new UnauthorizedException(ExceptionEnum.INVALID_TOKEN);
        }else{
            String email = redisCache.getCacheObject(token).toString();
            return email;
        }
    }

    @Override
    public void setUserVerified(String email){
        LambdaUpdateWrapper<User> luw = new LambdaUpdateWrapper<>();
        // TODO: eq改成redis value
        luw.set(User::getState, 1).eq(User::getUsername, "3rd");
        update(luw);
    };

}
