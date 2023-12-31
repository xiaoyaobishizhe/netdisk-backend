package com.xiaoyao.netdisk.user.exception;

import com.xiaoyao.netdisk.common.exception.E;
import com.xiaoyao.netdisk.common.exception.NetdiskException;
import com.xiaoyao.netdisk.common.exception.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NetdiskException.class)
    public R<Void> handleNetdiskException(NetdiskException e) {
        return R.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler
    public R<Void> handleException(Exception e) {
        log.error(e.getMessage(), e);
        return R.error(E.FAIL.getCode(), E.FAIL.getMessage());
    }
}
