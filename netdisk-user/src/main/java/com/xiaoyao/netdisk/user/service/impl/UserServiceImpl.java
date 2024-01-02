package com.xiaoyao.netdisk.user.service.impl;

import com.xiaoyao.netdisk.common.exception.E;
import com.xiaoyao.netdisk.common.exception.NetdiskException;
import com.xiaoyao.netdisk.common.util.SecureUtil;
import com.xiaoyao.netdisk.common.web.interceptor.TokenInterceptor;
import com.xiaoyao.netdisk.common.web.util.JwtUtil;
import com.xiaoyao.netdisk.user.dto.LoginDTO;
import com.xiaoyao.netdisk.user.entity.User;
import com.xiaoyao.netdisk.user.repository.UserRepository;
import com.xiaoyao.netdisk.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final SecureUtil secureUtil;

    public UserServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, SecureUtil secureUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.secureUtil = secureUtil;
    }

    @Override
    public void register(String username, String password) {
        if (userRepository.isExistUsername(username)) {
            throw new NetdiskException(E.USERNAME_HAS_EXIST);
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(secureUtil.sha256(password));
        user.setCreateTime(LocalDateTime.now());
        userRepository.insert(user);
    }

    @Override
    public LoginDTO login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null || !user.getPassword().equals(secureUtil.sha256(password))) {
            throw new NetdiskException(E.USERNAME_OR_PASSWORD_ERROR);
        }

        LoginDTO dto = new LoginDTO();
        dto.setToken(jwtUtil.createJwt(user.getId()));
        dto.setUsername(user.getUsername());
        return dto;
    }

    @Override
    public void logout() {
        jwtUtil.deleteRefreshToken(TokenInterceptor.USER_ID.get());
    }
}
