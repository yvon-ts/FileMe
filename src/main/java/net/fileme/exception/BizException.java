package net.fileme.exception;

import lombok.Getter;
import net.fileme.utils.enums.ExceptionEnum;

/**
 * Business exception
 */
@Getter
public class BizException extends RuntimeException{
    private final ExceptionEnum exceptionEnum;

    public BizException(ExceptionEnum exceptionEnum){
        this.exceptionEnum = exceptionEnum;
    }

    /**
     * transform 3rd party exception into customized enum
     * @param exceptionEnum
     * @param cause
     */
    public BizException(ExceptionEnum exceptionEnum, Throwable cause){
        super(cause);
        this.exceptionEnum = exceptionEnum;
    }

}
