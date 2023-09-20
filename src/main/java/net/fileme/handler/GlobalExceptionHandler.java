package net.fileme.handler;

import net.fileme.domain.Result;
import net.fileme.exception.BadRequestException;
import net.fileme.exception.BizException;
import net.fileme.exception.InternalErrorException;
import net.fileme.exception.NotFoundException;
import net.fileme.utils.enums.ExceptionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(BizException.class)
    public Result handleBizException(BizException ex){
        log.error(ex.toString());
        return Result.error(ex.getExceptionEnum());
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity handleBadRequest(BadRequestException ex){
        logError(ex, ex.getExceptionEnum());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(ex.getExceptionEnum()));
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity handleNotFound(NotFoundException ex){
        logError(ex, ex.getExceptionEnum());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Result.error(ex.getExceptionEnum()));
    }
    @ExceptionHandler(InternalErrorException.class)
    public ResponseEntity handleInternalError(InternalErrorException internalErrorException){
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(internalErrorException.getExceptionEnum()));
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
    public Result handleTypeMismatchException(TypeMismatchException typeMismatchException){
        System.out.println("typemismatch");
        return Result.error(ExceptionEnum.PARAM_ERROR);
    }

    // when violate Spring Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleArgsNotValid(MethodArgumentNotValidException ex){
        //之後看看如何加上UserID，目前只有路由
        logError(ex, ExceptionEnum.PARAM_ERROR);

        if(ex.getBindingResult().hasErrors()){
            ex.getBindingResult().getFieldErrors()
                    .forEach(error -> {
                        log.debug("Invalid {} value submitted for {}",
                                error.getRejectedValue(), error.getField());
                        log.debug(error.toString());
                    });
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(ExceptionEnum.PARAM_ERROR));
    }

    // when missing RequestParam
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity handleMissingRequestParam(MissingServletRequestParameterException ex){
        //之後看看如何加上UserID，目前只有路由
        logError(ex, ExceptionEnum.PARAM_ERROR);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(ExceptionEnum.PARAM_ERROR));
    }

    // e.g. update command without setting any values
    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity handleBadSql(BadSqlGrammarException ex){
        logError(ex, ExceptionEnum.PARAM_ERROR);
        log.error(ex.getSQLException().toString());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(ExceptionEnum.PARAM_ERROR));
    }

    private void logError(Exception ex, ExceptionEnum exceptionEnum){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String uri = attributes.getRequest().getRequestURI();
        StringBuilder builder = new StringBuilder();

        builder.append(ex.getClass().getSimpleName()).append(": ").append(uri);
        log.error(builder.toString());

        log.error(exceptionEnum.toStringDetails());
    }
}
