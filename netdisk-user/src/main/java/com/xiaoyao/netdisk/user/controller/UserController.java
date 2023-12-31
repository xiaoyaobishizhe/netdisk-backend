package com.xiaoyao.netdisk.user.controller;

import com.xiaoyao.netdisk.common.exception.R;
import com.xiaoyao.netdisk.user.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public R<Void> register(String username, String password) {
        userService.register(username, password);
        return R.ok();
    }
}
