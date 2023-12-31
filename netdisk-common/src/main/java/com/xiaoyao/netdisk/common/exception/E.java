package com.xiaoyao.netdisk.common.exception;

import lombok.Getter;

@Getter
public enum E {
    FAIL(1000, "操作失败"),
    USERNAME_HAS_EXIST(1001, "用户名已存在"),
    ;

    private final int code;
    private final String message;

    E(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
