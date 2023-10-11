package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.fileme.domain.dto.EmailDto;
import net.fileme.domain.dto.TokenDto;
import net.fileme.domain.mapper.UserMapper;
import net.fileme.domain.pojo.EmailTemplate;
import net.fileme.domain.pojo.User;
import net.fileme.enums.EmailTemplateEnum;
import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.ConflictException;
import net.fileme.exception.InternalErrorException;
import net.fileme.exception.UnauthorizedException;
import net.fileme.service.EmailService;
import net.fileme.service.EmailTemplateService;
import net.fileme.service.UserEmailService;
import net.fileme.utils.RandomUtil;
import net.fileme.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class UserEmailServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserEmailService {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private EmailService emailService;
    @Autowired
    private EmailTemplateService emailTemplateService;
    @Value("${mail.token.timeout}")
    private int timeout;
    @Value("${mail.token.time-unit}")
    private TimeUnit timeUnit;

    private Logger log = LoggerFactory.getLogger(UserEmailServiceImpl.class);

    // ----------------------------Sign Up----------------------------- //
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
    @Override // TODO: 看是否可寫成通用
    public void sendSignUpEmail(EmailTemplateEnum templateEnum, String email){
        TokenDto dto = createTokenDto(templateEnum.getMapping(), email, null);
        String token = createToken(dto);
        EmailTemplate template = emailTemplateService.findTemplate(templateEnum);
        createTokenEmail(template, email, token);
    }
    @Override
    public void processSignUp(String token){
        TokenDto dto = lookUpToken(token);
        boolean success = doSignUp(dto);
        if(!success){
            throw new ConflictException(ExceptionEnum.DUPLICATED_EMAIL);
        }
    }
    public boolean doSignUp(TokenDto dto){
        LambdaUpdateWrapper<User> luw = new LambdaUpdateWrapper<>();
        luw.set(User::getState, 0)
                .eq(User::getEmail, dto.getReqEmail())
                .eq(User::getState, dto.getMapping());
        return update(luw);
    }
    // ----------------------------Change Email----------------------------- //
    @Async
    @Override
    public void sendChangeEmail(EmailTemplateEnum templateEnum, Long userId, String reqEmail){
        // TODO: 加security後應該就不用撈資料庫
        String currentEmail = findCurrentEmail(userId);
        TokenDto dto = createTokenDto(templateEnum.getMapping(), reqEmail, currentEmail);
        String token = createToken(dto);
        EmailTemplate template = emailTemplateService.findTemplate(templateEnum);
        createTokenEmail(template, reqEmail, token);
    }
    @Override
    public TokenDto processChangeEmail(String token){
        TokenDto dto = lookUpToken(token);
        boolean success = doChangeEmail(dto);
        if(!success){
            throw new ConflictException(ExceptionEnum.DUPLICATED_EMAIL);
        }
        return dto;
    }

    public boolean doChangeEmail(TokenDto dto){
        LambdaUpdateWrapper<User> luw = new LambdaUpdateWrapper<>();
        luw.set(User::getEmail, dto.getReqEmail())
                .set(User::getState, 0)
                .eq(User::getEmail, dto.getPending())
                .eq(User::getState, dto.getMapping());
        return update(luw);
    }
    // ----------------------------Util Methods----------------------------- //

    @Override
    public void setUserState(EmailTemplateEnum templateEnum, Long userId){
        Integer mapping = emailTemplateService.findTemplate(templateEnum).getMapping();
        LambdaUpdateWrapper<User> luw = new LambdaUpdateWrapper<>();
        luw.set(User::getState, mapping).eq(User::getId, userId);
        boolean success = update(luw);
        if(!success){
            throw new InternalErrorException(ExceptionEnum.USER_STATE_ERROR);
        }
    }
    public<T> TokenDto createTokenDto(Integer mapping, String reqEmail, T pending){
        Integer issueNo = nextIssueNo(reqEmail);
        return new TokenDto(mapping, reqEmail, pending, issueNo);
    }
    public String createToken(TokenDto dto){
        String token = "";
        boolean isUnique = false;
        while(!isUnique){
            token = RandomUtil.createToken();
            isUnique = redisUtil.setUniqueObj(token, dto, timeout, timeUnit);
        }
        log.info("token generated for " + dto.getReqEmail());
        return token;
    }
    public Integer nextIssueNo(String reqEmail){
        Integer issueNo = redisUtil.getRedisValue(reqEmail);
        if(issueNo == null){
            redisUtil.setObj(reqEmail, 1, timeout, timeUnit);
            return 1;
        }else{
            issueNo++;
            redisUtil.setObj(reqEmail, issueNo, timeout, timeUnit);
            return issueNo;
        }
    }
    @Async
    @Override
    public void createBasicEmail(EmailTemplateEnum templateEnum, String emailReceiver){
        EmailTemplate template = emailTemplateService.findTemplate(templateEnum);
        String subject = template.getTitle();

        Context ctx = new Context();
        ctx.setVariable("text", template.getContent());
        String textDto = templateEngine.process("basicEmail", ctx);

        EmailDto dto = new EmailDto();
        dto.setReceiver(emailReceiver);
        dto.setSubject(subject);
        dto.setText(textDto);

        emailService.sendHtmlMail(dto);
    }
    public void createTokenEmail(EmailTemplate template, String emailReceiver, String token){
        String subject = template.getTitle();

        Context ctx = new Context();
        ctx.setVariable("token", token);
        ctx.setVariable("text", template.getContent());
        ctx.setVariable("endpoint", template.getEndpoint());
        ctx.setVariable("btn", template.getBtn());
        String textDto = templateEngine.process("tokenEmail", ctx);

        EmailDto dto = new EmailDto();
        dto.setReceiver(emailReceiver);
        dto.setSubject(subject);
        dto.setText(textDto);

        emailService.sendHtmlMail(dto);
    }
    public TokenDto lookUpToken(String token){
        boolean hasKey = redisUtil.hasKey(token);
        if(!hasKey){
            throw new UnauthorizedException(ExceptionEnum.INVALID_TOKEN);
        }else{
            TokenDto dto = redisUtil.getRedisValue(token);
            Integer dtoIssueNo = dto.getIssueNo();
            Integer currentIssueNo = redisUtil.getRedisValue(dto.getReqEmail());
            if(currentIssueNo != null && currentIssueNo != 0 && Objects.equals(currentIssueNo, dtoIssueNo)){
                return dto;
            }else{
                throw new UnauthorizedException(ExceptionEnum.INVALID_TOKEN);
            }
        }
    }
    public String findCurrentEmail(Long userId){
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        lqw.select(User::getEmail).eq(User::getId, userId);
        return getObj(lqw, o -> o.toString());
    }
    @Async
    @Override
    public void deleteToken(String token){
        redisUtil.deleteObj(token);
        log.info("Used token has been removed.");
    }
}
