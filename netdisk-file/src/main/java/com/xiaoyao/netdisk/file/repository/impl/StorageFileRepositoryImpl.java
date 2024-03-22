package com.xiaoyao.netdisk.file.repository.impl;

import com.xiaoyao.netdisk.file.repository.StorageFileRepository;
import com.xiaoyao.netdisk.file.repository.entity.StorageFile;
import com.xiaoyao.netdisk.file.repository.mapper.StorageFileMapper;
import org.springframework.stereotype.Repository;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;

@Repository
public class StorageFileRepositoryImpl implements StorageFileRepository {
    private final StorageFileMapper storageFileMapper;

    public StorageFileRepositoryImpl(StorageFileMapper storageFileMapper) {
        this.storageFileMapper = storageFileMapper;
    }

    @Override
    public void save(StorageFile storageFile) {
        storageFileMapper.insert(storageFile);
    }

    @Override
    public StorageFile findIdAndSizeByIdentifier(String identifier) {
        return storageFileMapper.selectOne(lambdaQuery(StorageFile.class)
                .select(StorageFile::getId,
                        StorageFile::getSize)
                .eq(StorageFile::getIdentifier, identifier));
    }

    @Override
    public String getPath(Long id) {
        return storageFileMapper.selectOne(lambdaQuery(StorageFile.class)
                .select(StorageFile::getPath)
                .eq(StorageFile::getId, id)).getPath();
    }
}
