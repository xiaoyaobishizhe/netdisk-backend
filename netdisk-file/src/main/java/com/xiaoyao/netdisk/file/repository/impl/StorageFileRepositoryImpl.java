package com.xiaoyao.netdisk.file.repository.impl;

import com.xiaoyao.netdisk.file.repository.StorageFileRepository;
import com.xiaoyao.netdisk.file.repository.mapper.StorageFileMapper;
import org.springframework.stereotype.Repository;

@Repository
public class StorageFileRepositoryImpl implements StorageFileRepository {
    private final StorageFileMapper storageFileMapper;

    public StorageFileRepositoryImpl(StorageFileMapper storageFileMapper) {
        this.storageFileMapper = storageFileMapper;
    }
}
