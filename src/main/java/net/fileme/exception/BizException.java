package net.fileme.exception;

import lombok.Getter;
import net.fileme.enums.ExceptionEnum;

/**
 * Business exception
 */
@Getter
public class BizException extends RuntimeException{
    private final ExceptionEnum exceptionEnum;

    public BizException(ExceptionEnum exceptionEnum){
        this.exceptionEnum = exceptionEnum;
    }

}
