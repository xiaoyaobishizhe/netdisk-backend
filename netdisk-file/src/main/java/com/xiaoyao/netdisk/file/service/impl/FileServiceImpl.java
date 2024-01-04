package com.xiaoyao.netdisk.file.service.impl;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaoyao.netdisk.common.exception.E;
import com.xiaoyao.netdisk.common.exception.NetdiskException;
import com.xiaoyao.netdisk.common.web.interceptor.TokenInterceptor;
import com.xiaoyao.netdisk.file.dto.ShardingDTO;
import com.xiaoyao.netdisk.file.properties.ShardingProperties;
import com.xiaoyao.netdisk.file.repository.ShardingRepository;
import com.xiaoyao.netdisk.file.repository.UserFileRepository;
import com.xiaoyao.netdisk.file.repository.entity.Sharding;
import com.xiaoyao.netdisk.file.repository.entity.UserFile;
import com.xiaoyao.netdisk.file.service.FileService;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class FileServiceImpl implements FileService {
    private final UserFileRepository userFileRepository;
    private final ShardingRepository shardingRepository;
    private final ShardingProperties shardingProperties;

    public FileServiceImpl(UserFileRepository userFileRepository, ShardingRepository shardingRepository,
                           ShardingProperties shardingProperties) {
        this.userFileRepository = userFileRepository;
        this.shardingRepository = shardingRepository;
        this.shardingProperties = shardingProperties;
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
    public ShardingDTO createOrGetSharding(String identifier, long totalSize) {
        // 创建分片任务前先查看是否已经存在分片任务，如果已存在则断点续传。
        Sharding sharding = shardingRepository.findByIdentifier(identifier, TokenInterceptor.USER_ID.get());
        if (sharding == null) {
            sharding = new Sharding();
            sharding.setUserId(TokenInterceptor.USER_ID.get());
            sharding.setIdentifier(identifier);
            sharding.setChunkSize(shardingProperties.getChunkSize());
            sharding.setCurrentChunk(0);
            sharding.setTotalChunk((int) NumberUtil.div(totalSize, (long) shardingProperties.getChunkSize(),
                    0, RoundingMode.CEILING));
            shardingRepository.save(sharding);
        }

        ShardingDTO dto = new ShardingDTO();
        dto.setChunkSize(sharding.getChunkSize());
        dto.setCurrentChunk(sharding.getCurrentChunk());
        dto.setTotalChunk(sharding.getTotalChunk());
        return dto;
    }
}
