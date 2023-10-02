package net.fileme.exception;

import net.fileme.enums.ExceptionEnum;

public class NotFoundException extends BizException{
    public NotFoundException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum);
    }
}
