package com.xiaoyao.netdisk.file.controller;

import com.xiaoyao.netdisk.common.exception.R;
import com.xiaoyao.netdisk.file.dto.ListSharesDTO;
import com.xiaoyao.netdisk.file.service.ShareService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
