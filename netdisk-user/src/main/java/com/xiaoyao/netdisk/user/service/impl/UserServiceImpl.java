package com.xiaoyao.netdisk.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xiaoyao.netdisk.common.exception.E;
import com.xiaoyao.netdisk.common.exception.NetdiskException;
import com.xiaoyao.netdisk.common.util.SecureUtil;
import com.xiaoyao.netdisk.common.web.interceptor.TokenInterceptor;
import com.xiaoyao.netdisk.common.web.util.JwtUtil;
import com.xiaoyao.netdisk.user.dto.UserInfoDTO;
import com.xiaoyao.netdisk.user.repository.entity.User;
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
        user.setNickname(username);
        user.setUsername(username);
        user.setPassword(secureUtil.sha256(password));
        user.setCreateTime(LocalDateTime.now());
        userRepository.insert(user);
    }

    @Override
    public String login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null || !user.getPassword().equals(secureUtil.sha256(password))) {
            throw new NetdiskException(E.USERNAME_OR_PASSWORD_ERROR);
        }
        return jwtUtil.createJwt(user.getId());
    }

    @Override
    public void logout() {
        jwtUtil.deleteRefreshToken(TokenInterceptor.USER_ID.get());
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        if (oldPassword.equals(newPassword)) {
            throw new NetdiskException(E.OLD_PASSWORD_SAME_AS_NEW_PASSWORD);
        }

        User user = userRepository.findPasswordById(TokenInterceptor.USER_ID.get());
        if (!user.getPassword().equals(secureUtil.sha256(oldPassword))) {
            throw new NetdiskException(E.OLD_PASSWORD_ERROR);
        }
        user.setId(TokenInterceptor.USER_ID.get());
        user.setPassword(secureUtil.sha256(newPassword));
        userRepository.update(user);
    }

    @Override
    public UserInfoDTO info(String userId) {
        User user = userRepository.findInfoById(
                StrUtil.isEmpty(userId) ? TokenInterceptor.USER_ID.get() : Long.parseLong(userId));
        UserInfoDTO dto = new UserInfoDTO();
        dto.setNickname(user.getNickname());
        return dto;
    }
}
