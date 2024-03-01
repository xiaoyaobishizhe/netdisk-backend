package com.xiaoyao.netdisk.file.repository;

import cn.hutool.core.util.IdUtil;
import com.xiaoyao.netdisk.file.repository.entity.UserFile;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserFileTreeNode {
    private UserFile value;
    private List<UserFileTreeNode> children = new ArrayList<>();

    public UserFileTreeNode(UserFile value) {
        this.value = value;
    }

    public UserFileTreeNode refreshIdDeeply(long id) {
        doRefreshIdDeeply(this, id);
        return this;
    }

    private void doRefreshIdDeeply(UserFileTreeNode node, long id) {
        node.value.setId(id);
        node.children.forEach(child -> {
            child.value.setParentId(id);
            doRefreshIdDeeply(child, IdUtil.getSnowflakeNextId());
        });
    }

    public void refreshPathDeeply(String path) {
        doRefreshPathDeeply(this, path);
    }

    private void doRefreshPathDeeply(UserFileTreeNode node, String path) {
        node.value.setPath(path);
        node.children.forEach(child -> doRefreshPathDeeply(child, node.value.getPath() + node.value.getName() + "/"));
    }

    public UserFileTreeNode refreshIdAndPathDeeply(long id, String path) {
        doRefreshIdAndPathDeeply(this, id, path);
        return this;
    }

    private void doRefreshIdAndPathDeeply(UserFileTreeNode node, long id, String path) {
        node.value.setId(id);
        node.value.setPath(path);
        node.children.forEach(child -> {
            child.value.setParentId(id);
            doRefreshIdAndPathDeeply(child, IdUtil.getSnowflakeNextId(), path + node.value.getName() + "/");
        });
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
