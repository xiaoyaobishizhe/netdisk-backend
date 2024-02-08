package com.xiaoyao.netdisk.file.service.impl;

import cn.hutool.core.date.DateUtil;
import com.xiaoyao.netdisk.common.exception.E;
import com.xiaoyao.netdisk.common.exception.NetdiskException;
import com.xiaoyao.netdisk.common.web.interceptor.TokenInterceptor;
import com.xiaoyao.netdisk.file.dto.ListRecycleBinDTO;
import com.xiaoyao.netdisk.file.repository.FileTreeNode;
import com.xiaoyao.netdisk.file.repository.UserFileRepository;
import com.xiaoyao.netdisk.file.repository.entity.UserFile;
import com.xiaoyao.netdisk.file.service.RecycleBinService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecycleBinServiceImpl implements RecycleBinService {
    private final UserFileRepository userFileRepository;

    public RecycleBinServiceImpl(UserFileRepository userFileRepository) {
        this.userFileRepository = userFileRepository;
    }

    @Override
    public ListRecycleBinDTO list() {
        List<UserFile> userFiles = userFileRepository.listDeleted(TokenInterceptor.USER_ID.get());

        ListRecycleBinDTO dto = new ListRecycleBinDTO();
        List<ListRecycleBinDTO.Item> items = new ArrayList<>();
        dto.setItems(items);
        userFiles.forEach(userFile -> {
            ListRecycleBinDTO.Item item = new ListRecycleBinDTO.Item();
            item.setId(String.valueOf(userFile.getId()));
            item.setName(userFile.getName());
            item.setFolder(userFile.getIsFolder());
            item.setDeleteTime(DateUtil.format(userFile.getDeleteTime(), "yyyy-MM-dd HH:mm:ss"));
            items.add(item);
        });
        return dto;
    }

    @Override
    public void delete(String id) {
        long userId = TokenInterceptor.USER_ID.get();
        long fid = Long.parseLong(id);
        FileTreeNode node = userFileRepository.findFileTree(fid, userId);
        if (node == null) {
            throw new NetdiskException(E.FILE_NOT_EXIST);
        }
        userFileRepository.updateParentId(fid, null, false, userId);
        List<Long> ids = new ArrayList<>();
        ids.add(fid);
        getChildrenIds(node, ids);
        userFileRepository.updateIsDeleted(ids, true, userId);
    }

    private void getChildrenIds(FileTreeNode node, List<Long> ids) {
        if (!node.getChildren().isEmpty()) {
            for (FileTreeNode child : node.getChildren()) {
                ids.add(child.getId());
                if (node.isFolder()) {
                    getChildrenIds(child, ids);
                }
            }
        }
    }

    @Override
    public void remove(String id) {
        long userId = TokenInterceptor.USER_ID.get();
        long fid = Long.parseLong(id);
        FileTreeNode node = userFileRepository.findDeletedFileTree(fid, userId);
        if (node == null) {
            throw new NetdiskException(E.FILE_NOT_EXIST);
        }
        List<Long> ids = new ArrayList<>();
        ids.add(fid);
        getChildrenIds(node, ids);
        userFileRepository.delete(ids, userId);
    }

    @Override
    public void restore(String id) {
        long userId = TokenInterceptor.USER_ID.get();
        long fid = Long.parseLong(id);
        FileTreeNode node = userFileRepository.findDeletedFileTree(fid, userId);
        if (node == null) {
            throw new NetdiskException(E.FILE_NOT_EXIST);
        }
        userFileRepository.updateParentId(fid, getFolderId(node.getPath(), userId), true, userId);
        List<Long> ids = new ArrayList<>();
        ids.add(fid);
        getChildrenIds(node, ids);
        userFileRepository.updateIsDeleted(ids, false, userId);
    }

    private Long getFolderId(String path, long userId) {
        if (path.equals("/")) {
            return null;
        }

        path = path.substring(0, path.length() - 1);
        String pPath = path.substring(0, path.lastIndexOf('/') + 1);
        String name = path.substring(path.lastIndexOf('/') + 1);
        Long folderId = userFileRepository.getFolderId(pPath, name, userId);
        if (folderId != null) {
            return folderId;
        }

        // 文件夹不存在，需要创建。
        UserFile userFile = new UserFile();
        userFile.setUserId(userId);
        userFile.setPath(pPath);
        userFile.setParentId(getFolderId(pPath, userId));
        userFile.setName(name);
        userFile.setIsFolder(true);
        userFile.setCreateTime(LocalDateTime.now());
        userFile.setUpdateTime(LocalDateTime.now());
        userFileRepository.save(userFile);
        return userFile.getId();
    }
}
