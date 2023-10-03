package net.fileme.controller;

import net.fileme.domain.pojo.User;
import net.fileme.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@Controller
public class UserViewsController {

    @Autowired
    private UserService userService;

    @PostMapping("/sign-up")
    public String signUp(@NotNull @RequestBody User guest){
        // TODO: log密碼明文需另外處理
        User user = userService.createUser(guest);
        userService.sendVerifyToken(user);
        return "sent";
    }

    @GetMapping("/verify/{token}")
    public String verifyToken(@NotNull @PathVariable String token){
        String email = userService.verifyToken(token);
        userService.setUserVerified(email);
        return "tmp";
    }

//    @GetMapping("/reset/{token}")
//    public String resetLink(@PathVariable String token, Model model){
//        model.addAttribute("token", token);
//        return "reset";
//    }
//    @PostMapping("/reset")
//    public String reset(@RequestParam("np") String password, @RequestParam String token){
//        System.out.println(password);
//        System.out.println(token);
//        return "success";
//    }
}
