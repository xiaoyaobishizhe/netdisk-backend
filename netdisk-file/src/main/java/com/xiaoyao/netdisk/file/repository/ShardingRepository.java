package com.xiaoyao.netdisk.file.repository;

import com.xiaoyao.netdisk.file.repository.entity.Sharding;

public interface ShardingRepository {
    Sharding findByIdentifier(String identifier, long userId);

    void save(Sharding sharding);
}