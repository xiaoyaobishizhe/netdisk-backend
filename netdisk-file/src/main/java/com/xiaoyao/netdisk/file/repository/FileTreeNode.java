package com.xiaoyao.netdisk.file.repository;

import lombok.Data;

import java.util.List;

@Data
public class FileTreeNode {
    private long id;
    private String path;
    private String name;
    private boolean isFolder;
    private List<FileTreeNode> children;
}
