package com.xiaoyao.netdisk.file.service;

import com.xiaoyao.netdisk.file.dto.ShardingDTO;

import java.util.Map;

public interface FileService {
    void createFolder(String parentId, String folderName);

    void rename(String fileId, String name);

    ShardingDTO createOrGetSharding(String identifier, String parentId, long size, String filename);

    Map<String, String> applyUploadChunk(String identifier, int chunkNumber);

    void finishUploadChunk(String identifier);

    void uploadChunk(String identifier, int chunkNumber);
}
