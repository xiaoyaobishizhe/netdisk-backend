package com.xiaoyao.netdisk.file.repository.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_user_file")
public class UserFile {
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
     * 所处路径
     */
    private String path;

    /**
     * 父文件夹主键值
     */
    private Long parentId;

    /**
     * 文件名
     */
    private String name;

    /**
     * 是否是文件夹
     */
    private Boolean isFolder;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 文件的唯一标识
     */
    private String identifier;

    /**
     * 存储文件主键值
     */
    private Long storageFileId;

    /**
     * 是否放到回收站
     */
    private Boolean isDeleted;

    /**
     * 删除时间
     */
    private LocalDateTime deleteTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}