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

    private void checkName(String name) {
        if (FileNameUtil.containsInvalid(name)) {
            throw new NetdiskException(E.FILE_NAME_INVALID);
        }
    }
}
