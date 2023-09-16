package net.fileme.handler;

import net.fileme.domain.Result;
import net.fileme.exception.BizException;
import net.fileme.utils.enums.ExceptionEnum;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    public Result handleBizException(BizException bizException){
        return Result.error(bizException.getExceptionEnum());
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result handleMaxUploadSize(){
        return Result.error(ExceptionEnum.FILE_SIZE_ERROR);
    }
    // -------------------------- SQL -------------------------- //
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
    // -------------------------- parameter -------------------------- //
    // when param type is not correct
    @ExceptionHandler(TypeMismatchException.class)
    public Result handleTypeMismatchException(){
        return Result.error(ExceptionEnum.PARAM_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidException(){
        return Result.error(ExceptionEnum.PARAM_ERROR);
    }

    // e.g. update command without setting any values
    @ExceptionHandler(BadSqlGrammarException.class)
    public Result handleBadSql(){
        return Result.error(ExceptionEnum.PARAM_ERROR);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result handleMissingParamException(){
        return Result.error(ExceptionEnum.PARAM_EMPTY);
    }
}
