package com.xiaoyao.netdisk.file.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@TableName(value = "tb_share", autoResultMap = true)
@Data
public class Share {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 分享名称
     */
    private String name;

    /**
     * 分享码
     */
    private String code;

    /**
     * 提取码
     */
    private String password;

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 文件列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> fileList;

    /**
     * 过期天数，0为永久有效
     */
    private Integer timeout;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}