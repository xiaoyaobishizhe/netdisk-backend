package com.xiaoyao.netdisk.file.controller;

import com.xiaoyao.netdisk.common.exception.R;
import com.xiaoyao.netdisk.file.dto.ApplyUploadChunkDTO;
import com.xiaoyao.netdisk.file.dto.FileListDTO;
import com.xiaoyao.netdisk.file.dto.FolderListDTO;
import com.xiaoyao.netdisk.file.dto.ShardingDTO;
import com.xiaoyao.netdisk.file.service.FileUploadService;
import com.xiaoyao.netdisk.file.service.UserFileService;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Validated
@RestController
@RequestMapping("/file")
public class FileController {
    private final UserFileService userFileService;
    private final FileUploadService fileUploadService;

    public FileController(UserFileService userFileService, FileUploadService fileUploadService) {
        this.userFileService = userFileService;
        this.fileUploadService = fileUploadService;
    }

    @PutMapping("/folder")
    public R<Void> createFolder(@Pattern(regexp = "(^\\d{1,19}$)?") String parentId,
                                @NotNull @Length(min = 1, max = 250) String folderName) {
        userFileService.createFolder(parentId, folderName);
        return R.ok();
    }

    @PutMapping("/name")
    public R<Void> rename(@Pattern(regexp = "(^\\d{1,19}$)?") String id,
                          @NotNull @Length(min = 1, max = 250) String name) {
        userFileService.rename(id, name);
        return R.ok();
    }

    @PutMapping("/sharding")
    public R<ShardingDTO> sharding(@NotBlank String identifier,
                                   @Pattern(regexp = "(^\\d{1,19}$)?") String parentId,
                                   @NotNull @Length(min = 1, max = 250) String filename,
                                   @Pattern(regexp = "^\\d{1,19}$") String size) {
        return R.ok(fileUploadService.createOrGetSharding(identifier, parentId, Long.parseLong(size), filename));
    }

    @PostMapping("/apply-upload-chunk")
    public R<ApplyUploadChunkDTO> applyUploadChunk(@NotBlank String identifier,
                                                   @NotNull @Min(1) String chunkNumber) {
        return R.ok(fileUploadService.applyUploadChunk(identifier, Integer.parseInt(chunkNumber)));
    }

    @PostMapping("/upload-chunk")
    public R<Void> uploadChunk(@NotBlank String identifier,
                               @NotNull @Min(1) String chunkNumber) {
        fileUploadService.uploadChunk(identifier, Integer.parseInt(chunkNumber));
        return R.ok();
    }

    @PostMapping("/finish-upload-chunk")
    public R<Void> finishUploadChunk(@NotBlank String identifier) {
        fileUploadService.finishUploadChunk(identifier);
        return R.ok();
    }

    @GetMapping("/list")
    public R<FileListDTO> list(@Pattern(regexp = "(^\\d{1,19}$)?") String parentId) {
        return R.ok(userFileService.list(parentId));
    }

    @GetMapping("/list-folder")
    public R<FolderListDTO> listFolder(@Pattern(regexp = "(^\\d{1,19}$)?") String parentId) {
        return R.ok(userFileService.listFolders(parentId));
    }

    @PostMapping("/copy")
    public R<FolderListDTO> copy(@Size(min = 1) String[] ids,
                                 @Pattern(regexp = "(^\\d{1,19}$)?") String parentId) {
        userFileService.copy(Arrays.stream(ids).toList(), parentId);
        return R.ok();
    }

    @PostMapping("/move")
    public R<FolderListDTO> move(@Size(min = 1) String[] ids,
                                 @Pattern(regexp = "(^\\d{1,19}$)?") String parentId) {
        userFileService.move(Arrays.stream(ids).toList(), parentId);
        return R.ok();
    }
}
