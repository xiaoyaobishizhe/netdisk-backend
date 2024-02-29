package com.xiaoyao.netdisk.file.service;

import com.xiaoyao.netdisk.file.dto.FileListDTO;
import com.xiaoyao.netdisk.file.dto.FolderListDTO;

import java.util.List;

public interface UserFileService {
    /**
     * 获取指定文件夹下的文件列表，如果parentId为空，则获取根目录下的文件列表。
     *
     * @param parentId 父文件夹id
     * @return 文件列表
     */
    FileListDTO list(String parentId);

    /**
     * 获取指定文件夹下的子文件夹列表，如果parentId为空，则获取根目录下的文件夹列表。
     *
     * @param parentId 父文件夹id
     * @return 文件夹列表
     */
    FolderListDTO listFolders(String parentId);

    /**
     * 在指定的文件夹下创建文件夹，如果parentId为空，则在根目录下创建文件夹。
     *
     * @param parentId   父文件夹id
     * @param folderName 文件夹名称
     */
    void createFolder(String parentId, String folderName);

    /**
     * 重命名文件或文件夹，如果新名称已存在则会自动生成一个不存在的名称。
     *
     * @param fileId 文件或文件夹id
     * @param name   新名称
     */
    void rename(String fileId, String name);

    /**
     * 复制文件或文件夹到指定的文件夹下。
     *
     * @param ids      要复制的文件或文件夹
     * @param parentId 父文件夹id，如果为根路径则值为null
     */
    void copy(List<String> ids, String parentId);
}
