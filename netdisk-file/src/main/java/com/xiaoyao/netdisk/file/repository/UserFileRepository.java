package com.xiaoyao.netdisk.file.repository;

import com.xiaoyao.netdisk.file.repository.entity.UserFile;

public interface UserFileRepository {
    /**
     * 判断文件夹是否存在。
     */
    boolean isExistFolder(Long parentId, String folderName);

    /**
     * 判断在指定路径下名称是否存在。
     */
    boolean isExistName(Long parentId, String name, long userId);

    void save(UserFile userFile);

    UserFile findNameAndParentIdById(long fileId, long userId);

    void update(UserFile file);
}
