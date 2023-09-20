package net.fileme.exception;

import net.fileme.utils.enums.ExceptionEnum;
public class BadRequestException extends BizException{
    public BadRequestException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum);
    }
}
