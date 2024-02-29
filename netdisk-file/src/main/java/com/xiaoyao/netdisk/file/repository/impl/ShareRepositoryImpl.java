package com.xiaoyao.netdisk.file.repository.impl;

import com.xiaoyao.netdisk.file.repository.ShareRepository;
import com.xiaoyao.netdisk.file.repository.entity.Share;
import com.xiaoyao.netdisk.file.repository.mapper.ShareMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;

@Repository
public class ShareRepositoryImpl implements ShareRepository {
    private final ShareMapper shareMapper;

    public ShareRepositoryImpl(ShareMapper shareMapper) {
        this.shareMapper = shareMapper;
    }

    @Override
    public List<Share> listShares(long userId) {
        return shareMapper.selectList(lambdaQuery(Share.class)
                .select(Share::getId,
                        Share::getName,
                        Share::getCreateTime,
                        Share::getTimeout)
                .eq(Share::getUserId, userId));
    }

    @Override
    public void save(Share share) {
        shareMapper.insert(share);
    }

    @Override
    public Share findShareLink(long id, long userId) {
        return shareMapper.selectOne(lambdaQuery(Share.class)
                .select(Share::getCode,
                        Share::getPassword,
                        Share::getTimeout,
                        Share::getCreateTime)
                .eq(Share::getId, id)
                .eq(Share::getUserId, userId));
    }

    @Override
    public boolean deleteShare(long id, long userId) {
        return shareMapper.delete(lambdaQuery(Share.class)
                .eq(Share::getId, id)
                .eq(Share::getUserId, userId)) > 0;
    }

    @Override
    public Share findAccessToken(String code) {
        return shareMapper.selectOne(lambdaQuery(Share.class)
                .select(Share::getPassword,
                        Share::getToken,
                        Share::getTimeout,
                        Share::getCreateTime)
                .eq(Share::getCode, code));
    }
}
