package net.fileme.handler;

import net.fileme.domain.Result;
import net.fileme.exception.BizException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    public Result handleBizException(BizException bizException){
        return Result.error(bizException.getExceptionEnum());
    }
}
