package com.xiaoyao.netdisk.user.repository;

import com.xiaoyao.netdisk.user.entity.User;

public interface UserRepository {
    boolean isExistUsername(String username);

    void insert(User user);

    User findByUsername(String username);

    User findPasswordById(Long userId);

    void update(User user);

    User findInfoById(Long userId);
}
