package com.xiaoyao.netdisk.user.repository;

import com.xiaoyao.netdisk.user.entity.User;

public interface UserRepository {
    boolean isExistUsername(String username);

    void insert(User user);
}
