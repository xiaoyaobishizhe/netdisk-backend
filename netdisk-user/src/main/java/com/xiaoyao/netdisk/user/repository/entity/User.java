package com.xiaoyao.netdisk.user.repository.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_user")
public class User {
    @TableId
    private Long id;

    private String nickname;

    private String username;

    private String password;

    private LocalDateTime createTime;
}
