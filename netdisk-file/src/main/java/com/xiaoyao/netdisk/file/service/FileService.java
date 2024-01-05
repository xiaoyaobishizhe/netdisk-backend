package com.xiaoyao.netdisk.file.service;

import com.xiaoyao.netdisk.file.dto.ShardingDTO;

import java.util.Map;

public interface FileService {
    void createFolder(String parentId, String folderName);

    void rename(String fileId, String name);

    ShardingDTO createOrGetSharding(String identifier, String filename, long totalSize);

    Map<String, String> applyUploadChunk(String identifier, int i);
}
