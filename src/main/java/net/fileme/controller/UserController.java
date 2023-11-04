package net.fileme.controller;

import net.fileme.domain.MyUserDetails;
import net.fileme.domain.dto.TokenDto;
import net.fileme.domain.pojo.User;
import net.fileme.enums.EmailTemplateEnum;
import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.UnauthorizedException;
import net.fileme.service.LoginService;
import net.fileme.service.UserEmailService;
import net.fileme.service.ValidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Controller
public class UserController {
    @Autowired
    private UserEmailService userEmailService;
    @Autowired
    private ValidateService validateService;
    @Autowired
    private LoginService loginService;

    // ----------------------------Sign Up----------------------------- //
    @PostMapping("/support/sign-up")
    public String signUp(@Validated(User.Create.class) @NotNull @RequestBody User guest, Model model){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.SIGN_UP;

        userEmailService.createUser(guest);
        userEmailService.sendTokenEmail(templateEnum, guest.getEmail(), null);
        model.addAttribute("viewText", templateEnum.getAsyncViewText());
        return templateEnum.getAsyncView();
    }

    @GetMapping("/support/sign-up/{token}")
    public String signUpToken(@NotNull @PathVariable String token, Model model){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.SIGN_UP;

        userEmailService.processSignUp(token);
        userEmailService.deleteToken(token);

        model.addAttribute("viewText", templateEnum.getViewText());
        return templateEnum.getView();
    }
    // ----------------------------Change Pwd----------------------------- //
    @PostMapping("/user/setting/password")
    public String changePwd(Model model, @NotNull @RequestParam String oldPassword
            , @NotNull @RequestParam String newPassword
            , @AuthenticationPrincipal MyUserDetails myUserDetails){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.SET_PWD;

        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();
        String currPassword = myUserDetails.getPassword();
        String email = new String(myUserDetails.getUser().getEmail()); // for async method after log out

        userEmailService.matchCurrentPwd(oldPassword, currPassword);
        validateService.regexPwd(newPassword); // workaround: @Pattern validation not working
        userEmailService.processChangePwd(userId, newPassword);
        userEmailService.createBasicEmail(EmailTemplateEnum.RESET_NOTICE, email);

        loginService.logout();

        model.addAttribute("viewText", templateEnum.getAsyncViewText());
        return templateEnum.getAsyncView();
    }
    // ----------------------------Change Email----------------------------- //
    @PostMapping("/user/setting/email")
    public String changeEmail(@NotNull @RequestParam String newEmail, Model model
            , @AuthenticationPrincipal MyUserDetails myUserDetails) {
        EmailTemplateEnum templateEnum = EmailTemplateEnum.SET_EMAIL;
        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        String oldEmail = new String(myUserDetails.getUser().getEmail()); // for async method after log out

        validateService.regexEmail(newEmail); // workaround: @Email validation not working
        validateService.checkEmail(newEmail);
        userEmailService.setUserState(templateEnum, oldEmail);
        userEmailService.sendTokenEmailForEmailChange(templateEnum, oldEmail, newEmail);

        loginService.logout();

        model.addAttribute("viewText", templateEnum.getAsyncViewText());
        return templateEnum.getAsyncView();
    }
    @GetMapping("/support/verify/{token}")
    public String verifyToken(@NotNull @PathVariable String token, Model model){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.DEFAULT;

        TokenDto dto = userEmailService.processChangeEmail(token);
        String email = new String(dto.getReqEmail()); // for async method after deleting token
        userEmailService.createBasicEmail(EmailTemplateEnum.SET_EMAIL_NOTICE, email);
        userEmailService.deleteToken(token);

        model.addAttribute("viewText", templateEnum.getViewText());
        return templateEnum.getView();
    }
    // ----------------------------Reset Password----------------------------- //
    @PostMapping("/support/password")
    public String reset(@NotNull @RequestParam String email, Model model){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.RESET;

        validateService.regexEmail(email); // workaround: @Email validation not working
        userEmailService.setUserState(templateEnum, email);
        userEmailService.sendTokenEmail(templateEnum, email, null);

        model.addAttribute("viewText", templateEnum.getAsyncViewText());
        return templateEnum.getAsyncView();
    }
    @GetMapping("/support/reset/{token}")
    public String resetLink(@NotNull @PathVariable String token, Model model){
        userEmailService.lookUpToken(token);
        model.addAttribute("token", token);
        return "reset";
    }
    @PostMapping("/support/reset")
    public String reset(@NotNull @RequestParam String password, @NotNull @RequestParam String token, Model model){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.RESET;

        validateService.regexPwd(password); // workaround: @Pattern validation not working
        TokenDto dto = userEmailService.processResetPassword(password, token);
        String email = new String(dto.getReqEmail()); // for async method after deleting token
        userEmailService.createBasicEmail(EmailTemplateEnum.RESET_NOTICE, email);
        userEmailService.deleteToken(token);

        model.addAttribute("viewText", templateEnum.getViewText());
        return templateEnum.getView();
    }
}
