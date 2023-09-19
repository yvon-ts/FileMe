package net.fileme.exception;

import net.fileme.utils.enums.ExceptionEnum;

public class NotFoundException extends BizException{
    public NotFoundException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum);
    }
}
