package com.xiaoyao.netdisk.user.service;

import com.xiaoyao.netdisk.user.dto.LoginDTO;

public interface UserService {
    void register(String username, String password);

    LoginDTO login(String username, String password);

    void logout();
}
