package com.xiaoyao.netdisk.user.service.impl;

import com.xiaoyao.netdisk.common.exception.E;
import com.xiaoyao.netdisk.common.exception.NetdiskException;
import com.xiaoyao.netdisk.common.util.SecureUtil;
import com.xiaoyao.netdisk.user.dto.LoginDTO;
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
        user.setPassword(SecureUtil.sha256(password));
        user.setCreateTime(LocalDateTime.now());
        userRepository.insert(user);
    }

    @Override
    public LoginDTO login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null || !user.getPassword().equals(SecureUtil.sha256(password))) {
            throw new NetdiskException(E.USERNAME_OR_PASSWORD_ERROR);
        }

        LoginDTO dto = new LoginDTO();
        dto.setToken(SecureUtil.createJwt(user.getId()));
        dto.setUsername(user.getUsername());
        return dto;
    }

    @Override
    public void logout() {

    }
}
