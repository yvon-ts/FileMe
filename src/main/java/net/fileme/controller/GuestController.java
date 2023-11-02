package net.fileme.controller;

import net.fileme.domain.Result;
import net.fileme.service.ValidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@RestController
public class GuestController {
    @Autowired
    private ValidateService validateService;

    @PostMapping("/pub/valid/user")
    public Result validateUsername(@NotNull @RequestParam String username){
        validateService.checkUserName(username);
        return Result.success();
    }
    @PostMapping("/pub/valid/mail")
    public Result validateEmail(@NotNull @RequestParam String email){
        validateService.checkEmail(email);
        return Result.success();
    }
}
