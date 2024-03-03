package com.xiaoyao.netdisk.file.controller;

import com.xiaoyao.netdisk.common.exception.R;
import com.xiaoyao.netdisk.file.dto.FileListDTO;
import com.xiaoyao.netdisk.file.dto.ListSharesDTO;
import com.xiaoyao.netdisk.file.service.ShareService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@Validated
@RestController
@RequestMapping("/share")
public class ShareController {
    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @GetMapping("/list-share")
    public R<ListSharesDTO> listShares() {
        return R.ok(shareService.listShares());
    }

    @PostMapping("/create")
    public R<String> create(@NotNull @Length(min = 1, max = 250) String name,
                            @Pattern(regexp = "(^[0-9a-z]{4}$)?") String password,
                            @NotNull Integer timeout,
                            @Size(min = 1) String[] ids) {
        return R.ok(shareService.createShare(name, password, timeout, Arrays.stream(ids).map(Long::parseLong).toList()));
    }

    @PostMapping("/delete")
    public R<Void> delete(@Size(min = 1) String[] ids) {
        shareService.deleteShare(Arrays.stream(ids).toList());
        return R.ok();
    }

    @GetMapping("/link")
    public R<String> link(@NotNull @Pattern(regexp = "^\\d{1,19}$") String id) {
        return R.ok(shareService.getShareLink(id));
    }

    @GetMapping("/user-id")
    public R<Long> userId(@NotNull @Length(min = 30, max = 30) String code) {
        return R.ok(shareService.getUserId(code));
    }

    @PostMapping("/access-token")
    public R<String> accessToken(@NotNull @Length(min = 30, max = 30) String code,
                                 @NotNull @Pattern(regexp = "(^[0-9a-z]{4}$)?") String password) {
        return R.ok(shareService.getAccessToken(code, password));
    }

    @GetMapping("/list")
    public R<FileListDTO> list(@NotNull @Length(min = 100, max = 100) String token,
                               @Pattern(regexp = "^\\d{1,19}$") String parentId) {
        return R.ok(shareService.list(token, parentId));
    }

    @PostMapping("/save")
    public R<Void> save(
            @NotNull @Length(min = 100, max = 100) String token,
            @Size(min = 1) String[] ids,
            @Pattern(regexp = "(^\\d{1,19}$)?") String parentId) {
        shareService.save(token, Arrays.stream(ids).map(Long::parseLong).toList(), parentId);
        return R.ok();
    }
}
