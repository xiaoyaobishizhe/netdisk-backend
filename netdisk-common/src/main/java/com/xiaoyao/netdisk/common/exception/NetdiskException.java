package com.xiaoyao.netdisk.common.exception;

import lombok.Getter;

@Getter
public class NetdiskException extends RuntimeException {
    private final int code;

    public NetdiskException(E e) {
        super(e.getMessage());
        this.code = e.getCode();
    }
}
