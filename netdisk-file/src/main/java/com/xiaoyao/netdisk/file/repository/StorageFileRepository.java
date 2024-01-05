package com.xiaoyao.netdisk.file.repository;

import com.xiaoyao.netdisk.file.repository.entity.StorageFile;

public interface StorageFileRepository {
    void save(StorageFile storageFile);
}
