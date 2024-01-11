package com.xiaoyao.netdisk.file.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaoyao.netdisk.common.exception.E;
import com.xiaoyao.netdisk.common.exception.NetdiskException;
import com.xiaoyao.netdisk.common.web.interceptor.TokenInterceptor;
import com.xiaoyao.netdisk.file.dto.ApplyUploadChunkDTO;
import com.xiaoyao.netdisk.file.dto.FileListDTO;
import com.xiaoyao.netdisk.file.dto.ShardingDTO;
import com.xiaoyao.netdisk.file.properties.MinioProperties;
import com.xiaoyao.netdisk.file.properties.ShardingProperties;
import com.xiaoyao.netdisk.file.repository.ShardingRepository;
import com.xiaoyao.netdisk.file.repository.StorageFileRepository;
import com.xiaoyao.netdisk.file.repository.UserFileRepository;
import com.xiaoyao.netdisk.file.repository.entity.Sharding;
import com.xiaoyao.netdisk.file.repository.entity.StorageFile;
import com.xiaoyao.netdisk.file.repository.entity.UserFile;
import com.xiaoyao.netdisk.file.service.FileService;
import io.minio.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FileServiceImpl implements FileService {
    private final UserFileRepository userFileRepository;
    private final ShardingRepository shardingRepository;
    private final ShardingProperties shardingProperties;
    private final MinioProperties minioProperties;
    private final MinioClient minioClient;
    private final StorageFileRepository storageFileRepository;

    public FileServiceImpl(UserFileRepository userFileRepository, ShardingRepository shardingRepository,
                           ShardingProperties shardingProperties, MinioProperties minioProperties,
                           MinioClient minioClient, StorageFileRepository storageFileRepository) {
        this.userFileRepository = userFileRepository;
        this.shardingRepository = shardingRepository;
        this.shardingProperties = shardingProperties;
        this.minioProperties = minioProperties;
        this.minioClient = minioClient;
        this.storageFileRepository = storageFileRepository;
    }

    @Override
    public void createFolder(String parentId, String folderName) {
        checkName(folderName);
        long userId = TokenInterceptor.USER_ID.get();
        Long pid = StrUtil.isBlank(parentId) ? null : Long.parseLong(parentId);
        if (pid != null && !userFileRepository.isFolderExist(pid, userId)) {
            // 父文件夹不存在
            throw new NetdiskException(E.PARENT_FOLDER_NOT_EXIST);
        }
        if (userFileRepository.isNameExist(pid, folderName, userId)) {
            // 存在同名文件或文件夹，更改文件夹名称。
            folderName = newFolderName(folderName);
        }

        UserFile folder = new UserFile();
        folder.setUserId(userId);
        folder.setParentId(pid);
        folder.setName(folderName);
        folder.setIsFolder(true);
        folder.setCreateTime(LocalDateTime.now());
        folder.setUpdateTime(LocalDateTime.now());
        userFileRepository.save(folder);
    }

    private void checkName(String name) {
        if (FileNameUtil.containsInvalid(name)) {
            throw new NetdiskException(E.FILE_NAME_INVALID);
        }
    }

    @Override
    public void rename(String fileId, String name) {
        checkName(name);
        long fid = Long.parseLong(fileId);
        long userId = TokenInterceptor.USER_ID.get();
        UserFile userFile = userFileRepository.findIsFolderById(fid, userId);
        if (userFile == null) {
            // 文件不存在
            throw new NetdiskException(E.FILE_NOT_EXIST);
        } else if (userFileRepository.isNameExist(userFile.getParentId(), name, userId)) {
            // 存在同名文件或文件夹，更改名称。
            if (userFile.getIsFolder()) {
                name = newFolderName(name);
            } else {
                name = newFileName(userFile.getParentId(), name, userId);
            }
        }

        userFile = new UserFile();
        userFile.setId(fid);
        userFile.setName(name);
        userFile.setUpdateTime(LocalDateTime.now());
        userFileRepository.update(userFile);
    }

    @Override
    public ShardingDTO createOrGetSharding(String identifier, String parentId, long size, String filename) {
        checkName(filename);
        long userId = TokenInterceptor.USER_ID.get();
        Long folderId = StrUtil.isBlank(parentId) ? null : Long.parseLong(parentId);
        if (size < shardingProperties.getMinChunkSize()) {
            // 文件太小，不需要分片
            throw new NetdiskException(E.FILE_SIZE_TOO_SMALL_TO_SHADING);
        }
        if (storageFileRepository.isIdentifierExist(identifier)) {
            // 文件已存在，直接秒传。
            ShardingDTO dto = new ShardingDTO();
            dto.setCanSecUpload(true);
            return dto;
        }
        if (folderId != null && !userFileRepository.isFolderExist(folderId, userId)) {
            // 父文件夹不存在
            throw new NetdiskException(E.PARENT_FOLDER_NOT_EXIST);
        }
        UserFile existedFile = userFileRepository.findIdentifierById(folderId, filename, userId);
        if (existedFile != null) {
            if (existedFile.getIdentifier() == null) {
                // 存在同名的文件夹，更改文件名。
                filename = newFileName(folderId, filename, userId);
            } else if (existedFile.getIdentifier().equals(identifier)) {
                // 存在同名文件，但是是同一个文件，直接返回。
                ShardingDTO dto = new ShardingDTO();
                dto.setExistSameFile(true);
                return dto;
            } else {
                // 存在同名文件，但是不是同一个文件，更改文件名。
                filename = newFileName(folderId, filename, userId);
            }
        }

        // 分片任务不存在则创建一个新的分片任务，如果已存在则返回已有的分片任务以支持断点续传。
        Sharding sharding = shardingRepository.findProgressByIdentifier(identifier, userId);
        if (sharding == null) {
            sharding = new Sharding();
            sharding.setUserId(userId);
            sharding.setIdentifier(identifier);
            sharding.setParentId(folderId);
            sharding.setFilename(filename);
            sharding.setSize(size);
            Pair<Integer, Integer> pair = computeChunkSizeAndCount(size);
            sharding.setChunkSize(pair.getKey());
            sharding.setCurrentChunk(0);
            sharding.setTotalChunk(pair.getValue());
            shardingRepository.save(sharding);
        }

        ShardingDTO dto = new ShardingDTO();
        dto.setChunkSize(sharding.getChunkSize());
        dto.setCurrentChunk(sharding.getCurrentChunk());
        dto.setTotalChunk(sharding.getTotalChunk());
        return dto;
    }

    private String newFolderName(String filename) {
        return filename + "_" + DateUtil.format(LocalDateTime.now(), "yyyyMMdd_HHmmss");
    }

    private String newFileName(Long parentId, String filename, long userId) {

        int i = 1;
        do {
            filename = StrUtil.format("{}({})", filename, i);
            i++;
        } while (userFileRepository.isNameExist(parentId, filename, userId));
        return filename;
    }

    private Pair<Integer, Integer> computeChunkSizeAndCount(long size) {
        int chunkSize = shardingProperties.getMinChunkSize();
        int count = (int) (size / chunkSize);
        int remain = (int) (size % chunkSize);
        chunkSize += remain / count;
        chunkSize += remain % count == 0 ? 0 : 1;
        return Pair.of(chunkSize, count);
    }

    @Override
    public ApplyUploadChunkDTO applyUploadChunk(String identifier, int chunkNumber) {
        Sharding sharding = shardingRepository.findProgressByIdentifier(identifier, TokenInterceptor.USER_ID.get());
        if (sharding == null) {
            // 不存在分片任务
            throw new NetdiskException(E.NO_SHADING_TASK);
        } else if (chunkNumber != sharding.getCurrentChunk() + 1 || chunkNumber > sharding.getTotalChunk()) {
            // 分片序号不正确
            throw new NetdiskException(E.INVALID_SHADING_CHUNK);
        }

        Map<String, String> formData;
        try {
            PostPolicy policy = new PostPolicy(minioProperties.getBucket(), ZonedDateTime.now().plusHours(1));
            policy.addContentLengthRangeCondition(1, sharding.getChunkSize());
            policy.addEqualsCondition("key", StrUtil.format("chunk/{}/{}", identifier, chunkNumber));
            formData = minioClient.getPresignedPostFormData(policy);
            System.out.println(formData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ApplyUploadChunkDTO dto = new ApplyUploadChunkDTO();
        dto.setKey(StrUtil.format("chunk/{}/{}", identifier, chunkNumber));
        dto.setFormData(formData);
        dto.setUploadUrl(minioProperties.getEndpoint() + "/" + minioProperties.getBucket());
        return dto;
    }

    @Override
    public void finishUploadChunk(String identifier) {
        Sharding sharding = shardingRepository.findByIdentifier(identifier, TokenInterceptor.USER_ID.get());
        if (sharding == null) {
            // 不存在分片任务
            throw new NetdiskException(E.NO_SHADING_TASK);
        } else if (sharding.getCurrentChunk().intValue() != sharding.getTotalChunk().intValue()) {
            // 分片任务未完成
            throw new NetdiskException(E.SHADING_TASK_NOT_COMPLETE);
        }

        // 合并分片
        List<ComposeSource> chunks = new ArrayList<>();
        for (int i = 1; i <= sharding.getTotalChunk(); i++) {
            chunks.add(ComposeSource.builder()
                    .bucket(minioProperties.getBucket())
                    .object(StrUtil.format("chunk/{}/{}", identifier, i))
                    .build());
        }
        String storageFilename = IdUtil.fastSimpleUUID();
        try {
            minioClient.composeObject(
                    ComposeObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            // 为MinIO中的文件添加后缀名，以便MinIO能够自动识别出文件类型。
                            .object(StrUtil.format("file/{}{}", storageFilename,
                                    FileNameUtil.extName(sharding.getFilename()).isEmpty() ?
                                            "" : "." + FileNameUtil.extName(sharding.getFilename())))
                            .sources(chunks)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 删除分片
        for (int i = 1; i <= sharding.getTotalChunk(); i++) {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(StrUtil.format("chunk/{}/{}", identifier, i))
                        .build());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // 删除分片任务
        shardingRepository.delete(identifier);

        // 保存文件信息
        StorageFile storageFile = new StorageFile();
        storageFile.setIdentifier(identifier);
        storageFile.setPath(StrUtil.format("file/{}{}", storageFilename,
                FileNameUtil.extName(sharding.getFilename()).isEmpty() ?
                        "" : "." + FileNameUtil.extName(sharding.getFilename())));
        storageFileRepository.save(storageFile);

        // 保存文件信息到用户文件表
        UserFile file = new UserFile();
        file.setUserId(TokenInterceptor.USER_ID.get());
        file.setParentId(sharding.getParentId());
        file.setName(sharding.getFilename());
        file.setIsFolder(false);
        file.setStorageFileId(storageFile.getId());
        file.setCreateTime(LocalDateTime.now());
        file.setUpdateTime(LocalDateTime.now());
        userFileRepository.save(file);
    }

    @Override
    public void uploadChunk(String identifier, int chunkNumber) {
        if (!shardingRepository.incrementChunkNumber(identifier, chunkNumber, TokenInterceptor.USER_ID.get())) {
            throw new NetdiskException(E.INVALID_SHADING_CHUNK);
        }
    }

    @Override
    public FileListDTO list(String parentId) {
        long userId = TokenInterceptor.USER_ID.get();
        List<UserFile> files = userFileRepository.findListByParentId(
                StrUtil.isBlank(parentId) ? null : Long.parseLong(parentId), userId);
        FileListDTO dto = new FileListDTO();
        List<FileListDTO.Item> items = new ArrayList<>();
        dto.setFiles(items);
        for (UserFile file : files) {
            FileListDTO.Item item = new FileListDTO.Item();
            item.setId(file.getId().toString());
            item.setName(file.getName());
            item.setFolder(file.getIsFolder());
            item.setUpdateTime(DateUtil.format(file.getUpdateTime(), "yyyy-MM-dd HH:mm:ss"));
            items.add(item);
        }
        return dto;
    }
}
