package com.xiaoyao.netdisk.user.controller;

import com.xiaoyao.netdisk.common.exception.R;
import com.xiaoyao.netdisk.user.dto.LoginDTO;
import com.xiaoyao.netdisk.user.service.UserService;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public R<Void> register(@NotNull @Length(min = 1, max = 16) String username,
                            @NotNull @Length(min = 1, max = 16) String password) {
        userService.register(username, password);
        return R.ok();
    }

    @PostMapping("/login")
    public R<LoginDTO> login(@NotNull @Length(min = 1, max = 16) String username,
                             @NotNull @Length(min = 1, max = 16) String password) {
        return R.ok(userService.login(username, password));
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        userService.logout();
        return R.ok();
    }
}
