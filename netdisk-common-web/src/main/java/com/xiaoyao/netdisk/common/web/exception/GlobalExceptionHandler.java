package com.xiaoyao.netdisk.common.web.exception;

import com.xiaoyao.netdisk.common.exception.E;
import com.xiaoyao.netdisk.common.exception.NetdiskException;
import com.xiaoyao.netdisk.common.exception.R;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理表单参数校验异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public R<Void> handleMethodArgumentNotValidException(Exception e) {
        String message = null;
        if (e instanceof MethodArgumentNotValidException exception) {
            FieldError fieldError = exception.getFieldError();
            message = Objects.requireNonNull(fieldError).getField() + "字段" + fieldError.getDefaultMessage();
        } else if (e instanceof ConstraintViolationException exception) {
            ConstraintViolation<?> violation = exception.getConstraintViolations().iterator().next();
            String path = violation.getPropertyPath().toString();
            message = path.substring(path.indexOf(".") + 1) + "字段" + violation.getMessage();
        }
        return R.error(E.INVALID_PARAMS.getCode(), message);
    }

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
