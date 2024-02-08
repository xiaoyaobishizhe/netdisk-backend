package com.xiaoyao.netdisk.file.dto;

import lombok.Data;

import java.util.List;

@Data
public class ListRecycleBinDTO {
    private List<Item> items;

    @Data
    public static class Item {
        private String id;
        private String name;
        private boolean isFolder;
        private String deleteTime;
    }
}
