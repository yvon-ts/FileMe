package net.fileme.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.fileme.domain.dto.TokenDto;
import net.fileme.enums.EmailTemplateEnum;
import net.fileme.service.LoginService;
import net.fileme.service.UserEmailService;
import net.fileme.service.ValidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@Tag(name = "User")
@Controller
public class UserController {
    @Autowired
    private UserEmailService userEmailService;
    @Autowired
    private ValidateService validateService;
    @Autowired
    private LoginService loginService;

    @GetMapping("/support/sign-up/{token}")
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
    @GetMapping("/support/verify/{token}")
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
    @GetMapping("/support/reset/{token}")
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
