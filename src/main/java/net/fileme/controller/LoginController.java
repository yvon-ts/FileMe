package net.fileme.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.fileme.domain.MyUserDetails;
import net.fileme.domain.Result;
import net.fileme.domain.dto.UserDto;
import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.UnauthorizedException;
import net.fileme.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Tag(name = "Login / Logout")
@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;
    @PostMapping("/user/login")
    @Operation(summary = "會員登入", description = "[version 1.0]")
    public Result login(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "使用者帳密", content = @Content(
            examples = {@ExampleObject(value = "{\"username\": \"username\", \"password\": \"password\"}")}))
                            @org.springframework.web.bind.annotation.RequestBody @NotNull UserDto dto){
        return loginService.login(dto);
    }
    @PostMapping("/user/logout")
    @Operation(summary = "會員登出", description = "[version 1.0]")
    public Result logout(@AuthenticationPrincipal MyUserDetails myUserDetails){
        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();

        return loginService.logout(userId);
    }
}
