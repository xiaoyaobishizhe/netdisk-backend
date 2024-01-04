package com.xiaoyao.netdisk.file.dto;

import lombok.Data;

@Data
public class ShardingDTO {
    private Integer chunkSize;

    private Integer currentChunk;

    private Integer totalChunk;
}
