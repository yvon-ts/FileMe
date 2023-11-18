package net.fileme.handler;

import net.fileme.domain.Result;
import net.fileme.exception.*;
import net.fileme.enums.ExceptionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private void logError(Exception ex, ExceptionEnum exceptionEnum){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String uri = attributes.getRequest().getRequestURI();
        StringBuilder builder = new StringBuilder();

        builder.append(ex.getClass().getSimpleName()).append(": ").append(uri);
        log.error(builder.toString());

        log.error(exceptionEnum.toStringDetails());
    }
    // -------------------------- Customized Exceptions -------------------------- //
    @ExceptionHandler(BizException.class)
    public ResponseEntity handleBizException(BizException ex){
        logError(ex, ex.getExceptionEnum());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ex.getExceptionEnum()));
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity handleBadRequest(BadRequestException ex){
        logError(ex, ex.getExceptionEnum());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(ex.getExceptionEnum()));
    }
    @ExceptionHandler(UnauthorizedException.class)
    public ModelAndView handleUnauthorized(UnauthorizedException ex){
        logError(ex, ex.getExceptionEnum());
        ModelAndView view = new ModelAndView("error");
        view.addObject("errMsg", ex.getExceptionEnum().getDesc());
        return view;
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity handleNotFound(NotFoundException ex){
        logError(ex, ex.getExceptionEnum());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Result.error(ex.getExceptionEnum()));
    }
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity handleConflict(ConflictException ex){
        logError(ex, ex.getExceptionEnum());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Result.error(ex.getExceptionEnum()));
    }
    @ExceptionHandler(InternalErrorException.class)
    public ResponseEntity handleInternalError(InternalErrorException ex){
        logError(ex, ex.getExceptionEnum());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ex.getExceptionEnum()));
    }
    // -------------------------- Spring Exceptions -------------------------- //
    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity handleInternalAuthException(InternalAuthenticationServiceException ex){
        logError(ex, ExceptionEnum.LOGIN_ERROR);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(ExceptionEnum.LOGIN_ERROR));
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity handleFailedLogin(BadCredentialsException ex){
        logError(ex, ExceptionEnum.LOGIN_ERROR);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(ExceptionEnum.LOGIN_ERROR));
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity handleAuthentication(AuthenticationException ex){
        logError(ex, ExceptionEnum.GUEST_NOT_ALLOWED);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(ExceptionEnum.GUEST_NOT_ALLOWED));
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex){
        logError(ex, ExceptionEnum.METHOD_NOT_ALLOWED);
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Result.error(ExceptionEnum.METHOD_NOT_ALLOWED));
    }
    @ExceptionHandler(SpelEvaluationException.class)
    public ModelAndView handleSpelEvaluation(SpelEvaluationException ex){
        return handleUnauthorized(new UnauthorizedException(ExceptionEnum.PRE_AUTH_FAIL));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity handleHttpMsgNotReadable(HttpMessageNotReadableException ex){
        return handleBadRequest(new BadRequestException(ExceptionEnum.PARAM_ERROR));
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity handleMaxUploadSize(MaxUploadSizeExceededException ex){
        logError(ex, ExceptionEnum.FILE_OVER_SIZE);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ExceptionEnum.FILE_OVER_SIZE));
    }
    // when violate sql unique constraint
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity handleDuplicateKey(DuplicateKeyException ex){
        logError(ex, ExceptionEnum.UNIQUE_CONFLICT);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Result.error(ExceptionEnum.UNIQUE_CONFLICT));
    }

    // when violate sql foreign key constraint
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity handleIntegrityViolation(DataIntegrityViolationException ex){
        logError(ex, ExceptionEnum.VIOLATE_KEY);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Result.error(ExceptionEnum.VIOLATE_KEY));
    }
    // when param type is not correct
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity handleTypeMismatch(TypeMismatchException ex){
        logError(ex, ExceptionEnum.PARAM_ERROR);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(ExceptionEnum.PARAM_ERROR));
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
                        // TODO: 避免pwd明碼顯示於log須移除getRejectedValue
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
}
