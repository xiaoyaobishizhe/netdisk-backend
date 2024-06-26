package com.xiaoyao.netdisk.file.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaoyao.netdisk.common.exception.E;
import com.xiaoyao.netdisk.common.exception.NetdiskException;
import com.xiaoyao.netdisk.common.web.interceptor.TokenInterceptor;
import com.xiaoyao.netdisk.file.dto.DownloadDTO;
import com.xiaoyao.netdisk.file.dto.FileListDTO;
import com.xiaoyao.netdisk.file.dto.FolderListDTO;
import com.xiaoyao.netdisk.file.properties.MinioProperties;
import com.xiaoyao.netdisk.file.repository.StorageFileRepository;
import com.xiaoyao.netdisk.file.repository.UserFileRepository;
import com.xiaoyao.netdisk.file.repository.UserFileTreeNode;
import com.xiaoyao.netdisk.file.repository.entity.UserFile;
import com.xiaoyao.netdisk.file.service.UserFileService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserFileServiceImpl implements UserFileService {
    private final UserFileRepository userFileRepository;
    private final StorageFileRepository storageFileRepository;
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public UserFileServiceImpl(UserFileRepository userFileRepository, StorageFileRepository storageFileRepository,
                               MinioClient minioClient, MinioProperties minioProperties) {
        this.userFileRepository = userFileRepository;
        this.storageFileRepository = storageFileRepository;
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    @Override
    public FileListDTO list(String parentId, boolean isSelf) {
        Long userId = isSelf ? TokenInterceptor.USER_ID.get() : null;
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
    public FileListDTO list(List<Long> ids) {
        List<UserFile> files = userFileRepository.findListByIds(ids);
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

    @Override
    public void createFolder(String parentId, String folderName) {
        checkName(folderName);
        long userId = TokenInterceptor.USER_ID.get();
        Long pid = StrUtil.isBlank(parentId) ? null : Long.parseLong(parentId);

        if (pid != null && !userFileRepository.isFolderExist(pid, userId)) {
            throw new NetdiskException(E.PARENT_FOLDER_NOT_EXIST);
        } else if (userFileRepository.isNameExistInParent(pid, folderName, userId)) {
            throw new NetdiskException(E.FILE_NAME_ALREADY_EXIST);
        }

        UserFile folder = new UserFile();
        folder.setUserId(userId);
        folder.setPath(pid == null ? "/" : userFileRepository.getFolderFullPath(pid, userId));
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
    public void rename(String id, String name) {
        checkName(name);
        long userId = TokenInterceptor.USER_ID.get();

        UserFileTreeNode tree = userFileRepository.findUserFileTree(Long.parseLong(id), false, userId);

        if (tree == null) {
            throw new NetdiskException(E.FILE_NOT_EXIST);
        } else if (userFileRepository.isNameExistInParent(tree.getValue().getParentId(), name, userId)) {
            throw new NetdiskException(E.FILE_NAME_ALREADY_EXIST);
        }

        tree.refreshNameDeeply(name);
        userFileRepository.updateNameAndPath(tree);
    }

    @Override
    public void copy(List<String> ids, String parentId) {
        long userId = TokenInterceptor.USER_ID.get();
        Long pid = StrUtil.isBlank(parentId) ? null : Long.parseLong(parentId);

        List<UserFileTreeNode> trees = userFileRepository.findUserFileTrees(
                ids.stream().map(Long::parseLong).toList(), false, userId);

        // 确保文件都在同一路径下
        if (trees.stream()
                .map(node -> node.getValue().getParentId() == null ? 0 : node.getValue().getParentId())
                .distinct()
                .count() != 1) {
            throw new NetdiskException(E.FILE_NOT_SAME_PATH);
        }

        // 不能将文件复制到自身或其子目录下
        trees.forEach(tree -> {
            tree.collectFolder().forEach(folder -> {
                if (folder.getId().equals(pid)) {
                    throw new NetdiskException(E.CANNOT_MOVE_TO_SELF_OR_CHILD);
                }
            });
        });

        // 确保所有名称在目标文件夹下都不存在
        if (userFileRepository.isNameExistInParent(pid, trees.stream().map(node -> node.getValue().getName()).toList(), userId)) {
            throw new NetdiskException(E.FILE_NAME_ALREADY_EXIST);
        }

        // 重新组装文件从属关系
        String path = pid == null ? "/" : userFileRepository.getFolderFullPath(pid, userId);
        trees.forEach(tree -> {
            tree.getValue().setParentId(pid);
            tree.refreshIdAndPathDeeply(path, true);
        });

        userFileRepository.save(trees.stream().flatMap(tree -> tree.collectAll().stream()).toList());
    }

    @Override
    public void move(List<String> ids, String parentId) {
        long userId = TokenInterceptor.USER_ID.get();
        Long pid = StrUtil.isBlank(parentId) ? null : Long.parseLong(parentId);

        List<UserFileTreeNode> trees = userFileRepository.findUserFileTrees(
                ids.stream().map(Long::parseLong).toList(), false, userId);

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
        if (userFileRepository.isNameExistInParent(pid, trees.stream().map(node -> node.getValue().getName()).toList(), userId)) {
            throw new NetdiskException(E.FILE_NAME_ALREADY_EXIST);
        }

        // 重新组装文件从属关系
        String path = pid == null ? "/" : userFileRepository.getFolderFullPath(pid, userId);
        trees.forEach(tree -> {
            tree.getValue().setParentId(pid);
            tree.refreshPathDeeply(path);
        });

        userFileRepository.updateParentIdAndPath(trees, pid);
    }

    @Override
    public DownloadDTO download(String id) {
        long userId = TokenInterceptor.USER_ID.get();
        UserFile userFile = userFileRepository.getStorageFileIdAndName(Long.parseLong(id), userId);
        String path = storageFileRepository.getPath(userFile.getStorageFileId());
        try {
            DownloadDTO dto = new DownloadDTO();
            dto.setUrl(minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(minioProperties.getBucket()).object("/" + path)
                    .method(Method.GET)
                    .expiry(1, TimeUnit.DAYS)
                    .build()));
            dto.setFilename(userFile.getName());
            return dto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
