package com.xiaoyao.netdisk.file.controller;

import com.xiaoyao.netdisk.common.exception.R;
import com.xiaoyao.netdisk.file.dto.ApplyUploadChunkDTO;
import com.xiaoyao.netdisk.file.dto.ShardingDTO;
import com.xiaoyao.netdisk.file.service.FileService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PutMapping("/name")
    public R<Void> rename(@Pattern(regexp = "^\\d{1,19}$") String id,
                          @NotNull @Length(min = 1, max = 250) String name) {
        fileService.rename(id, name);
        return R.ok();
    }

    @PutMapping("/sharding")
    public R<ShardingDTO> sharding(@NotBlank String identifier,
                                   @Pattern(regexp = "^\\d{1,19}$") String parentId,
                                   @NotNull @Length(min = 1, max = 250) String filename,
                                   @Pattern(regexp = "^\\d{1,19}$") String size) {
        return R.ok(fileService.createOrGetSharding(identifier, parentId, Long.parseLong(size), filename));
    }

    @PostMapping("/apply-upload-chunk")
    public R<ApplyUploadChunkDTO> applyUploadChunk(@NotBlank String identifier,
                                                   @NotNull @Min(1) String chunkNumber) {
        return R.ok(fileService.applyUploadChunk(identifier, Integer.parseInt(chunkNumber)));
    }

    @PostMapping("/upload-chunk")
    public R<Void> uploadChunk(@NotBlank String identifier,
                               @NotNull @Min(1) String chunkNumber) {
        fileService.uploadChunk(identifier, Integer.parseInt(chunkNumber));
        return R.ok();
    }

    @PostMapping("/finish-upload-chunk")
    public R<Void> finishUploadChunk(@NotBlank String identifier) {
        fileService.finishUploadChunk(identifier);
        return R.ok();
    }
}
