package com.xiaoyao.netdisk.file.repository.impl;

import com.xiaoyao.netdisk.file.repository.entity.UserFile;
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
    public boolean isExistName(Long parentId, String name, long userId) {
        return userFileMapper.selectCount(lambdaQuery(UserFile.class)
                .eq(UserFile::getUserId, userId)
                .isNull(parentId == null, UserFile::getParentId)
                .eq(parentId != null, UserFile::getParentId, parentId)
                .eq(UserFile::getName, name)) > 0;
    }

    @Override
    public boolean isExistParentId(long parentId, long userId) {
        return userFileMapper.selectCount(lambdaQuery(UserFile.class)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getParentId, parentId)) > 0;
    }

    @Override
    public void save(UserFile userFile) {
        userFileMapper.insert(userFile);
    }

    @Override
    public UserFile findNameAndParentIdById(long fileId, long userId) {
        return userFileMapper.selectOne(lambdaQuery(UserFile.class)
                .select(UserFile::getName,
                        UserFile::getParentId)
                .eq(UserFile::getId, fileId)
                .eq(UserFile::getUserId, userId));
    }

    @Override
    public void update(UserFile file) {
        userFileMapper.updateById(file);
    }
}
