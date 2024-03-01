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

    public void refreshPathDeeply(String path) {
        doRefreshPathDeeply(this, path);
    }

    private void doRefreshPathDeeply(UserFileTreeNode node, String path) {
        node.value.setPath(path);
        node.children.forEach(child -> doRefreshPathDeeply(child, node.value.getPath() + node.value.getName() + "/"));
    }

    public void refreshIdAndPathDeeply(long id, String path, boolean refreshTime) {
        doRefreshIdAndPathDeeply(this, id, path, refreshTime);
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

    public void refreshNameDeeply(String name) {
        value.setName(name);
        children.forEach(child -> child.refreshPathDeeply(value.getPath() + value.getName() + "/"));
    }

    public List<UserFile> collectFolder() {
        List<UserFile> folders = new ArrayList<>();
        doCollectFolder(this, folders, true);
        return folders;
    }

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
