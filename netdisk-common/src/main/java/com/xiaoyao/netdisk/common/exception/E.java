package com.xiaoyao.netdisk.common.exception;

import lombok.Getter;

@Getter
public enum E {
    FAIL(1000, "操作失败"),
    USERNAME_HAS_EXIST(1001, "用户名已存在"),
    INVALID_PARAMS(1002, "参数校验失败"),
    USERNAME_OR_PASSWORD_ERROR(1003, "用户名或密码错误"),
    NO_LOGIN(1004, "未登录"),
    INVALID_TOKEN(1005, "无效的token"),
    TOKEN_EXPIRED(1006, "token已过期"),
    OLD_PASSWORD_ERROR(1007, "旧密码错误"),
    OLD_PASSWORD_SAME_AS_NEW_PASSWORD(1008, "旧密码与新密码相同"),
    FOLDER_ALREADY_EXIST(1009, "文件夹已存在"),
    FILE_NAME_INVALID(1010, "文件名称不合法"),
    FILE_NOT_EXIST(1011, "文件不存在"),
    FILE_NAME_ALREADY_EXIST(1012, "文件名称已存在"),
    NO_SHADING_TASK(1013, "没有可用的分片任务"),
    INVALID_SHADING_CHUNK(1014, "无效的分片序号"),
    SHADING_TASK_NOT_COMPLETE(1015, "分片任务未完成"),
    PARENT_FOLDER_NOT_EXIST(1016, "父文件夹不存在"),
    FILE_SIZE_TOO_SMALL_TO_SHADING(1017, "文件大小太小，无法进行分片上传"),
    SHARE_NOT_EXIST(1018, "分享不存在"),
    SHARE_TIMEOUT(1019, "分享已过期"),
    SHARE_PASSWORD_ERROR(1020, "分享提取码错误"),
    ;

    private final int code;
    private final String message;

    E(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
