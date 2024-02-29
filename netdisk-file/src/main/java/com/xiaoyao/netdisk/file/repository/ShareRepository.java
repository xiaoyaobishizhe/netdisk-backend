package com.xiaoyao.netdisk.file.repository;

import com.xiaoyao.netdisk.file.repository.entity.Share;

import java.util.List;

public interface ShareRepository {
    List<Share> listShares(long userId);

    void save(Share share);

    Share findShareLink(long id, long userId);

    boolean deleteShare(long id, long userId);

    Share findAccessToken(String code);
}
