package com.xiaoyao.netdisk.file.repository;

import com.xiaoyao.netdisk.file.repository.entity.UserFile;

import java.util.List;

public interface UserFileRepository {
    /**
     * 判断在指定文件夹下名称是否存在。
     *
     * @param parentId 文件夹的id，如果为null，则表示在根目录下查找。
     * @param name     文件夹或文件的名称
     * @param userId   用户id
     * @return 如果存在，则返回true，否则返回false。
     */
    boolean isNameExist(Long parentId, String name, long userId);

    /**
     * 判断指定的文件夹是否存在。
     *
     * @param folderId 文件夹的id
     * @param userId   用户id
     * @return 如果id存在并且是文件夹类型则返回true， 否则返回false。
     */
    boolean isFolderExist(long folderId, long userId);

    /**
     * 在指定的文件夹下，查找指定名称的文件或文件夹，并且如果是文件的话则获取文件的唯一标识。
     *
     * @param folderId 文件夹id，如果为null则表示在根目录下查找。
     * @param name     文件或文件夹的名称
     * @param userId   用户id
     * @return 如果指定名称的文件或文件夹不存在则返回null。
     */
    UserFile findIdentifierById(Long folderId, String name, long userId);

    String findFolderPathById(long id, long userId);

    void save(UserFile userFile);

    UserFile findIsFolderById(long id, long userId);

    void update(UserFile file);

    List<UserFile> findListByParentId(Long parentId, long userId);

    void updatePathByParentId(String path, long parentId, long userId);

    FileTreeNode findFileTreeById(long id, long userId, String oldName);
}
