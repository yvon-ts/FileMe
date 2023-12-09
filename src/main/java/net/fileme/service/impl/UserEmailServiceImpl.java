package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.fileme.domain.dto.EmailDto;
import net.fileme.domain.dto.TokenDto;
import net.fileme.domain.dto.UserDto;
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
import net.fileme.utils.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private RedisCache redisCache;
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
    public User createUser(UserDto guest){
        String tmpPassword = guest.getPassword();
        String password = passwordEncoder.encode(tmpPassword);

        User user = new User();
        user.setUsername(guest.getUsername());
        user.setPassword(password);
        user.setEmail(guest.getEmail());
        save(user);
        return user;
    }
    @Override
    public void processSignUp(String token){
        TokenDto dto = lookUpToken(token);
        boolean success = doSignUp(dto);
        if(!success){
            throw new InternalErrorException(ExceptionEnum.SIGN_UP_FAIL);
        }
    }
    public boolean doSignUp(TokenDto dto){
        LambdaUpdateWrapper<User> luw = new LambdaUpdateWrapper<>();
        luw.set(User::getState, 0)
                .eq(User::getEmail, dto.getReqEmail())
                .eq(User::getState, dto.getMapping());
        return update(luw);
    }
    // ----------------------------Change Password----------------------------- //
    @Override
    public void matchCurrentPwd(String inputPwd, String currentPwd){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if(!encoder.matches(inputPwd, currentPwd)) throw new UnauthorizedException(ExceptionEnum.WRONG_PWD);
    }
    @Override
    public void processChangePwd(Long userId, String newPwd){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = encoder.encode(newPwd);
        LambdaUpdateWrapper<User> luw = new LambdaUpdateWrapper<>();
        luw.set(User::getPassword, password)
                .eq(User::getId, userId)
                .eq(User::getState, 0);
        boolean success = update(luw);
        if(!success) throw new InternalErrorException(ExceptionEnum.CHANGE_PWD_FAIL);
    }
    // ----------------------------Change Email----------------------------- //
    @Override
    public TokenDto processChangeEmail(String token){
        TokenDto dto = lookUpToken(token);
        boolean success = doChangeEmail(dto);
        if(!success) throw new InternalErrorException(ExceptionEnum.CHANGE_EMAIL_FAIL);
        return dto;
    }

    public boolean doChangeEmail(TokenDto dto){
        LambdaUpdateWrapper<User> luw = new LambdaUpdateWrapper<>();
        luw.set(User::getEmail, dto.getPending())
                .set(User::getState, 0)
                .eq(User::getEmail, dto.getReqEmail())
                .eq(User::getState, dto.getMapping());
        return update(luw);
    }
    // ----------------------------Reset Password----------------------------- //
    @Override
    public TokenDto processResetPassword(String rawPassword, String token){
        TokenDto dto = lookUpToken(token);
        String password = passwordEncoder.encode(rawPassword);
        dto.setPending(password);
        boolean success = doResetPassword(dto);
        if(!success){
            throw new InternalErrorException(ExceptionEnum.PWD_RESET_FAIL);
        }
        return dto;
    }
    public boolean doResetPassword(TokenDto dto){
        LambdaUpdateWrapper<User> luw = new LambdaUpdateWrapper<>();
        luw.set(User::getPassword, dto.getPending())
                .set(User::getState, 0)
                .eq(User::getEmail, dto.getReqEmail())
                .eq(User::getState, dto.getMapping());
        return update(luw);
    }
    // ----------------------------Util Methods----------------------------- //

    @Override
    public void setUserState(EmailTemplateEnum templateEnum, String email){
        Integer mapping = emailTemplateService.findTemplate(templateEnum).getMapping();
        LambdaUpdateWrapper<User> luw = new LambdaUpdateWrapper<>();
        luw.set(User::getState, mapping)
                .eq(User::getEmail, email)
                .and(i -> i
                        .eq(User::getState, 0)
                        .or()
                        .eq(User::getState, mapping)
                );
        boolean success = update(luw);
        if(!success){
            throw new InternalErrorException(ExceptionEnum.USER_STATE_ERROR);
        }
    }
    @Async
    @Override
    public void sendTokenEmail(EmailTemplateEnum templateEnum, String emailReceiver, String pending){
        TokenDto dto = createTokenDto(templateEnum.getMapping(), emailReceiver, pending);
        String token = createToken(dto);
        EmailTemplate template = emailTemplateService.findTemplate(templateEnum);
        createTokenEmail(template, emailReceiver, token);
    }
    @Async
    @Override // different email receiver when changing email
    public void sendTokenEmailForEmailChange(EmailTemplateEnum templateEnum, String oldEmail, String newEmail){
        TokenDto dto = createTokenDto(templateEnum.getMapping(), oldEmail, newEmail);
        String token = createToken(dto);
        EmailTemplate template = emailTemplateService.findTemplate(templateEnum);
        createTokenEmail(template, newEmail, token);
    }

    /**
     *
     * @param mapping: mapping with EmailTemplateEnum and user state
     * @param reqEmail: request from which email
     * @param pending: data to be handled later
     * @return: above attribute with latest token issue no.
     */
    public<T> TokenDto createTokenDto(Integer mapping, String reqEmail, T pending){
        Integer issueNo = nextIssueNo(reqEmail);
        return new TokenDto(mapping, reqEmail, pending, issueNo);
    }
    public String createToken(TokenDto dto){
        String token = "";
        boolean isUnique = false;
        while(!isUnique){
            token = RandomUtil.createToken();
            isUnique = redisCache.setUniqueObj(token, dto, timeout, timeUnit);
        }
        log.info("token generated for " + dto.getReqEmail());
        return token;
    }
    public Integer nextIssueNo(String reqEmail){
        Integer issueNo = redisCache.getRedisValue(reqEmail);
        if(Objects.isNull(issueNo)){
            redisCache.setObj(reqEmail, 1, timeout, timeUnit);
            return 1;
        }else{
            issueNo++;
            redisCache.setObj(reqEmail, issueNo, timeout, timeUnit);
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
    @Override
    public TokenDto lookUpToken(String token){
        boolean hasKey = redisCache.hasKey(token);
        if(!hasKey){
            throw new UnauthorizedException(ExceptionEnum.NO_SUCH_TOKEN);
        }else{
            TokenDto dto = redisCache.getRedisValue(token);
            Integer dtoIssueNo = dto.getIssueNo();
            Integer currentIssueNo = redisCache.getRedisValue(dto.getReqEmail());
            if(currentIssueNo != null && currentIssueNo != 0 && Objects.equals(currentIssueNo, dtoIssueNo)){
                return dto;
            }else{
                throw new ConflictException(ExceptionEnum.WRONG_TOKEN_VERSION);
            }
        }
    }
    @Async
    @Override
    public void deleteToken(String token){
        redisCache.deleteObj(token);
        log.info("Used token has been removed.");
    }
}
