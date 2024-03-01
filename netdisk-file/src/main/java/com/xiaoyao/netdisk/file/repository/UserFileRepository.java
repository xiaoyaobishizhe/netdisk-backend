package com.xiaoyao.netdisk.file.repository;

import com.xiaoyao.netdisk.file.repository.entity.UserFile;

import java.util.List;

public interface UserFileRepository {
    /**
     * 判断文件夹下是否存在指定的名称。
     *
     * @param parentId 文件夹的id，如果为null，则表示在根目录下查找。
     * @param name     文件夹或文件的名称
     * @param userId   用户id
     * @return 如果存在，则返回true，否则返回false。
     */
    boolean isNameExistInParent(Long parentId, String name, long userId);

    /**
     * 判断文件夹下是否存在某个指定的名称。
     *
     * @param parentId 文件夹的id，如果为null，则表示在根目录下查找。
     * @param names    文件夹或文件的名称列表
     * @param userId   用户id
     * @return 如果存在，则返回true，否则返回false。
     */
    boolean isNameExistInParent(Long parentId, List<String> names, long userId);

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
     * @param parentId 文件夹id，如果为null则表示在根目录下查找。
     * @param name     文件或文件夹的名称
     * @param userId   用户id
     * @return 如果指定名称的文件不存在则返回null。
     */
    String getFileIdentifierInParent(Long parentId, String name, long userId);

    /**
     * 获取指定id的文件夹的完整路径（包含目标文件夹名称）。
     *
     * @param folderId 文件夹id
     * @param userId   用户id
     * @return 如果指定id的文件夹不存在则返回null。
     */
    String getFolderFullPath(long folderId, long userId);

    /**
     * 保存用户文件。
     *
     * @param userFile 用户文件
     */
    void save(UserFile userFile);

    /**
     * 保存用户文件。
     *
     * @param userFiles 用户文件列表
     */
    void save(List<UserFile> userFiles);

    /**
     * 获取指定文件夹下的文件列表，如果是根路径则parentId为null。
     *
     * @param parentId   文件夹id
     * @param onlyFolder 是否只列出文件夹
     * @param userId     用户id
     * @return 如果文件夹不存在则返回空列表
     */
    List<UserFile> findListByParentId(Long parentId, boolean onlyFolder, long userId);

    /**
     * 列出所有回收站中的根节点文件。
     *
     * @param userId 用户id
     * @return 用户文件列表
     */
    List<UserFile> listDeletedRootFile(long userId);

    /**
     * 删除所有指定id的文件。
     *
     * @param ids 文件id列表
     */
    void delete(List<Long> ids);

    /**
     * 删除所有回收站中的文件。
     *
     * @param userId 用户id
     */
    void deleteAllDeleted(long userId);

    /**
     * 获取指定路径下的文件夹的id。
     *
     * @param path   文件夹的路径
     * @param name   文件夹的名称
     * @param userId 用户id
     * @return 如果文件夹不存在则返回null
     */
    Long getFolderIdInPathByName(String path, String name, long userId);

    /**
     * 判断指定id的文件是否全都存在。
     *
     * @param ids    文件id列表
     * @param userId 用户id
     * @return 如果文件都存在则返回true，否则返回false。
     */
    boolean isAllExist(List<Long> ids, long userId);

    /**
     * 查询文件树。
     *
     * @param id        根节点id
     * @param isDeleted 是否查询回收站中的节点
     * @param userId    用户id
     * @return 文件树列表，如果没有文件树则返回null
     */
    UserFileTreeNode findUserFileTree(long id, boolean isDeleted, long userId);

    /**
     * 查询文件树。
     *
     * @param ids       根节点id
     * @param isDeleted 是否查询回收站中的节点
     * @param userId    用户id
     * @return 文件树列表，如果没有文件树则返回空列表
     */
    List<UserFileTreeNode> findUserFileTrees(List<Long> ids, boolean isDeleted, long userId);

    /**
     * 更新根节点的父文件夹id，同时更新所有节点的路径。
     *
     * @param trees    文件树列表
     * @param parentId 父文件夹id
     */
    void updateParentIdAndPath(List<UserFileTreeNode> trees, Long parentId);

    /**
     * 将文件树中的所有文件移动到回收站中。
     *
     * @param trees 文件树列表
     */
    void moveToRecycleBin(List<UserFileTreeNode> trees);

    /**
     * 将回收站中的文件树的所有文件恢复到用户空间中。
     *
     * @param trees 文件树列表
     */
    void moveToUserSpace(List<UserFileTreeNode> trees);

    /**
     * 更新根节点的名称和所有子节点的路径。
     *
     * @param tree 文件树
     */
    void updateNameAndPath(UserFileTreeNode tree);
}
