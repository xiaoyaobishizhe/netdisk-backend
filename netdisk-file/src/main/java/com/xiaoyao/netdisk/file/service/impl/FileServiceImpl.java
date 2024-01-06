package com.xiaoyao.netdisk.file.service.impl;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaoyao.netdisk.common.exception.E;
import com.xiaoyao.netdisk.common.exception.NetdiskException;
import com.xiaoyao.netdisk.common.web.interceptor.TokenInterceptor;
import com.xiaoyao.netdisk.file.dto.ApplyUploadChunkDTO;
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
        if (userFileRepository.isExistFolder(parentId == null ? null : Long.valueOf(parentId), folderName)) {
            throw new NetdiskException(E.FOLDER_ALREADY_EXIST);
        }

        UserFile folder = new UserFile();
        folder.setUserId(TokenInterceptor.USER_ID.get());
        folder.setParentId(StrUtil.isBlank(parentId) ? null : Long.parseLong(parentId));
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
        // 判断父文件夹下是否存在同名文件夹或文件
        long userId = TokenInterceptor.USER_ID.get();
        UserFile file = userFileRepository.findNameAndParentIdById(Long.parseLong(fileId), userId);
        if (file == null) {
            throw new NetdiskException(E.FILE_NOT_EXIST);
        } else if (userFileRepository.isExistName(file.getParentId(), name, userId)) {
            throw new NetdiskException(E.FILE_NAME_ALREADY_EXIST);
        }

        file = new UserFile();
        file.setId(Long.parseLong(fileId));
        file.setName(name);
        file.setUpdateTime(LocalDateTime.now());
        userFileRepository.update(file);
    }

    @Override
    public ShardingDTO createOrGetSharding(String identifier, String parentId, long size, String filename) {
        if (size < shardingProperties.getMinChunkSize()) {
            throw new NetdiskException(E.FILE_SIZE_TOO_SMALL_TO_SHADING);
        }
        long userId = TokenInterceptor.USER_ID.get();
        if (StrUtil.isNotBlank(parentId) && !userFileRepository.isExistParentId(Long.parseLong(parentId), userId)) {
            throw new NetdiskException(E.PARENT_FOLDER_NOT_EXIST);
        }

        // 创建分片任务前先查看是否已经存在分片任务，如果已存在则断点续传。
        Sharding sharding = shardingRepository.findByIdentifier(identifier, userId);
        if (sharding == null) {
            sharding = new Sharding();
            sharding.setUserId(userId);
            sharding.setIdentifier(identifier);
            sharding.setParentId(StrUtil.isBlank(parentId) ? null : Long.parseLong(parentId));
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
        Sharding sharding = shardingRepository.findByIdentifier(identifier, TokenInterceptor.USER_ID.get());
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
                            .object(StrUtil.format("file/{}", storageFilename))
                            .sources(chunks)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 删除分片任务
        shardingRepository.delete(identifier);

        StorageFile storageFile = new StorageFile();
        storageFile.setPath(StrUtil.format("file/{}", storageFilename));
        storageFileRepository.save(storageFile);

        UserFile file = new UserFile();
        file.setUserId(TokenInterceptor.USER_ID.get());
        file.setParentId(sharding.getParentId());
        file.setName(sharding.getFilename());
        file.setIsFolder(false);
        file.setStorageFileId(storageFile.getId());
        file.setCreateTime(LocalDateTime.now());
        file.setUpdateTime(LocalDateTime.now());
        userFileRepository.save(file);

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
    }

    @Override
    public void uploadChunk(String identifier, int chunkNumber) {
        if (!shardingRepository.incrementChunkNumber(identifier, chunkNumber, TokenInterceptor.USER_ID.get())) {
            throw new NetdiskException(E.INVALID_SHADING_CHUNK);
        }
    }
}
