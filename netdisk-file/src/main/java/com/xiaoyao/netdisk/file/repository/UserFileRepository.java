package com.xiaoyao.netdisk.file.repository;

import com.xiaoyao.netdisk.file.entity.UserFile;

public interface UserFileRepository {
    boolean isExistFolder(Long parentId, String folderName);

    void save(UserFile userFile);
}
