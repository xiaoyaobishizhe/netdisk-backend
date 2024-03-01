package com.xiaoyao.netdisk.file.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaoyao.netdisk.common.exception.E;
import com.xiaoyao.netdisk.common.exception.NetdiskException;
import com.xiaoyao.netdisk.common.web.interceptor.TokenInterceptor;
import com.xiaoyao.netdisk.file.dto.FileListDTO;
import com.xiaoyao.netdisk.file.dto.FolderListDTO;
import com.xiaoyao.netdisk.file.repository.FileTreeNode;
import com.xiaoyao.netdisk.file.repository.UserFileRepository;
import com.xiaoyao.netdisk.file.repository.UserFileTreeNode;
import com.xiaoyao.netdisk.file.repository.entity.UserFile;
import com.xiaoyao.netdisk.file.service.UserFileService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

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

    @Override
    public void copy(List<String> ids, String parentId) {
        long userId = TokenInterceptor.USER_ID.get();
        Long pid = StrUtil.isBlank(parentId) ? null : Long.parseLong(parentId);

        // 找到最顶层文件
        List<UserFile> rootFiles = userFileRepository.findListByIds(
                ids.stream().map(Long::parseLong).distinct().toList(), userId);

        // 确保所有名称在目标文件夹下都不存在
        List<String> names = new ArrayList<>();
        rootFiles.forEach(file -> names.add(file.getName()));
        if (userFileRepository.isNameExist(pid, names, userId)) {
            throw new NetdiskException(E.FILE_NAME_ALREADY_EXIST);
        }

        // 向下搜索所有子文件（深拷贝）
        List<String> paths = new ArrayList<>();
        rootFiles.forEach(file -> {
            if (file.getIsFolder()) {
                paths.add(file.getPath() + file.getName() + "/");
            }
        });
        Map<Long, List<UserFile>> children = new HashMap<>();    // key为父文件夹id，value为子文件
        if (!paths.isEmpty()) {
            userFileRepository.findListByPaths(paths, userId).forEach(userFile ->
                    children.computeIfAbsent(userFile.getParentId(), id -> new ArrayList<>()).add(userFile));
        }

        // 重新组装文件从属关系
        String path = pid == null ? "/" : userFileRepository.getPathByFolderId(pid, userId);
        List<UserFile> userFiles = new ArrayList<>();
        Queue<UserFile> taskQueue = new ArrayDeque<>();   // 通过栈来代替递归
        rootFiles.forEach(rootFile -> {
            if (rootFile.getIsFolder()) {
                rootFile.setPath(path);
                rootFile.setParentId(pid);
                taskQueue.add(rootFile);
            } else {
                rootFile.setId(IdUtil.getSnowflakeNextId());
                rootFile.setPath(path);
                rootFile.setParentId(pid);
                rootFile.setCreateTime(LocalDateTime.now());
                rootFile.setUpdateTime(LocalDateTime.now());
                userFiles.add(rootFile);
            }
        });
        UserFile current;
        while ((current = taskQueue.poll()) != null) {
            List<UserFile> childTask = children.get(current.getId());
            current.setId(IdUtil.getSnowflakeNextId());
            if (childTask != null) {
                for (UserFile child : childTask) {
                    child.setPath(current.getPath() + current.getName() + "/");
                    child.setParentId(current.getId());
                    taskQueue.add(child);
                }
            }
            current.setCreateTime(LocalDateTime.now());
            current.setUpdateTime(LocalDateTime.now());
            userFiles.add(current);
        }

        // 批量插入
        userFileRepository.banchSave(userFiles);
    }

    @Override
    public void move(List<String> ids, String parentId) {
        long userId = TokenInterceptor.USER_ID.get();
        Long pid = StrUtil.isBlank(parentId) ? null : Long.parseLong(parentId);

        List<UserFileTreeNode> trees = userFileRepository.findUserFileTreesByIds(ids.stream().map(Long::parseLong).toList(), userId);

        // 确保文件都在同一路径下
        if (trees.stream()
                .map(node -> node.getValue().getParentId() == null ? 0 : node.getValue().getParentId())
                .distinct()
                .count() != 1) {
            throw new NetdiskException(E.FILE_NOT_SAME_PATH);
        }

        // 不能将文件移动到自身或其子目录下
        trees.forEach(tree -> {
            tree.collectFolder().forEach(folder -> {
                if (folder.getId().equals(pid)) {
                    throw new NetdiskException(E.CANNOT_MOVE_TO_SELF_OR_CHILD);
                }
            });
        });

        // 确保所有名称在目标文件夹下都不存在
        if (userFileRepository.isNameExist(pid, trees.stream().map(node -> node.getValue().getName()).toList(), userId)) {
            throw new NetdiskException(E.FILE_NAME_ALREADY_EXIST);
        }

        // 重新组装文件从属关系
        String path = pid == null ? "/" : userFileRepository.getPathByFolderId(pid, userId);
        trees.forEach(tree -> {
            tree.getValue().setParentId(pid);
            tree.refreshPathDeeply(path);
        });

        userFileRepository.updateParentIdAndPath(trees, pid);
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
                StrUtil.isBlank(parentId) ? null : Long.parseLong(parentId), false, userId);
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

    @Override
    public FolderListDTO listFolders(String parentId) {
        long userId = TokenInterceptor.USER_ID.get();
        List<UserFile> files = userFileRepository.findListByParentId(
                StrUtil.isBlank(parentId) ? null : Long.parseLong(parentId), true, userId);
        FolderListDTO dto = new FolderListDTO();
        List<FolderListDTO.Item> items = new ArrayList<>();
        dto.setFolders(items);
        for (UserFile file : files) {
            FolderListDTO.Item item = new FolderListDTO.Item();
            item.setId(file.getId().toString());
            item.setName(file.getName());
            items.add(item);
        }
        return dto;
    }
}
