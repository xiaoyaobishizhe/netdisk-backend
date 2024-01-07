package com.xiaoyao.netdisk.file.repository;

import com.xiaoyao.netdisk.file.repository.entity.Sharding;

public interface ShardingRepository {
    /**
     * 查询分片任务的分块进度。
     *
     * @param identifier 文件的唯一标识
     * @param userId     用户id
     * @return 如果分片任务不存在则返回null
     */
    Sharding findProgressByIdentifier(String identifier, long userId);

    Sharding findByIdentifier(String identifier, long userId);

    void save(Sharding sharding);

    void delete(String identifier);

    boolean incrementChunkNumber(String identifier, int chunkNumber, long userId);
}
