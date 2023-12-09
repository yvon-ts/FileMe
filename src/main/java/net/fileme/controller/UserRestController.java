package net.fileme.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.fileme.domain.MyUserDetails;
import net.fileme.domain.Result;
import net.fileme.domain.dto.UpdatePwdDto;
import net.fileme.domain.dto.UserDto;
import net.fileme.enums.EmailTemplateEnum;
import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.ConflictException;
import net.fileme.exception.UnauthorizedException;
import net.fileme.service.LoginService;
import net.fileme.service.UserEmailService;
import net.fileme.service.ValidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Tag(name = "User")
@RestController
public class UserRestController {
    @Autowired
    private UserEmailService userEmailService;
    @Autowired
    private ValidateService validateService;
    @Autowired
    private LoginService loginService;

    // ----------------------------Sign Up----------------------------- //
    @PostMapping("/support/sign-up")
    @Operation(summary = "[Create] 使用者註冊", description = "[version 1.0] <br><ul><li>帳號不得包含下列單詞：admin, fileme</li><li>以非同步方式寄出註冊驗證信</li></ul>", responses = {
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "400", description = "Regex not matched", content = @Content),
            @ApiResponse(responseCode = "409", description = "Username or Email has been used", content = @Content)})
    public Result signUp(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "使用者帳密及信箱")
                         @org.springframework.web.bind.annotation.RequestBody
                         @Validated(UserDto.Create.class) @NotNull UserDto guest){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.SIGN_UP;
        validateService.checkUserName(guest.getUsername());
        validateService.checkEmail(guest.getEmail());

        userEmailService.createUser(guest);
        userEmailService.sendTokenEmail(templateEnum, guest.getEmail(), null);

        return Result.success();
    }

    // ----------------------------Change Pwd----------------------------- //
    @PostMapping("/user/setting/password")
    @Operation(summary = "[Update] 變更密碼", description = "[version 1.0]<br><ul><li>登入狀態操作，變更後會強制登出</li><li>以非同步方式寄出變更通知信</li></ul>", responses = {
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "400", description = "Regex not matched", content = @Content),
            @ApiResponse(responseCode = "401", description = "Current password not matched or Guest not allowed", content = @Content)})
    public Result changePwd(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "使用者新舊密碼")
                            @org.springframework.web.bind.annotation.RequestBody @NotNull UpdatePwdDto dto,
                            @AuthenticationPrincipal MyUserDetails myUserDetails){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.SET_PWD;

        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();

        Integer state = myUserDetails.getUser().getState();
        if(state != 0) throw new ConflictException(ExceptionEnum.USER_STATE_ERROR);

        String currPassword = myUserDetails.getPassword();
        String email = new String(myUserDetails.getUser().getEmail()); // for async method after log out

        userEmailService.matchCurrentPwd(dto.getOldPassword(), currPassword);

        userEmailService.processChangePwd(userId, dto.getNewPassword());
        userEmailService.createBasicEmail(EmailTemplateEnum.RESET_NOTICE, email);

        loginService.logout();

        return Result.success();
    }
    // ----------------------------Change Email----------------------------- //
    @PostMapping("/user/setting/email")
    @Operation(summary = "[Update] 變更信箱", description = "[version 1.0]<br><ul><li>登入狀態操作，變更後會強制登出</li><li>以非同步方式寄出變更信箱驗證信</li></ul>", responses = {
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "400", description = "Regex not matched", content = @Content),
            @ApiResponse(responseCode = "401", description = "Guest not allowed", content = @Content)})
    public Result changeEmail(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "新信箱", content = @Content(schema = @Schema(
                                      example = "{\"email\": \"example@email.com\"}")))
                              @org.springframework.web.bind.annotation.RequestBody
                              @Validated(UserDto.CheckEmail.class) @NotNull UserDto dto,
                              @AuthenticationPrincipal MyUserDetails myUserDetails) {
        EmailTemplateEnum templateEnum = EmailTemplateEnum.SET_EMAIL;
        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        String oldEmail = new String(myUserDetails.getUser().getEmail()); // for async method after log out

        validateService.checkEmail(dto.getEmail());
        userEmailService.setUserState(templateEnum, oldEmail);
        userEmailService.sendTokenEmailForEmailChange(templateEnum, oldEmail, dto.getEmail());

        loginService.logout();

        return Result.success();
    }
    // ----------------------------Reset Password----------------------------- //
    @PostMapping("/support/password")
    @Operation(summary = "[Update] 忘記密碼", description = "[version 1.0] <br> 以非同步方式寄出變更信箱驗證信", responses = {
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "400", description = "param error", content = @Content),
            @ApiResponse(responseCode = "500", description = "user not found or user state error (code: 23114)", content = @Content)
    })
    public Result reset(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "會員信箱", content = @Content(schema = @Schema(
                            example = "{\"email\": \"example@email.com\"}")))
                            @org.springframework.web.bind.annotation.RequestBody
                            @Validated(UserDto.CheckEmail.class) @NotNull UserDto dto){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.RESET;

        userEmailService.setUserState(templateEnum, dto.getEmail());
        userEmailService.sendTokenEmail(templateEnum, dto.getEmail(), null);

        return Result.success();
    }
    @PostMapping("/support/hi")
    public String hi(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "會員信箱", content = @Content(schema = @Schema(
            example = "{\"email\": \"example@email.com\"}")))
                         @org.springframework.web.bind.annotation.RequestBody
                         @Validated(UserDto.CheckEmail.class) @NotNull UserDto dto){
        System.out.println(dto.getEmail());
        return "hello";
    }
//    @PostMapping("/support/reset")
//    @Operation(summary = "[Update] 密碼重置", description = "[version 1.0] <br> 以非同步方式寄出變更通知信", responses = {
//            @ApiResponse(responseCode = "200", content = @Content),
//            @ApiResponse(responseCode = "400", description = "Regex not matched", content = @Content),
//            @ApiResponse(responseCode = "401", description = "No such token", content = @Content),
//            @ApiResponse(responseCode = "409", description = "Token version conflict", content = @Content)})
//    public String reset(@NotNull @RequestParam String password, @NotNull @RequestParam String token, Model model){
//        EmailTemplateEnum templateEnum = EmailTemplateEnum.RESET;
//
//        validateService.regexPwd(password); // workaround: @Pattern validation not working
//        TokenDto dto = userEmailService.processResetPassword(password, token);
//        String email = new String(dto.getReqEmail()); // for async method after deleting token
//        userEmailService.createBasicEmail(EmailTemplateEnum.RESET_NOTICE, email);
//        userEmailService.deleteToken(token);
//
//        model.addAttribute("viewText", templateEnum.getViewText());
//        return templateEnum.getView();
//    }
}
