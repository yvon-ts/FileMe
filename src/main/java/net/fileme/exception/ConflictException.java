package net.fileme.exception;

import net.fileme.enums.ExceptionEnum;

public class ConflictException extends BizException{
    public ConflictException(ExceptionEnum exceptionEnum){
        super(exceptionEnum);
    }
}
