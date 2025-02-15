package com.sstdl.picturebackend.exception;

import com.sstdl.picturebackend.common.ErrorCode;
import lombok.Getter;

/**
 * @author SSTDL
 * @description 自定义异常类
 */
@Getter
public class BusinessException extends RuntimeException {
    /**
     * 错误码
     */
    private final int code;

    public BusinessException(final int code, final String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
