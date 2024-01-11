package com.xiaoyao.netdisk.file.dto;

import lombok.Data;

import java.util.List;

@Data
public class FileListDTO {
    private List<Item> files;

    @Data
    public static class Item {
        private String id;
        private String name;
        private boolean isFolder;
        private Long size;
        private String updateTime;
    }
}
