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
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(BizException.class)
    public Result handleBizException(BizException bizException){
        return Result.error(bizException.getExceptionEnum());
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity handleBadRequest(BadRequestException badRequestException){
        logger.error(String.valueOf(badRequestException.getClass()));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(badRequestException.getExceptionEnum()));
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity handleNotFound(NotFoundException notFoundException){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Result.error(notFoundException.getExceptionEnum()));
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
        return Result.error(ExceptionEnum.PARAM_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleValidException(MethodArgumentNotValidException methodArgumentNotValidException){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(ExceptionEnum.PARAM_ERROR));
    }

    // e.g. update command without setting any values
    @ExceptionHandler(BadSqlGrammarException.class)
    public Result handleBadSql(BadSqlGrammarException badSqlGrammarException){
        return Result.error(ExceptionEnum.PARAM_ERROR);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result handleMissingParamException(){
        return Result.error(ExceptionEnum.PARAM_EMPTY);
    }
}
