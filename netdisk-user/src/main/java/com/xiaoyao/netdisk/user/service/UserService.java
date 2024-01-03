package com.xiaoyao.netdisk.user.service;

import com.xiaoyao.netdisk.user.dto.UserInfoDTO;

public interface UserService {
    void register(String username, String password);

    String login(String username, String password);

    void logout();

    void changePassword(String oldPassword, String newPassword);

    UserInfoDTO info();
}
