package net.fileme.exception;

import net.fileme.enums.ExceptionEnum;

public class UnauthorizedException extends BizException{
    public UnauthorizedException(ExceptionEnum exceptionEnum){
        super(exceptionEnum);
    }
}
