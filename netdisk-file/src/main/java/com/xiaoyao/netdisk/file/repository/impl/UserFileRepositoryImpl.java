package com.xiaoyao.netdisk.file.repository.impl;

import com.xiaoyao.netdisk.file.repository.UserFileRepository;
import com.xiaoyao.netdisk.file.repository.UserFileTreeNode;
import com.xiaoyao.netdisk.file.repository.entity.UserFile;
import com.xiaoyao.netdisk.file.repository.mapper.UserFileMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;
import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaUpdate;

@Repository
public class UserFileRepositoryImpl implements UserFileRepository {
    private final UserFileMapper userFileMapper;

    public UserFileRepositoryImpl(UserFileMapper userFileMapper) {
        this.userFileMapper = userFileMapper;
    }

    @Override
    public boolean isNameExistInParent(Long parentId, String name, long userId) {
        return isNameExistInParent(parentId, List.of(name), userId);
    }

    @Override
    public boolean isNameExistInParent(Long parentId, List<String> names, long userId) {
        return userFileMapper.selectCount(lambdaQuery(UserFile.class)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, false)
                .eq(parentId != null, UserFile::getParentId, parentId).isNull(parentId == null, UserFile::getParentId)
                .in(UserFile::getName, names)) > 0;
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
    public String getFileIdentifierInParent(Long parentId, String name, long userId) {
        UserFile userFile = userFileMapper.selectOne(lambdaQuery(UserFile.class)
                .select(UserFile::getIdentifier)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, false)
                .eq(parentId != null, UserFile::getParentId, parentId).isNull(parentId == null, UserFile::getParentId)
                .eq(UserFile::getName, name));
        if (userFile == null) {
            return null;
        }
        return userFile.getIdentifier() == null ? "" : userFile.getIdentifier();
    }

    @Override
    public String getFolderFullPath(long folderId, long userId) {
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
        // TODO 手动生成id
        userFileMapper.insert(userFile);
    }

    @Override
    public void save(List<UserFile> userFiles) {
        userFileMapper.insertMany(userFiles);
    }

    @Override
    public List<UserFile> findListByParentId(Long parentId, boolean onlyFolder, Long userId) {
        return userFileMapper.selectList(lambdaQuery(UserFile.class)
                .select(UserFile::getId,
                        UserFile::getName,
                        UserFile::getIsFolder,
                        UserFile::getSize,
                        UserFile::getUpdateTime)
                .eq(userId != null, UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, false)
                .eq(parentId != null, UserFile::getParentId, parentId).isNull(parentId == null, UserFile::getParentId)
                .eq(onlyFolder, UserFile::getIsFolder, true));
    }

    @Override
    public List<UserFile> listDeletedRootFile(long userId) {
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
    public void delete(List<Long> ids) {
        userFileMapper.delete(lambdaQuery(UserFile.class)
                .in(UserFile::getId, ids));
    }

    @Override
    public void deleteAllDeleted(long userId) {
        userFileMapper.delete(lambdaQuery(UserFile.class)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, true));
    }

    @Override
    public Long getFolderIdInPathByName(String path, String name, long userId) {
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
    public boolean isAllExist(List<Long> ids, long userId) {
        return userFileMapper.selectCount(lambdaQuery(UserFile.class)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, false)
                .in(UserFile::getId, ids)) == ids.size();
    }

    @Override
    public UserFileTreeNode findUserFileTree(long id, boolean isDeleted, long userId) {
        List<UserFileTreeNode> trees = findUserFileTrees(List.of(id), isDeleted, userId);
        return trees.isEmpty() ? null : trees.get(0);
    }

    @Override
    public List<UserFileTreeNode> findUserFileTrees(List<Long> ids, boolean isDeleted, Long userId) {
        // TODO 优化查询字段，而不是一律查询所有字段
        List<UserFileTreeNode> result = new ArrayList<>();
        userFileMapper.selectList(lambdaQuery(UserFile.class)
                .eq(userId != null, UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, isDeleted)
                // 只能查询回收站中的根节点树
                .isNull(isDeleted, UserFile::getParentId)
                .in(UserFile::getId, ids)).forEach(root -> {
            if (root.getIsFolder()) {
                result.add(composeUserFileTree(root, userFileMapper.selectList(lambdaQuery(UserFile.class)
                        .eq(userId != null, UserFile::getUserId, userId)
                        .eq(UserFile::getIsDeleted, isDeleted)
                        .likeRight(UserFile::getPath, root.getPath() + root.getName() + "/"))));
            } else {
                result.add(new UserFileTreeNode(root));
            }
        });
        return result;
    }

    private UserFileTreeNode composeUserFileTree(UserFile root, List<UserFile> children) {
        UserFileTreeNode node = new UserFileTreeNode(root);
        children.stream()
                .filter(file -> root.getId().equals(file.getParentId()))
                .forEach(file -> node.getChildren().add(composeUserFileTree(file, children)));
        return node;
    }

    @Override
    public void updateParentIdAndPath(List<UserFileTreeNode> trees, Long parentId) {
        userFileMapper.update(null, lambdaUpdate(UserFile.class)
                .set(UserFile::getParentId, parentId)
                .set(UserFile::getPath, trees.get(0).getValue().getPath())
                .in(UserFile::getId, trees.stream().map(node -> node.getValue().getId()).toList()));
        trees.forEach(this::updateChildPath);
    }

    /**
     * 更新指定节点下所有层级的子节点的path。
     */
    private void updateChildPath(UserFileTreeNode node) {
        if (node.getChildren().isEmpty()) {
            return;
        }
        userFileMapper.update(null, lambdaUpdate(UserFile.class)
                .set(UserFile::getPath, node.getChildren().get(0).getValue().getPath())
                .in(UserFile::getId, node.getChildren().stream().map(child -> child.getValue().getId()).toList()));
        node.getChildren().forEach(this::updateChildPath);
    }

    @Override
    public void moveToRecycleBin(List<UserFileTreeNode> trees) {
        userFileMapper.update(null, lambdaUpdate(UserFile.class)
                .set(UserFile::getParentId, null)
                .set(UserFile::getIsDeleted, true)
                .set(UserFile::getDeleteTime, LocalDateTime.now())
                .in(UserFile::getId, trees.stream().map(node -> node.getValue().getId()).toList()));
        List<Long> child = trees.stream()
                .flatMap(node -> node.collectAll().stream()
                        .map(UserFile::getId)
                        .filter(id -> !id.equals(node.getValue().getId())))
                .toList();
        if (!child.isEmpty()) {
            userFileMapper.update(null, lambdaUpdate(UserFile.class)
                    .set(UserFile::getIsDeleted, true)
                    .in(UserFile::getId, child));
        }
    }

    @Override
    public void moveToUserSpace(List<UserFileTreeNode> trees) {
        trees.forEach(tree -> userFileMapper.update(null, lambdaUpdate(UserFile.class)
                .set(UserFile::getParentId, tree.getValue().getParentId())
                .set(UserFile::getIsDeleted, false)
                .set(UserFile::getDeleteTime, null)
                .eq(UserFile::getId, tree.getValue().getId())));
        List<Long> child = trees.stream()
                .flatMap(node -> node.collectAll().stream()
                        .map(UserFile::getId)
                        .filter(id -> !id.equals(node.getValue().getId())))
                .toList();
        if (!child.isEmpty()) {
            userFileMapper.update(null, lambdaUpdate(UserFile.class)
                    .set(UserFile::getIsDeleted, false)
                    .in(UserFile::getId, child));
        }
    }

    @Override
    public void updateNameAndPath(UserFileTreeNode node) {
        userFileMapper.update(null, lambdaUpdate(UserFile.class)
                .set(UserFile::getName, node.getValue().getName())
                .set(UserFile::getUpdateTime, LocalDateTime.now())
                .eq(UserFile::getId, node.getValue().getId()));
        updateChildPath(node);
    }

    @Override
    public Map<Long, String> findPathByIds(List<Long> ids) {
        Map<Long, String> result = new HashMap<>();
        userFileMapper.selectList(lambdaQuery(UserFile.class)
                        .select(UserFile::getId,
                                UserFile::getPath,
                                UserFile::getName)
                        .in(UserFile::getId, ids))
                .forEach(file -> result.put(file.getId(), file.getPath() + file.getName() + "/"));
        return result;
    }

    @Override
    public List<UserFile> findListByIds(List<Long> ids) {
        return userFileMapper.selectList(lambdaQuery(UserFile.class)
                .select(UserFile::getId,
                        UserFile::getName,
                        UserFile::getIsFolder,
                        UserFile::getSize,
                        UserFile::getUpdateTime)
                .in(UserFile::getId, ids));
    }
}
