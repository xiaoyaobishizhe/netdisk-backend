package com.xiaoyao.netdisk.file.controller;

import com.xiaoyao.netdisk.common.exception.R;
import com.xiaoyao.netdisk.file.dto.ListRecycleBinDTO;
import com.xiaoyao.netdisk.file.service.RecycleBinService;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@Validated
@RestController
@RequestMapping("/file/recycle-bin")
public class RecycleBinController {
    private final RecycleBinService recycleBinService;

    public RecycleBinController(RecycleBinService recycleBinService) {
        this.recycleBinService = recycleBinService;
    }

    @GetMapping("/list")
    public R<ListRecycleBinDTO> list() {
        return R.ok(recycleBinService.list());
    }

    @PostMapping("/restore")
    public R<Void> restore(@Size(min = 1) String[] ids) {
        recycleBinService.restore(Arrays.stream(ids).toList());
        return R.ok();
    }

    @PostMapping("/remove")
    public R<Void> remove(@Size(min = 1) String[] ids) {
        recycleBinService.remove(Arrays.stream(ids).toList());
        return R.ok();
    }

    @PostMapping("/clear")
    public R<Void> clear() {
        recycleBinService.clear();
        return R.ok();
    }
}
