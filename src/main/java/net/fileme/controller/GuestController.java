package net.fileme.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.fileme.domain.Result;
import net.fileme.service.ValidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@Tag(name = "Guest API")
@RestController
public class GuestController {
    @Autowired
    private ValidateService validateService;

    @Operation(summary = "檢查帳號")
    @PostMapping("/pub/valid/user")
    public Result validateUsername(@NotNull @RequestParam String username){
        validateService.checkUserName(username);
        return Result.success();
    }
    @Operation(summary = "檢查信箱")
    @PostMapping("/pub/valid/mail")
    public Result validateEmail(@NotNull @RequestParam String email){
        validateService.checkEmail(email);
        return Result.success();
    }
}
