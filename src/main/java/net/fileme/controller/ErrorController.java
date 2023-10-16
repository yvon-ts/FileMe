package net.fileme.controller;

import net.fileme.domain.Result;
import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.UnauthorizedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ErrorController {

    // TODO: 錯誤訊息需再調整
    @GetMapping("/error")
    public Result error(HttpServletRequest request){
        throw new UnauthorizedException(ExceptionEnum.EXPIRED_TOKEN);
    }
    @PostMapping("/error")
    public Result errorPost(HttpServletRequest request){
        throw new UnauthorizedException(ExceptionEnum.EXPIRED_TOKEN);
    }
}
