package com.xiaoyao.netdisk.file.repository.impl;

import com.xiaoyao.netdisk.file.repository.FileTreeNode;
import com.xiaoyao.netdisk.file.repository.UserFileRepository;
import com.xiaoyao.netdisk.file.repository.UserFileTreeNode;
import com.xiaoyao.netdisk.file.repository.entity.UserFile;
import com.xiaoyao.netdisk.file.repository.mapper.UserFileMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
                .eq(UserFile::getIsDeleted, false)
                .eq(parentId != null, UserFile::getParentId, parentId).isNull(parentId == null, UserFile::getParentId)
                .eq(UserFile::getName, name)) > 0;
    }

    @Override
    public boolean isNameExist(Long parentId, List<String> name, long userId) {
        return userFileMapper.selectCount(lambdaQuery(UserFile.class)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, false)
                .eq(parentId != null, UserFile::getParentId, parentId).isNull(parentId == null, UserFile::getParentId)
                .in(UserFile::getName, name)) > 0;
    }

    @Override
    public boolean isFolderExist(long folderId, long userId) {
        return userFileMapper.selectCount(lambdaQuery(UserFile.class)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, false)
                .eq(UserFile::getId, folderId)
                .eq(UserFile::getIsFolder, true)) > 0;
    }

    @Override
    public String getIdentifier(Long folderId, String name, long userId) {
        UserFile userFile = userFileMapper.selectOne(lambdaQuery(UserFile.class)
                .select(UserFile::getIdentifier)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, false)
                .eq(folderId != null, UserFile::getParentId, folderId).isNull(folderId == null, UserFile::getParentId)
                .eq(UserFile::getName, name));
        if (userFile == null) {
            return null;
        }
        return userFile.getIdentifier() == null ? "" : userFile.getIdentifier();
    }

    @Override
    public String getPathByFolderId(long folderId, long userId) {
        UserFile userFile = userFileMapper.selectOne(lambdaQuery(UserFile.class)
                .select(UserFile::getPath,
                        UserFile::getName)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, false)
                .eq(UserFile::getId, folderId)
                .eq(UserFile::getIsFolder, true));
        if (userFile == null) {
            return null;
        }
        return userFile.getPath() + userFile.getName() + "/";
    }

    @Override
    public void save(UserFile userFile) {
        userFileMapper.insert(userFile);
    }

    @Override
    public UserFile findIsFolderAndParentIdAndNameById(long id, long userId) {
        return userFileMapper.selectOne(lambdaQuery(UserFile.class)
                .select(UserFile::getIsFolder,
                        UserFile::getParentId,
                        UserFile::getName)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, false)
                .eq(UserFile::getId, id));
    }

    @Override
    public void update(UserFile file) {
        userFileMapper.updateById(file);
    }

    @Override
    public List<UserFile> findListByParentId(Long parentId, boolean onlyFolder, long userId) {
        return userFileMapper.selectList(lambdaQuery(UserFile.class)
                .select(UserFile::getId,
                        UserFile::getName,
                        UserFile::getIsFolder,
                        UserFile::getSize,
                        UserFile::getUpdateTime)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, false)
                .eq(parentId != null, UserFile::getParentId, parentId).isNull(parentId == null, UserFile::getParentId)
                .eq(onlyFolder, UserFile::getIsFolder, true));
    }

    @Override
    public void updatePathByParentId(String path, long parentId, long userId) {
        userFileMapper.update(null, lambdaUpdate(UserFile.class)
                .set(UserFile::getPath, path)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, false)
                .eq(UserFile::getParentId, parentId));
    }

    @Override
    public List<UserFile> listDeleted(long userId) {
        return userFileMapper.selectList(lambdaQuery(UserFile.class)
                .select(UserFile::getId,
                        UserFile::getName,
                        UserFile::getIsFolder,
                        UserFile::getDeleteTime)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, true)
                .isNull(UserFile::getParentId));
    }

    @Override
    public void updateParentId(long fid, Long parentId, boolean isDeleted, long userId) {
        userFileMapper.update(null, lambdaUpdate(UserFile.class)
                .set(isDeleted, UserFile::getParentId, parentId)
                .set(!isDeleted, UserFile::getParentId, null)
                .set(!isDeleted, UserFile::getDeleteTime, LocalDateTime.now())
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, isDeleted)
                .eq(UserFile::getId, fid));
    }

    @Override
    public void updateIsDeleted(List<Long> ids, boolean isDeleted, long userId) {
        userFileMapper.update(null, lambdaUpdate(UserFile.class)
                .set(UserFile::getIsDeleted, isDeleted)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, !isDeleted)
                .in(UserFile::getId, ids));
    }

    @Override
    public void delete(List<Long> ids, long userId) {
        userFileMapper.delete(lambdaQuery(UserFile.class)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, true)
                .in(UserFile::getId, ids));
    }

    @Override
    public void deleteAllDeleted(long userId) {
        userFileMapper.delete(lambdaQuery(UserFile.class)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, true));
    }

    @Override
    public FileTreeNode findFileTree(long id, String oldName, long userId) {
        return findFileTree(id, false, oldName, userId);
    }

    @Override
    public FileTreeNode findFileTree(long id, long userId) {
        return findFileTree(id, false, null, userId);
    }

    @Override
    public FileTreeNode findDeletedFileTree(long id, long userId) {
        return findFileTree(id, true, null, userId);
    }

    @Override
    public Long getFolderId(String path, String name, long userId) {
        UserFile userFile = userFileMapper.selectOne(lambdaQuery(UserFile.class)
                .select(UserFile::getId)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, false)
                .eq(UserFile::getPath, path)
                .eq(UserFile::getIsFolder, true)
                .eq(UserFile::getName, name));
        return userFile == null ? null : userFile.getId();
    }

    @Override
    public boolean isAllExist(List<Long> fileList, long userId) {
        return userFileMapper.selectCount(lambdaQuery(UserFile.class)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, false)
                .in(UserFile::getId, fileList)) == fileList.size();
    }

    @Override
    public List<UserFile> findListByIds(List<Long> ids, long userId) {
        return userFileMapper.selectList(lambdaQuery(UserFile.class)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, false)
                .in(UserFile::getId, ids));
    }

    @Override
    public List<UserFile> findListByPaths(List<String> paths, long userId) {
        return userFileMapper.selectList(lambdaQuery(UserFile.class)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, false)
                // TODO bug 应该是多个likeRight
                .in(UserFile::getPath, paths));
    }

    @Override
    public void banchSave(List<UserFile> userFiles) {
        userFileMapper.insertMany(userFiles);
    }

    @Override
    public List<UserFileTreeNode> findUserFileTreesByIds(List<Long> ids, long userId) {
        List<UserFileTreeNode> result = new ArrayList<>();
        userFileMapper.selectList(lambdaQuery(UserFile.class)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, false)
                .in(UserFile::getId, ids)).forEach(root -> {
            if (root.getIsFolder()) {
                result.add(composeUserFileTree(root, userFileMapper.selectList(lambdaQuery(UserFile.class)
                        .eq(UserFile::getUserId, userId)
                        .eq(UserFile::getIsDeleted, false)
                        .likeRight(UserFile::getPath, root.getPath() + root.getName() + "/"))));
            } else {
                result.add(new UserFileTreeNode(root));
            }
        });
        return result;
    }

    @Override
    public void updateParentIdAndPath(List<UserFileTreeNode> trees, Long parentId) {
        userFileMapper.update(null, lambdaUpdate(UserFile.class)
                .set(UserFile::getParentId, parentId)
                .set(UserFile::getPath, trees.get(0).getValue().getPath())
                .in(UserFile::getId, trees.stream().map(node -> node.getValue().getId()).toList()));
        trees.forEach(this::updateChildPath);
    }

    @Override
    public void moveToRecycleBin(List<UserFileTreeNode> trees) {
        System.out.println(trees.stream().map(node -> node.getValue().getId()).toList());
        userFileMapper.update(null, lambdaUpdate(UserFile.class)
                .set(UserFile::getParentId, null)
                .set(UserFile::getIsDeleted, true)
                .set(UserFile::getDeleteTime, LocalDateTime.now())
                .in(UserFile::getId, trees.stream().map(node -> node.getValue().getId()).toList()));
        List<Long> child = trees.stream()
                .flatMap(node -> node.collectFolder().stream()
                        .map(UserFile::getId)
                        .filter(id -> !id.equals(node.getValue().getId())))
                .toList();
        if (!child.isEmpty()) {
            userFileMapper.update(null, lambdaUpdate(UserFile.class)
                    .set(UserFile::getIsDeleted, true)
                    .set(UserFile::getDeleteTime, LocalDateTime.now())
                    .in(UserFile::getId, child));
        }
    }

    private void updateChildPath(UserFileTreeNode node) {
        if (node.getChildren().isEmpty()) {
            return;
        }
        userFileMapper.update(null, lambdaUpdate(UserFile.class)
                .set(UserFile::getPath, node.getChildren().get(0).getValue().getPath())
                .in(UserFile::getId, node.getChildren().stream().map(child -> child.getValue().getId()).toList()));
        node.getChildren().forEach(this::updateChildPath);
    }

    private UserFileTreeNode composeUserFileTree(UserFile root, List<UserFile> children) {
        UserFileTreeNode node = new UserFileTreeNode(root);
        children.stream()
                .filter(file -> file.getParentId().equals(root.getId()))
                .forEach(file -> {
                    node.getChildren().add(composeUserFileTree(file, children));
                });
        return node;
    }

    private FileTreeNode findFileTree(long id, boolean isDeleted, String oldName, long userId) {
        UserFile userFile = userFileMapper.selectOne(lambdaQuery(UserFile.class)
                .select(UserFile::getId,
                        UserFile::getPath,
                        UserFile::getName,
                        UserFile::getIsFolder)
                .select(isDeleted, UserFile::getParentId)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, isDeleted)
                .eq(UserFile::getId, id));
        if (userFile == null || (isDeleted && userFile.getParentId() != null)) {
            return null;
        }
        FileTreeNode node = convertToTreeNode(userFile);
        if (!userFile.getIsFolder()) {
            return node;
        }
        List<UserFile> userFiles = userFileMapper.selectList(lambdaQuery(UserFile.class)
                .select(UserFile::getId,
                        UserFile::getParentId,
                        UserFile::getPath,
                        UserFile::getName,
                        UserFile::getIsFolder)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, isDeleted)
                .likeRight(UserFile::getPath, userFile.getPath() + (oldName == null ? userFile.getName() : oldName) + "/"));
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
