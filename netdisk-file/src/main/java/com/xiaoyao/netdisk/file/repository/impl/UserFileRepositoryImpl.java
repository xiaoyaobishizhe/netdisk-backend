package com.xiaoyao.netdisk.file.repository.impl;

import com.xiaoyao.netdisk.file.repository.UserFileRepository;
import com.xiaoyao.netdisk.file.repository.mapper.UserFileMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserFileRepositoryImpl implements UserFileRepository {
    private final UserFileMapper userFileMapper;

    public UserFileRepositoryImpl(UserFileMapper userFileMapper) {
        this.userFileMapper = userFileMapper;
    }
}
