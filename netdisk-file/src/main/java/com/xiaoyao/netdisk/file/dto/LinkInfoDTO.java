package com.xiaoyao.netdisk.file.dto;

import lombok.Data;

@Data
public class LinkInfoDTO {
    private String code;
    private String pwd;

    public LinkInfoDTO(String code, String pwd) {
        this.code = code;
        this.pwd = pwd;
    }
}
