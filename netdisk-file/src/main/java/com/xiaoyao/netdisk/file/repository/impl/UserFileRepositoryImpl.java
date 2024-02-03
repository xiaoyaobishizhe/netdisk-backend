package com.xiaoyao.netdisk.file.repository.impl;

import com.xiaoyao.netdisk.file.repository.FileTreeNode;
import com.xiaoyao.netdisk.file.repository.entity.UserFile;
import com.xiaoyao.netdisk.file.repository.UserFileRepository;
import com.xiaoyao.netdisk.file.repository.mapper.UserFileMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;
import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaUpdate;

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
                .eq(UserFile::getName, name)
                .eq(UserFile::getIsDeleted, false)) > 0;
    }

    @Override
    public boolean isFolderExist(long folderId, long userId) {
        return userFileMapper.selectCount(lambdaQuery(UserFile.class)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getId, folderId)
                .eq(UserFile::getIsFolder, true)
                .eq(UserFile::getIsDeleted, false)) > 0;
    }

    @Override
    public UserFile findIdentifierById(Long folderId, String name, long userId) {
        return userFileMapper.selectOne(lambdaQuery(UserFile.class)
                .select(UserFile::getIdentifier)
                .eq(UserFile::getUserId, userId)
                .isNull(folderId == null, UserFile::getParentId)
                .eq(folderId != null, UserFile::getParentId, folderId)
                .eq(UserFile::getName, name)
                .eq(UserFile::getIsDeleted, false));
    }

    @Override
    public String findFolderPathById(long id, long userId) {
        UserFile userFile = userFileMapper.selectOne(lambdaQuery(UserFile.class)
                .select(UserFile::getPath,
                        UserFile::getName)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getId, id)
                .eq(UserFile::getIsFolder, true)
                .eq(UserFile::getIsDeleted, false));
        return userFile.getPath() + userFile.getName() + "/";
    }

    @Override
    public void save(UserFile userFile) {
        userFileMapper.insert(userFile);
    }

    @Override
    public UserFile findIsFolderById(long i, long userId) {
        return userFileMapper.selectOne(lambdaQuery(UserFile.class)
                .select(UserFile::getIsFolder,
                        UserFile::getParentId,
                        UserFile::getName)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getId, i)
                .eq(UserFile::getIsDeleted, false));
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
                .eq(parentId != null, UserFile::getParentId, parentId)
                .eq(UserFile::getIsDeleted, false));
    }

    @Override
    public void updatePathByParentId(String path, long parentId, long userId) {
        userFileMapper.update(null, lambdaUpdate(UserFile.class)
                .set(UserFile::getPath, path)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getParentId, parentId)
                .eq(UserFile::getIsDeleted, false));
    }

    @Override
    public FileTreeNode findFileTreeById(long id, long userId, String oldName) {
        UserFile userFile = userFileMapper.selectOne(lambdaQuery(UserFile.class)
                .select(UserFile::getId,
                        UserFile::getPath,
                        UserFile::getName,
                        UserFile::getIsFolder)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, false)
                .eq(UserFile::getId, id));
        FileTreeNode node = convertToTreeNode(userFile);
        List<UserFile> userFiles = userFileMapper.selectList(lambdaQuery(UserFile.class)
                .select(UserFile::getId,
                        UserFile::getParentId,
                        UserFile::getPath,
                        UserFile::getName,
                        UserFile::getIsFolder)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, false)
                .likeRight(UserFile::getPath, userFile.getPath() + oldName + "/"));
        findChildren(node, userFiles);
        return node;
    }

    private FileTreeNode convertToTreeNode(UserFile userFile) {
        FileTreeNode node = new FileTreeNode();
        node.setId(userFile.getId());
        node.setPath(userFile.getPath());
        node.setName(userFile.getName());
        node.setFolder(userFile.getIsFolder());
        node.setChildren(new ArrayList<>());
        return node;
    }

    private void findChildren(FileTreeNode node, List<UserFile> userFiles) {
        userFiles.stream()
                .filter(file -> file.getParentId() == node.getId())
                .forEach(file -> {
                    FileTreeNode child = convertToTreeNode(file);
                    node.getChildren().add(child);
                    findChildren(child, userFiles);
                });
    }
}
