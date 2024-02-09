package com.xiaoyao.netdisk.file.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaoyao.netdisk.common.exception.E;
import com.xiaoyao.netdisk.common.exception.NetdiskException;
import com.xiaoyao.netdisk.common.web.interceptor.TokenInterceptor;
import com.xiaoyao.netdisk.file.dto.FileListDTO;
import com.xiaoyao.netdisk.file.repository.FileTreeNode;
import com.xiaoyao.netdisk.file.repository.UserFileRepository;
import com.xiaoyao.netdisk.file.repository.entity.UserFile;
import com.xiaoyao.netdisk.file.service.UserFileService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserFileServiceImpl implements UserFileService {
    private final UserFileRepository userFileRepository;

    public UserFileServiceImpl(UserFileRepository userFileRepository) {
        this.userFileRepository = userFileRepository;
    }

    @Override
    public void createFolder(String parentId, String folderName) {
        checkName(folderName);
        long userId = TokenInterceptor.USER_ID.get();
        Long pid = StrUtil.isBlank(parentId) ? null : Long.parseLong(parentId);
        if (pid != null && !userFileRepository.isFolderExist(pid, userId)) {
            // 父文件夹不存在
            throw new NetdiskException(E.PARENT_FOLDER_NOT_EXIST);
        }
        if (userFileRepository.isNameExist(pid, folderName, userId)) {
            // 存在同名文件或文件夹，更改文件夹名称。
            folderName = newFolderName(folderName);
        }

        UserFile folder = new UserFile();
        folder.setUserId(userId);
        folder.setPath(pid == null ? "/" : userFileRepository.getPathByFolderId(pid, userId));
        folder.setParentId(pid);
        folder.setName(folderName);
        folder.setIsFolder(true);
        folder.setCreateTime(LocalDateTime.now());
        folder.setUpdateTime(LocalDateTime.now());
        userFileRepository.save(folder);
    }

    private void checkName(String name) {
        if (FileNameUtil.containsInvalid(name)) {
            throw new NetdiskException(E.FILE_NAME_INVALID);
        }
    }

    @Override
    public void rename(String fileId, String name) {
        checkName(name);
        long fid = Long.parseLong(fileId);
        long userId = TokenInterceptor.USER_ID.get();
        UserFile userFile = userFileRepository.findIsFolderAndParentIdAndNameById(fid, userId);
        if (userFile == null) {
            // 文件不存在
            throw new NetdiskException(E.FILE_NOT_EXIST);
        } else if (userFileRepository.isNameExist(userFile.getParentId(), name, userId)) {
            // 存在同名文件或文件夹，更改名称。
            if (userFile.getIsFolder()) {
                name = newFolderName(name);
            } else {
                name = newFileName(userFile.getParentId(), name, userId);
            }
        }

        boolean isFolder = userFile.getIsFolder();
        String oldName = userFile.getName();
        userFile = new UserFile();
        userFile.setId(fid);
        userFile.setName(name);
        userFile.setUpdateTime(LocalDateTime.now());
        userFileRepository.update(userFile);

        // 修改文件夹名称时，需要修改文件夹下的所有文件的路径。
        if (isFolder) {
            FileTreeNode node = userFileRepository.findFileTree(fid, oldName, userId);
            refreshChildPath(node, node.getPath() + node.getName() + "/");
        }
    }

    private void refreshChildPath(FileTreeNode node, String path) {
        List<FileTreeNode> children = node.getChildren();
        long userId = TokenInterceptor.USER_ID.get();
        if (!children.isEmpty()) {
            children.forEach(child -> child.setPath(path));
            userFileRepository.updatePathByParentId(path, node.getId(), userId);
            children.forEach(child -> {
                if (child.isFolder()) {
                    refreshChildPath(child, path + child.getName() + "/");
                }
            });
        }
    }

    private String newFolderName(String filename) {
        return filename + "_" + DateUtil.format(LocalDateTime.now(), "yyyyMMdd_HHmmss");
    }

    private String newFileName(Long parentId, String filename, long userId) {

        int i = 1;
        do {
            filename = StrUtil.format("{}({})", filename, i);
            i++;
        } while (userFileRepository.isNameExist(parentId, filename, userId));
        return filename;
    }

    @Override
    public FileListDTO list(String parentId) {
        long userId = TokenInterceptor.USER_ID.get();
        List<UserFile> files = userFileRepository.findListByParentId(
                StrUtil.isBlank(parentId) ? null : Long.parseLong(parentId), userId);
        FileListDTO dto = new FileListDTO();
        List<FileListDTO.Item> items = new ArrayList<>();
        dto.setFiles(items);
        for (UserFile file : files) {
            FileListDTO.Item item = new FileListDTO.Item();
            item.setId(file.getId().toString());
            item.setName(file.getName());
            item.setFolder(file.getIsFolder());
            item.setSize(file.getSize());
            item.setUpdateTime(DateUtil.format(file.getUpdateTime(), "yyyy-MM-dd HH:mm:ss"));
            items.add(item);
        }
        return dto;
    }
}
