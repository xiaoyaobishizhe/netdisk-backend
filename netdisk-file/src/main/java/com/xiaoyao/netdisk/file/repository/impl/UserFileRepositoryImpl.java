package com.xiaoyao.netdisk.file.repository.impl;

import com.xiaoyao.netdisk.file.entity.UserFile;
import com.xiaoyao.netdisk.file.repository.UserFileRepository;
import com.xiaoyao.netdisk.file.repository.mapper.UserFileMapper;
import org.springframework.stereotype.Repository;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;

@Repository
public class UserFileRepositoryImpl implements UserFileRepository {
    private final UserFileMapper userFileMapper;

    public UserFileRepositoryImpl(UserFileMapper userFileMapper) {
        this.userFileMapper = userFileMapper;
    }

    @Override
    public boolean isExistFolder(Long parentId, String folderName) {
        return userFileMapper.selectCount(lambdaQuery(UserFile.class)
                .eq(parentId != null, UserFile::getParentId, parentId)
                .eq(UserFile::getName, folderName)) > 0;
    }

    @Override
    public void save(UserFile userFile) {
        userFileMapper.insert(userFile);
    }
}
