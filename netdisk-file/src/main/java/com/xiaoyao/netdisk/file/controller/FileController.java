package com.xiaoyao.netdisk.file.controller;

import com.xiaoyao.netdisk.common.exception.R;
import com.xiaoyao.netdisk.file.dto.ApplyUploadChunkDTO;
import com.xiaoyao.netdisk.file.dto.FileListDTO;
import com.xiaoyao.netdisk.file.dto.FolderListDTO;
import com.xiaoyao.netdisk.file.dto.ShardingDTO;
import com.xiaoyao.netdisk.file.service.FileUploadService;
import com.xiaoyao.netdisk.file.service.RecycleBinService;
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
    private final RecycleBinService recycleBinService;

    public FileController(UserFileService userFileService, FileUploadService fileUploadService,
                          RecycleBinService recycleBinService) {
        this.userFileService = userFileService;
        this.fileUploadService = fileUploadService;
        this.recycleBinService = recycleBinService;
    }

    @PutMapping("/folder")
    public R<Void> createFolder(@Pattern(regexp = "(^\\d{1,19}$)?") String parentId,
                                @NotNull @Length(min = 1, max = 250) String folderName) {
        userFileService.createFolder(parentId, folderName);
        return R.ok();
    }

    @PostMapping("/rename")
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
        return R.ok(userFileService.list(parentId, true));
    }

    @GetMapping("/list-folder")
    public R<FolderListDTO> listFolder(@Pattern(regexp = "(^\\d{1,19}$)?") String parentId) {
        return R.ok(userFileService.listFolders(parentId));
    }

    @PostMapping("/copy")
    public R<Void> copy(@Size(min = 1) String[] ids,
                        @Pattern(regexp = "(^\\d{1,19}$)?") String parentId) {
        userFileService.copy(Arrays.stream(ids).toList(), parentId);
        return R.ok();
    }

    @PostMapping("/move")
    public R<Void> move(@Size(min = 1) String[] ids,
                        @Pattern(regexp = "(^\\d{1,19}$)?") String parentId) {
        userFileService.move(Arrays.stream(ids).toList(), parentId);
        return R.ok();
    }

    @PostMapping("/delete")
    public R<Void> delete(@Size(min = 1) String[] ids) {
        recycleBinService.delete(Arrays.stream(ids).toList());
        return R.ok();
    }
}
