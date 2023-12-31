package com.xiaoyao.netdisk.user.repository.impl;

import com.xiaoyao.netdisk.user.entity.User;
import com.xiaoyao.netdisk.user.repository.UserRepository;
import com.xiaoyao.netdisk.user.repository.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final UserMapper userMapper;

    public UserRepositoryImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public boolean isExistUsername(String username) {
        return userMapper.selectCount(lambdaQuery(User.class).eq(User::getUsername, username)) > 0;
    }

    @Override
    public void insert(User user) {
        userMapper.insert(user);
    }
}
