package com.xiaoyao.netdisk.file.service.impl;

import cn.hutool.core.date.DateUtil;
import com.xiaoyao.netdisk.common.web.interceptor.TokenInterceptor;
import com.xiaoyao.netdisk.file.dto.ListRecycleBinDTO;
import com.xiaoyao.netdisk.file.repository.UserFileRepository;
import com.xiaoyao.netdisk.file.repository.UserFileTreeNode;
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
        List<UserFile> userFiles = userFileRepository.listDeletedRootFile(TokenInterceptor.USER_ID.get());

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
    public void delete(List<String> ids) {
        userFileRepository.moveToRecycleBin(
                userFileRepository.findUserFileTrees(
                        ids.stream().map(Long::parseLong).toList(),
                        false,
                        TokenInterceptor.USER_ID.get()));
    }

    @Override
    public void remove(List<String> ids) {
        long userId = TokenInterceptor.USER_ID.get();
        List<UserFileTreeNode> trees = userFileRepository.findUserFileTrees(
                ids.stream().map(Long::parseLong).toList(), true, userId);
        userFileRepository.delete(trees.stream()
                .flatMap(tree -> tree.collectAll().stream())
                .map(UserFile::getId)
                .toList());
    }

    @Override
    public void restore(List<String> ids) {
        long userId = TokenInterceptor.USER_ID.get();

        List<UserFileTreeNode> trees = userFileRepository.findUserFileTrees(
                ids.stream().map(Long::parseLong).toList(), true, userId);

        // 确保父文件夹存在，如果不存在则自动创建，同时还需要确保名称在路径下不存在。
        List<UserFileTreeNode> needMove = new ArrayList<>();
        trees.forEach(tree -> {
            tree.getValue().setParentId(getFolderId(tree.getValue().getPath(), userId));
            if (!userFileRepository.isNameExistInParent(tree.getValue().getParentId(), tree.getValue().getName(), userId)) {
                needMove.add(tree);
            }
        });

        userFileRepository.moveToUserSpace(needMove);
    }

    @Override
    public void clear() {
        userFileRepository.deleteAllDeleted(TokenInterceptor.USER_ID.get());
    }

    private Long getFolderId(String path, long userId) {
        if (path.equals("/")) {
            return null;
        }

        path = path.substring(0, path.length() - 1);
        String pPath = path.substring(0, path.lastIndexOf('/') + 1);
        String name = path.substring(path.lastIndexOf('/') + 1);
        Long folderId = userFileRepository.getFolderIdInPathByName(pPath, name, userId);
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
