package com.xiaoyao.netdisk.common.exception;

import lombok.Getter;

@Getter
public class R<T> {
    private int code;
    private String message;
    private T data;

    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.code = 200;
        r.message = "success";
        return r;
    }

    public static <T> R<T> ok(T t) {
        R<T> r = new R<>();
        r.code = 200;
        r.message = "success";
        r.data = t;
        return r;
    }

    public static <T> R<T> error(int code, String message) {
        R<T> r = new R<>();
        r.code = code;
        r.message = message;
        return r;
    }
}
