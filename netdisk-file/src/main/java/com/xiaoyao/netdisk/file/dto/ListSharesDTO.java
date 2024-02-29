package com.xiaoyao.netdisk.file.dto;

import lombok.Data;

import java.util.List;

@Data
public class ListSharesDTO {
    private List<Item> items;

    @Data
    public static class Item {
        private String id;
        private String name;
        private String createTime;
        private Integer timeout;
    }
}
