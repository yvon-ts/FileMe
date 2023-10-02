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
        User user = userService.createUser(guest);
        userService.verifyUser(user);
        return "sent";
    }

    @GetMapping("/verify/{token}")
    public String verifyLink(@PathVariable String token){
        // TODO: check token
        userService.setUserVerified();
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
