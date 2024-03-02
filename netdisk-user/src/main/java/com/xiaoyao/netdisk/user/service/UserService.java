package com.xiaoyao.netdisk.user.service;

import com.xiaoyao.netdisk.user.dto.UserInfoDTO;

public interface UserService {
    void register(String username, String password);

    String login(String username, String password);

    void logout();

    void changePassword(String oldPassword, String newPassword);

    /**
     * 查询用户信息，如果userId为null则查询当前登录用户的信息。
     *
     * @param userId 用户id
     * @return 用户信息
     */
    UserInfoDTO info(String userId);
}
