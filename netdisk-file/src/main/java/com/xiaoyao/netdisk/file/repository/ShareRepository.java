package com.xiaoyao.netdisk.file.repository;

import com.xiaoyao.netdisk.file.repository.entity.Share;

import java.util.List;

public interface ShareRepository {
    List<Share> listShares(long userId);

    void save(Share share);

    Share findShareLink(long id, long userId);

    void deleteShare(List<Long> ids, long userId);

    Share findAccessToken(String code);

    /**
     * 获取分享者的用户id。
     *
     * @param code 分享的code
     * @return 分享者的用户id
     */
    Long getUserIdByCode(String code);
}
