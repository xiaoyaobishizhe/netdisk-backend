package com.xiaoyao.netdisk.file.dto;

import lombok.Data;

import java.util.List;

@Data
public class FolderListDTO {
    private List<Item> folders;

    @Data
    public static class Item {
        private String id;
        private String name;
    }
}
