package com.xiaoyao.netdisk.file.repository.impl;

import com.xiaoyao.netdisk.file.repository.ShardingRepository;
import com.xiaoyao.netdisk.file.repository.entity.Sharding;
import com.xiaoyao.netdisk.file.repository.mapper.ShardingMapper;
import org.springframework.stereotype.Repository;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;
import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaUpdate;

@Repository
public class ShardingRepositoryImpl implements ShardingRepository {
    private final ShardingMapper shardingMapper;

    public ShardingRepositoryImpl(ShardingMapper shardingMapper) {
        this.shardingMapper = shardingMapper;
    }

    @Override
    public Sharding findProgressByIdentifier(String identifier, long userId) {
        return shardingMapper.selectOne(lambdaQuery(Sharding.class)
                .select(Sharding::getChunkSize,
                        Sharding::getCurrentChunk,
                        Sharding::getTotalChunk)
                .eq(Sharding::getUserId, userId)
                .eq(Sharding::getIdentifier, identifier));
    }

    @Override
    public Sharding findByIdentifier(String identifier, long userId) {
        return shardingMapper.selectOne(lambdaQuery(Sharding.class)
                .select(Sharding::getChunkSize,
                        Sharding::getFilename,
                        Sharding::getParentId,
                        Sharding::getSize,
                        Sharding::getCurrentChunk,
                        Sharding::getTotalChunk)
                .eq(Sharding::getUserId, userId)
                .eq(Sharding::getIdentifier, identifier));
    }

    @Override
    public void save(Sharding sharding) {
        shardingMapper.insert(sharding);
    }

    @Override
    public void delete(String identifier) {
        shardingMapper.delete(lambdaQuery(Sharding.class)
                .eq(Sharding::getIdentifier, identifier));
    }

    @Override
    public boolean incrementChunkNumber(String identifier, int chunkNumber, long userId) {
        return shardingMapper.update(null, lambdaUpdate(Sharding.class)
                .set(Sharding::getCurrentChunk, chunkNumber)
                .eq(Sharding::getIdentifier, identifier)
                .eq(Sharding::getUserId, userId)
                .eq(Sharding::getCurrentChunk, chunkNumber - 1)) == 1;
    }
}
