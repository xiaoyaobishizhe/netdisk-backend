package com.xiaoyao.netdisk.file.repository.impl;

import com.xiaoyao.netdisk.file.repository.entity.UserFile;
import com.xiaoyao.netdisk.file.repository.UserFileRepository;
import com.xiaoyao.netdisk.file.repository.mapper.UserFileMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;

@Repository
public class UserFileRepositoryImpl implements UserFileRepository {
    private final UserFileMapper userFileMapper;

    public UserFileRepositoryImpl(UserFileMapper userFileMapper) {
        this.userFileMapper = userFileMapper;
    }

    @Override
    public boolean isNameExist(Long parentId, String name, long userId) {
        return userFileMapper.selectCount(lambdaQuery(UserFile.class)
                .eq(UserFile::getUserId, userId)
                .isNull(parentId == null, UserFile::getParentId)
                .eq(parentId != null, UserFile::getParentId, parentId)
                .eq(UserFile::getName, name)) > 0;
    }

    @Override
    public boolean isFolderExist(long folderId, long userId) {
        return userFileMapper.selectCount(lambdaQuery(UserFile.class)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getId, folderId)
                .eq(UserFile::getIsFolder, true)) > 0;
    }

    @Override
    public UserFile findIdentifierById(Long folderId, String name, long userId) {
        return userFileMapper.selectOne(lambdaQuery(UserFile.class)
                .select(UserFile::getIdentifier)
                .eq(UserFile::getUserId, userId)
                .isNull(folderId == null, UserFile::getParentId)
                .eq(folderId != null, UserFile::getParentId, folderId)
                .eq(UserFile::getName, name));
    }

    @Override
    public void save(UserFile userFile) {
        userFileMapper.insert(userFile);
    }

    @Override
    public UserFile findIsFolderById(long i, long userId) {
        return userFileMapper.selectOne(lambdaQuery(UserFile.class)
                .select(UserFile::getIsFolder,
                        UserFile::getParentId)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getId, i));
    }

    @Override
    public void update(UserFile file) {
        userFileMapper.updateById(file);
    }

    @Override
    public List<UserFile> findListByParentId(Long parentId, long userId) {
        return userFileMapper.selectList(lambdaQuery(UserFile.class)
                .select(UserFile::getId,
                        UserFile::getName,
                        UserFile::getIsFolder,
                        UserFile::getSize,
                        UserFile::getUpdateTime)
                .eq(UserFile::getUserId, userId)
                .isNull(parentId == null, UserFile::getParentId)
                .eq(parentId != null, UserFile::getParentId, parentId));
    }
}
