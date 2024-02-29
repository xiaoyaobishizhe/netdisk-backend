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
     * 判断在指定文件夹下是否有某个名称存在。
     *
     * @param parentId 文件夹的id，如果为null，则表示在根目录下查找。
     * @param name     文件夹或文件的名称列表
     * @param userId   用户id
     * @return 如果存在，则返回true，否则返回false。
     */
    boolean isNameExist(Long parentId, List<String> name, long userId);

    /**
     * 判断指定的文件夹是否存在。
     *
     * @param folderId 文件夹的id
     * @param userId   用户id
     * @return 如果id存在并且是文件夹类型则返回true， 否则返回false。
     */
    boolean isFolderExist(long folderId, long userId);

    /**
     * 在指定的文件夹下查找指定名称的文件的唯一标识，如果指定名称的文件是文件夹则返回空字符串。
     *
     * @param folderId 文件夹id，如果为null则表示在根目录下查找。
     * @param name     文件或文件夹的名称
     * @param userId   用户id
     * @return 如果指定名称的文件不存在则返回null。
     */
    String getIdentifier(Long folderId, String name, long userId);

    /**
     * 获取指定id的文件夹的路径（包含目标文件夹名称）。
     *
     * @param folderId 文件夹id
     * @param userId   用户id
     * @return 如果指定id的文件夹不存在则返回null。
     */
    String getPathByFolderId(long folderId, long userId);

    void save(UserFile userFile);

    UserFile findIsFolderAndParentIdAndNameById(long id, long userId);

    void update(UserFile file);

    /**
     * 获取指定文件夹下的文件列表，如果是根路径则parentId为null。
     *
     * @param parentId   文件夹id
     * @param onlyFolder 是否只列出文件夹
     * @param userId     用户id
     * @return 如果文件夹不存在则返回空列表
     */
    List<UserFile> findListByParentId(Long parentId, boolean onlyFolder, long userId);

    void updatePathByParentId(String path, long parentId, long userId);

    List<UserFile> listDeleted(long userId);

    void updateParentId(long fid, Long parentId, boolean isDeleted, long userId);

    void updateIsDeleted(List<Long> ids, boolean isDeleted, long userId);

    void delete(List<Long> ids, long userId);

    void deleteAllDeleted(long userId);

    /**
     * 查找文件树，并且使用指定的文件名来匹配所有的子文件。
     *
     * @param id      文件id
     * @param oldName 原先的文件名
     * @param userId  用户id
     * @return 如果文件不存在则返回null
     */
    FileTreeNode findFileTree(long id, String oldName, long userId);

    /**
     * 查找文件树。
     *
     * @param id     文件id
     * @param userId 用户id
     * @return 如果文件不存在则返回null
     */
    FileTreeNode findFileTree(long id, long userId);

    /**
     * 查找回收站中的文件树。
     *
     * @param id     文件id
     * @param userId 用户id
     * @return 如果文件不存在则返回null
     */
    FileTreeNode findDeletedFileTree(long id, long userId);

    /**
     * 获取指定路径下的文件夹的id。
     *
     * @param path   文件夹的路径
     * @param name   文件夹的名称
     * @param userId 用户id
     * @return 如果文件夹不存在则返回null
     */
    Long getFolderId(String path, String name, long userId);

    /**
     * 判断指定的文件是否全都存在。
     *
     * @param fileList 文件id列表
     * @param userId   用户id
     * @return 如果文件都存在则返回true，否则返回false。
     */
    boolean isAllExist(List<Long> fileList, long userId);

    List<UserFile> findListByIds(List<Long> ids, long userId);

    List<UserFile> findListByPaths(List<String> paths, long userId);

    void banchSave(List<UserFile> userFiles);
}
