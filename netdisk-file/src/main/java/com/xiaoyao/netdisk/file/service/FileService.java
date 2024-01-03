package com.xiaoyao.netdisk.file.service;

public interface FileService {
    void createFolder(String parentId, String folderName);

    void rename(String fileId, String name);
}
