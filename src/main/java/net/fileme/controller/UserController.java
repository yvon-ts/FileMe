package net.fileme.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.fileme.domain.MyUserDetails;
import net.fileme.domain.dto.TokenDto;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Tag(name = "User")
@Controller
public class UserController {
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
    public String signUp(Model model,
                         @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "使用者帳密及信箱")
                             @org.springframework.web.bind.annotation.RequestBody
                         @Validated(UserDto.Create.class) @NotNull UserDto guest){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.SIGN_UP;
        validateService.checkUserName(guest.getUsername());
        validateService.checkEmail(guest.getEmail());

        userEmailService.createUser(guest);
        userEmailService.sendTokenEmail(templateEnum, guest.getEmail(), null);
        model.addAttribute("viewText", templateEnum.getAsyncViewText());
        return templateEnum.getAsyncView();
    }

    @PostMapping("/support/sign-up/{token}")
    @Operation(summary = "[Update] 驗證：使用者註冊", description = "[version 1.0]", responses = {
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "401", description = "No such token", content = @Content),
            @ApiResponse(responseCode = "409", description = "Token version conflict", content = @Content)})
    public String signUpToken(Model model,
                              @Parameter(description = "隨機32位數大小寫英數字金鑰")
                              @PathVariable @NotNull String token){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.SIGN_UP;

        userEmailService.processSignUp(token);
        userEmailService.deleteToken(token);

        model.addAttribute("viewText", templateEnum.getViewText());
        return templateEnum.getView();
    }
    // ----------------------------Change Pwd----------------------------- //
    @PostMapping("/user/setting/password")
    @Operation(summary = "[Update] 變更密碼", description = "[version 1.0]<br><ul><li>登入狀態操作，變更後會強制登出</li><li>以非同步方式寄出變更通知信</li></ul>", responses = {
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "400", description = "Regex not matched", content = @Content),
            @ApiResponse(responseCode = "401", description = "Current password not matched or Guest not allowed", content = @Content)})
    public String changePwd(Model model,
                            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "使用者帳密及信箱")
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
//        validateService.regexPwd(dto.getNewPassword()); // workaround: @Pattern validation not working //TODO: 可刪
        userEmailService.processChangePwd(userId, dto.getNewPassword());
        userEmailService.createBasicEmail(EmailTemplateEnum.RESET_NOTICE, email);

        loginService.logout();

        model.addAttribute("viewText", templateEnum.getAsyncViewText());
        return templateEnum.getAsyncView();
    }
    // ----------------------------Change Email----------------------------- //
    @PostMapping("/user/setting/email")
    @Operation(summary = "[Update] 變更信箱", description = "[version 1.0]<br><ul><li>登入狀態操作，變更後會強制登出</li><li>以非同步方式寄出變更信箱驗證信</li></ul>", responses = {
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "400", description = "Regex not matched", content = @Content),
            @ApiResponse(responseCode = "401", description = "Guest not allowed", content = @Content)})
    public String changeEmail(Model model,
                              @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "新信箱",
                                      content = @Content(schema = @Schema(example = "example@email.com")))
                              @org.springframework.web.bind.annotation.RequestBody @NotNull String newEmail,
                              @AuthenticationPrincipal MyUserDetails myUserDetails) {
        EmailTemplateEnum templateEnum = EmailTemplateEnum.SET_EMAIL;
        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        String oldEmail = new String(myUserDetails.getUser().getEmail()); // for async method after log out

        validateService.checkEmail(newEmail);
        userEmailService.setUserState(templateEnum, oldEmail);
        userEmailService.sendTokenEmailForEmailChange(templateEnum, oldEmail, newEmail);

        loginService.logout();

        model.addAttribute("viewText", templateEnum.getAsyncViewText());
        return templateEnum.getAsyncView();
    }
    @PostMapping("/support/verify/{token}")
    @Operation(summary = "[Update] 驗證：變更信箱", description = "[version 1.0]", responses = {
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "401", description = "No such token", content = @Content),
            @ApiResponse(responseCode = "409", description = "Token version conflict", content = @Content)})
    public String verifyToken(Model model,
                              @Parameter(description = "隨機32位數大小寫英數字金鑰")
                              @PathVariable @NotNull String token){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.DEFAULT;

        TokenDto dto = userEmailService.processChangeEmail(token);
        String email = new String(dto.getReqEmail()); // for async method after deleting token
        userEmailService.createBasicEmail(EmailTemplateEnum.SET_EMAIL_NOTICE, email);
        userEmailService.deleteToken(token);

        model.addAttribute("viewText", templateEnum.getViewText());
        return templateEnum.getView();
    }
    // ----------------------------Reset Password----------------------------- //
    @PostMapping("/support/password")
    @Operation(summary = "[Update] 忘記密碼", description = "[version 1.0] <br> 以非同步方式寄出變更信箱驗證信")
    public String reset(Model model,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "信箱",
                                content = @Content(schema = @Schema(example = "example@email.com")))
                        @org.springframework.web.bind.annotation.RequestBody @NotNull String email){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.RESET;

        validateService.regexEmail(email); // workaround: @Email validation not working
        userEmailService.setUserState(templateEnum, email);
        userEmailService.sendTokenEmail(templateEnum, email, null);

        model.addAttribute("viewText", templateEnum.getAsyncViewText());
        return templateEnum.getAsyncView();
    }
    @PostMapping("/support/reset/{token}")
    @Operation(summary = "[Update] 驗證：忘記密碼", description = "[version 1.0]", responses = {
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "401", description = "No such token", content = @Content),
            @ApiResponse(responseCode = "409", description = "Token version conflict", content = @Content)})
    public String resetLink(Model model,
                            @Parameter(description = "隨機32位數大小寫英數字金鑰")
                            @PathVariable @NotNull String token){
        userEmailService.lookUpToken(token);
        model.addAttribute("token", token);
        return "reset";
    }
    @PostMapping("/support/reset")
    @Operation(summary = "[Update] 密碼重置", description = "[version 1.0] <br> 以非同步方式寄出變更通知信", responses = {
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "400", description = "Regex not matched", content = @Content),
            @ApiResponse(responseCode = "401", description = "No such token", content = @Content),
            @ApiResponse(responseCode = "409", description = "Token version conflict", content = @Content)})
    public String reset(@NotNull @RequestParam String password, @NotNull @RequestParam String token, Model model){
        EmailTemplateEnum templateEnum = EmailTemplateEnum.RESET;

        validateService.regexPwd(password); // workaround: @Pattern validation not working
        TokenDto dto = userEmailService.processResetPassword(password, token);
        String email = new String(dto.getReqEmail()); // for async method after deleting token
        userEmailService.createBasicEmail(EmailTemplateEnum.RESET_NOTICE, email);
        userEmailService.deleteToken(token);

        model.addAttribute("viewText", templateEnum.getViewText());
        return templateEnum.getView();
    }
}
