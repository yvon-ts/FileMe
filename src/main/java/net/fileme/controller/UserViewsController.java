package net.fileme.controller;

import net.fileme.domain.pojo.User;
import net.fileme.enums.EmailTemplateEnum;
import net.fileme.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@Controller
public class UserViewsController {

    @Autowired
    private UserService userService;

    @PostMapping("/sign-up")
    public String signUp(@NotNull @RequestBody User guest, Model model){
        // TODO: log密碼明文需另外處理
        User user = userService.createUser(guest);
        userService.prepareTokenEmail(EmailTemplateEnum.SIGN_UP, user.getEmail());
        model.addAttribute("viewText", EmailTemplateEnum.SIGN_UP.getAsyncViewText());
        return EmailTemplateEnum.SIGN_UP.getAsyncView();
    }
    @GetMapping("/verify/{token}")
    public String verifyToken(@NotNull @PathVariable String token, Model model){
        userService.processToken(token, true);
        model.addAttribute("viewText", EmailTemplateEnum.DEFAULT.getViewText());
        return EmailTemplateEnum.DEFAULT.getView();
    }
    @PostMapping("/user/setting/email")
    public String changeEmail(@NotNull @RequestParam String email, Model model){
        // TODO: 先放到pending
        userService.prepareTokenEmail(EmailTemplateEnum.SET_EMAIL, email);
        model.addAttribute("viewText", EmailTemplateEnum.SET_EMAIL.getAsyncViewText());
        return EmailTemplateEnum.SET_EMAIL.getAsyncView();
    }
    @PostMapping("/support/password")
    public String reset(@NotNull @RequestParam String email, Model model){
        // TODO: 先放到pending
        userService.prepareTokenEmail(EmailTemplateEnum.RESET, email);
        model.addAttribute("viewText", EmailTemplateEnum.RESET.getAsyncViewText());
        return EmailTemplateEnum.RESET.getAsyncView();
    }
    @GetMapping("/reset/{token}")
    public String resetLink(@NotNull @PathVariable String token, Model model){
        model.addAttribute("token", token);
        return "reset";
    }
    @PostMapping("/reset")
    public String reset(@NotNull @RequestParam String password, @NotNull @RequestParam String token, Model model){
        //確認token
        String email = userService.lookUpToken(token);
        // TODO: 把密碼加密
        // TODO: update pwd, user state = 0 綁交易
        model.addAttribute("viewText", EmailTemplateEnum.RESET.getViewText());
        return EmailTemplateEnum.RESET.getView();
    }
}
