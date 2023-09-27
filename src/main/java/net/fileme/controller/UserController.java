package net.fileme.controller;

import net.fileme.domain.pojo.User;
import net.fileme.exception.InternalErrorException;
import net.fileme.service.UserService;
import net.fileme.utils.enums.ExceptionEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@RestController
public class UserController {

    @Autowired
    private UserService userService;
    @PostMapping("/user/login")
    public void login(){}
    // ----------------------------------Create---------------------------------- //
    @PostMapping("/user/register")
    public void register(@NotNull @RequestBody User guest){
        try{
            userService.register(guest);
        }catch(Exception e){
            throw new InternalErrorException(ExceptionEnum.REGISTER_FAIL);
        }
    }
}
