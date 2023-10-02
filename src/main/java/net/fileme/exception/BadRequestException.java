package net.fileme.exception;

import net.fileme.enums.ExceptionEnum;
public class BadRequestException extends BizException{
    public BadRequestException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum);
    }
}
