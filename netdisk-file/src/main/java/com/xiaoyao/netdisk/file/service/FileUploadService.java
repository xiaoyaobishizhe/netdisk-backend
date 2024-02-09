package com.xiaoyao.netdisk.file.service;

import com.xiaoyao.netdisk.file.dto.ApplyUploadChunkDTO;
import com.xiaoyao.netdisk.file.dto.ShardingDTO;

public interface FileUploadService {
    /**
     * 创建一个分片上传任务并返回任务的相关信息，如果已存在分片任务则直接返回。
     *
     * @param identifier 文件标识
     * @param parentId   父文件夹id
     * @param size       文件大小
     * @param filename   文件名
     * @return 分片上传任务
     */
    ShardingDTO createOrGetSharding(String identifier, String parentId, long size, String filename);

    /**
     * 申请上传指定编号的分片。
     *
     * @param identifier  文件标识
     * @param chunkNumber 分片编号
     * @return 上传分片的相关信息
     */
    ApplyUploadChunkDTO applyUploadChunk(String identifier, int chunkNumber);

    /**
     * 上传完成指定编号的分片。
     *
     * @param identifier  文件标识
     * @param chunkNumber 分片编号
     */
    void uploadChunk(String identifier, int chunkNumber);

    /**
     * 完成分片上传任务。
     *
     * @param identifier 文件标识
     */
    void finishUploadChunk(String identifier);
}
