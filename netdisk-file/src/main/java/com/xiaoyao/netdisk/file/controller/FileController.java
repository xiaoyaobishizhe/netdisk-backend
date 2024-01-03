package com.xiaoyao.netdisk.file.controller;

import com.xiaoyao.netdisk.common.exception.R;
import com.xiaoyao.netdisk.file.service.FileService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/file")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PutMapping("/folder")
    public R<Void> createFolder(@Pattern(regexp = "^\\d{1,19}$") String parentId,
                                @NotNull @Length(min = 1, max = 250) String folderName) {
        fileService.createFolder(parentId, folderName);
        return R.ok();
    }
}
