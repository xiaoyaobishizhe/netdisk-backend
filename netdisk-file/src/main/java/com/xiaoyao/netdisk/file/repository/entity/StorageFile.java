package com.xiaoyao.netdisk.file.repository.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_storage_file")
public class StorageFile {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 文件唯一标识
     */
    private String identifier;

    /**
     * 在存储库中的路径
     */
    private String path;
}