package com.xiaoyao.netdisk.file.repository.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "tb_sharding")
public class Sharding {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 用户主键值
     */
    private Long userId;

    /**
     * 文件本身的唯一标识
     */
    private String identifier;

    /**
     * 块的最大大小，单位为字节
     */
    private Integer chunkSize;

    /**
     * 当前已上传的块数
     */
    private Integer currentChunk;

    /**
     * 总的块数
     */
    private Integer totalChunk;
}