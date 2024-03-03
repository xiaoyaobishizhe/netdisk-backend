package com.xiaoyao.netdisk.file.repository;

import cn.hutool.core.util.IdUtil;
import com.xiaoyao.netdisk.file.repository.entity.UserFile;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserFileTreeNode {
    private UserFile value;
    private List<UserFileTreeNode> children = new ArrayList<>();

    public UserFileTreeNode(UserFile value) {
        this.value = value;
    }

    /**
     * 递归刷新所有节点的路径。
     *
     * @param path 新的路径
     */
    public void refreshPathDeeply(String path) {
        doRefreshPathDeeply(this, path);
    }

    private void doRefreshPathDeeply(UserFileTreeNode node, String path) {
        node.value.setPath(path);
        node.children.forEach(child -> doRefreshPathDeeply(child, node.value.getPath() + node.value.getName() + "/"));
    }

    /**
     * 递归刷新所有节点的id和路径，新的id是使用雪花算法来随机生成的。
     *
     * @param path        新的路径
     * @param refreshTime 是否同时刷新节点的创建时间和更新时间
     */
    public void refreshIdAndPathDeeply(String path, boolean refreshTime) {
        doRefreshIdAndPathDeeply(this, IdUtil.getSnowflakeNextId(), path, refreshTime);
    }

    private void doRefreshIdAndPathDeeply(UserFileTreeNode node, long id, String path, boolean refreshTime) {
        node.value.setId(id);
        node.value.setPath(path);
        if (refreshTime) {
            node.value.setCreateTime(LocalDateTime.now());
            node.value.setUpdateTime(LocalDateTime.now());
        }
        node.children.forEach(child -> {
            child.value.setParentId(id);
            doRefreshIdAndPathDeeply(child, IdUtil.getSnowflakeNextId(), path + node.value.getName() + "/", refreshTime);
        });
    }

    /**
     * 刷新当前节点的名称以及所有子节点的路径。
     *
     * @param name 节点名称
     */
    public void refreshNameDeeply(String name) {
        value.setName(name);
        children.forEach(child -> child.refreshPathDeeply(value.getPath() + value.getName() + "/"));
    }

    /**
     * 刷新所有节点的用户id
     *
     * @param userId 用户id
     */
    public void refreshUserIdDeeply(long userId) {
        doRefreshUserIdDeeply(this, userId);
    }

    private void doRefreshUserIdDeeply(UserFileTreeNode node, long userId) {
        node.value.setUserId(userId);
        node.getChildren().forEach(child -> doRefreshUserIdDeeply(child, userId));
    }

    /**
     * 收集当前节点树中的所有文件夹。
     *
     * @return 用户文件列表
     */
    public List<UserFile> collectFolder() {
        List<UserFile> folders = new ArrayList<>();
        doCollectFolder(this, folders, true);
        return folders;
    }

    /**
     * 收集当前节点树中的所有文件。
     *
     * @return 用户文件列表
     */
    public List<UserFile> collectAll() {
        List<UserFile> files = new ArrayList<>();
        doCollectFolder(this, files, false);
        return files;
    }

    private void doCollectFolder(UserFileTreeNode node, List<UserFile> folders, boolean onlyFolder) {
        if (onlyFolder && !node.value.getIsFolder()) {
            return;
        }
        folders.add(node.value);
        node.children.forEach(child -> doCollectFolder(child, folders, onlyFolder));
    }
}
