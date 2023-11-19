package net.fileme.controller;

import net.fileme.domain.Result;
import net.fileme.enums.ExceptionEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ExceptionController {
    @RequestMapping("/token-sign-error")
    public ResponseEntity tokenSignError(HttpServletRequest request){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(ExceptionEnum.TOKEN_SIGNATURE_ERROR));
    }
    @RequestMapping("/token-expired")
    public ResponseEntity tokenExpired(HttpServletRequest request){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(ExceptionEnum.EXPIRED_TOKEN));
    }
    @RequestMapping("/guest-not-allowed")
    public ResponseEntity guestNotAllowed(HttpServletRequest request){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(ExceptionEnum.GUEST_NOT_ALLOWED));
    }
}
