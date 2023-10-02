package net.fileme.controller;

import net.fileme.domain.pojo.User;
import net.fileme.exception.InternalErrorException;
import net.fileme.service.UserService;
import net.fileme.enums.ExceptionEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.internet.MimeMessage;
import javax.validation.constraints.NotNull;

@RestController
public class UserController {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private UserService userService;
    @PostMapping("/user/login")
    public void login(){}
    // ----------------------------------Create---------------------------------- //
    // see UserViewsController
}
