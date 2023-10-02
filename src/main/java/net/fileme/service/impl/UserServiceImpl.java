package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.fileme.domain.EmailDetails;
import net.fileme.domain.mapper.UserMapper;
import net.fileme.service.EmailService;
import net.fileme.service.UserService;
import net.fileme.domain.pojo.User;
import net.fileme.utils.RandomUtil;
import net.fileme.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.ServletContext;
import java.util.concurrent.TimeUnit;

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

    @Value("${jwt.exp.verify}")
    private int tokenExp;
    @Value("${mail.subject.verify}")
    private String subjectVerify;

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
    public void verifyUser(User user){
        // generate token
        String token = RandomUtil.createToken();
        System.out.println(token);
        Context thymeleafContext = new Context();
        thymeleafContext.setVariable("token", token);
        // put into Redis
        redisCache.setCacheObject(token, user.getEmail(), tokenExp, TimeUnit.MINUTES);
        // set emailDetails
        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setReceiver(user.getEmail());
        emailDetails.setSubject(subjectVerify);
        String emailText = templateEngine.process("verify", thymeleafContext);
        emailDetails.setText(emailText);
        // send
        emailService.sendHtmlMail(emailDetails);
    }

    @Override
    public void setUserVerified(){
        LambdaUpdateWrapper<User> luw = new LambdaUpdateWrapper<>();
        // TODO: eq改成redis value
        luw.set(User::getState, 1).eq(User::getUsername, "3rd");
        update(luw);
    };

}
