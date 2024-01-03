package com.xiaoyao.netdisk.file.service.impl;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaoyao.netdisk.common.exception.E;
import com.xiaoyao.netdisk.common.exception.NetdiskException;
import com.xiaoyao.netdisk.common.web.interceptor.TokenInterceptor;
import com.xiaoyao.netdisk.file.entity.UserFile;
import com.xiaoyao.netdisk.file.repository.UserFileRepository;
import com.xiaoyao.netdisk.file.service.FileService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FileServiceImpl implements FileService {
    private final UserFileRepository userFileRepository;

    public FileServiceImpl(UserFileRepository userFileRepository) {
        this.userFileRepository = userFileRepository;
    }

    @Override
    public void createFolder(String parentId, String folderName) {
        if (FileNameUtil.containsInvalid(folderName)) {
            throw new NetdiskException(E.FOLDER_NAME_INVALID);
        }
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
}
