package net.fileme.controller;

import net.fileme.domain.dto.TokenDto;
import net.fileme.domain.pojo.User;
import net.fileme.enums.EmailTemplateEnum;
import net.fileme.service.UserEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@Controller
public class UserController {
    @Autowired
    private UserEmailService userEmailService;

    @PostMapping("/sign-up")
    public String signUp(@NotNull @RequestBody User guest, Model model){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.SIGN_UP;
        userEmailService.createUser(guest);
        // TODO: log密碼明文需另外處理
        userEmailService.sendSignUpEmail(templateEnum, guest.getEmail());
        model.addAttribute("viewText", templateEnum.getAsyncViewText());
        return templateEnum.getAsyncView();
    }

    @GetMapping("/sign-up/{token}")
    public String signUpToken(@NotNull @PathVariable String token, Model model){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.SIGN_UP;

        userEmailService.processSignUp(token);
        userEmailService.deleteToken(token);

        model.addAttribute("viewText", templateEnum.getViewText());
        return templateEnum.getView();
    }

    @PostMapping("/user/setting/email")
    public String changeEmail(@NotNull @RequestParam Long userId,
                              @NotNull @RequestParam String newEmail, Model model){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.SET_EMAIL;

        userEmailService.setUserState(templateEnum,userId);
        userEmailService.sendChangeEmail(templateEnum, userId, newEmail);

        model.addAttribute("viewText", templateEnum.getAsyncViewText());
        return templateEnum.getAsyncView();
    }
    @GetMapping("/verify/{token}")
    public String verifyToken(@NotNull @PathVariable String token, Model model){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.DEFAULT;

        TokenDto dto = userEmailService.processChangeEmail(token);
        userEmailService.createBasicEmail(EmailTemplateEnum.SET_EMAIL_NOTICE, dto.getPending().toString());
        userEmailService.deleteToken(token);

        model.addAttribute("viewText", templateEnum.getViewText());
        return templateEnum.getView();
    }
    @PostMapping("/support/password")
    public String reset(@NotNull @RequestParam String email, Model model){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.RESET;

        model.addAttribute("viewText", templateEnum.getAsyncViewText());
        return templateEnum.getAsyncView();
    }
    @GetMapping("/reset/{token}")
    public String resetLink(@NotNull @PathVariable String token, Model model){
        model.addAttribute("token", token);
        return "reset";
    }
    @PostMapping("/reset")
    public String reset(@NotNull @RequestParam String password, @NotNull @RequestParam String token, Model model){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.RESET;

        model.addAttribute("viewText", templateEnum.getViewText());
        return templateEnum.getView();
    }
}
