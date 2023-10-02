package net.fileme.exception;

import net.fileme.enums.ExceptionEnum;

public class InternalErrorException extends BizException{
    public InternalErrorException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum);
    }
}
