package net.fileme.controller;

import net.fileme.domain.Result;
import net.fileme.domain.dto.UserDto;
import net.fileme.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;
    @PostMapping("/user/login")
    public Result login(@NotNull @RequestBody UserDto dto){
        return loginService.login(dto);
    }
}
