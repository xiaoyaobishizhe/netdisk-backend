package com.xiaoyao.netdisk.file.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ApplyUploadChunkDTO {
    private String key;
    private Map<String, String> formData;
    private String uploadUrl;
}
