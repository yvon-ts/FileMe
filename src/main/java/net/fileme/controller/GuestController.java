package net.fileme.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.fileme.domain.Result;
import net.fileme.service.ValidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@Tag(name = "Validate")
@RestController
public class GuestController {
    @Autowired
    private ValidateService validateService;

    @GetMapping("/pub/valid/user")
    @Operation(summary = "[Read] 檢查帳號", description = "[version 1.0]", responses = {
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "400", description = "Regex not matched", content = @Content),
            @ApiResponse(responseCode = "409", description = "Username has been used", content = @Content)})
    public Result validateUsername(@RequestParam @NotNull String username){
        validateService.checkUserName(username);
        return Result.success();
    }
    @GetMapping("/pub/valid/email")
    @Operation(summary = "[Read] 檢查信箱", description = "[version 1.0]", responses = {
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "400", description = "Regex not matched", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email has been used", content = @Content)})
    public Result validateEmail(@RequestParam @NotNull String email){
        validateService.checkEmail(email);
        return Result.success();
    }
}
