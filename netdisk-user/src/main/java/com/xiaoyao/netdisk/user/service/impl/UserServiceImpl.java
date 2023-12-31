package com.xiaoyao.netdisk.user.service.impl;

import com.xiaoyao.netdisk.common.exception.E;
import com.xiaoyao.netdisk.common.exception.NetdiskException;
import com.xiaoyao.netdisk.user.entity.User;
import com.xiaoyao.netdisk.user.repository.UserRepository;
import com.xiaoyao.netdisk.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void register(String username, String password) {
        if (userRepository.isExistUsername(username)) {
            throw new NetdiskException(E.USERNAME_HAS_EXIST);
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setCreateTime(LocalDateTime.now());
        userRepository.insert(user);
    }
}
