package net.fileme.handler;

import net.fileme.domain.Result;
import net.fileme.exception.BizException;
import net.fileme.exception.ExceptionEnum;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    public Result handleBizException(BizException bizException){
        return Result.error(bizException.getExceptionEnum());
    }

    // when violate sql unique constraint
    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleDuplicateException(){
        return Result.error(ExceptionEnum.DUPLICATED_DB);
    }

    // when violate sql foreign key constraint
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result handleIntegrityException(){
        return Result.error(ExceptionEnum.VIOLATE_KEY);
    }

    // when param type is not correct
    @ExceptionHandler(TypeMismatchException.class)
    public Result handleTypeMismatchException(){
        return Result.error(ExceptionEnum.PARAM_ERROR);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result handleMissingParamException(){
        return Result.error(ExceptionEnum.PARAM_EMPTY);
    }
}
