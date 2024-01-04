package com.xiaoyao.netdisk.file.service;

import com.xiaoyao.netdisk.file.dto.ShardingDTO;

public interface FileService {
    void createFolder(String parentId, String folderName);

    void rename(String fileId, String name);

    ShardingDTO createOrGetSharding(String identifier, long totalSize);
}
