package com.xiaoyao.netdisk.user.controller;

import com.xiaoyao.netdisk.common.exception.R;
import com.xiaoyao.netdisk.user.dto.UserInfoDTO;
import com.xiaoyao.netdisk.user.service.UserService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    public R<Void> register(@NotNull @Length(min = 6, max = 16) String username,
                            @NotNull @Length(min = 6, max = 16) String password) {
        userService.register(username, password);
        return R.ok();
    }

    @GetMapping
    public R<UserInfoDTO> info(@Pattern(regexp = "(^\\d{1,19}$)?") String userId) {
        return R.ok(userService.info(userId));
    }

    @PostMapping("/login")
    public R<String> login(@NotNull @Length(min = 6, max = 16) String username,
                           @NotNull @Length(min = 6, max = 16) String password) {
        return R.ok(userService.login(username, password));
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        userService.logout();
        return R.ok();
    }

    @PostMapping("/password")
    public R<Void> password(@NotNull @Length(min = 6, max = 16) String oldPassword,
                            @NotNull @Length(min = 6, max = 16) String newPassword) {
        userService.changePassword(oldPassword, newPassword);
        return R.ok();
    }
}
