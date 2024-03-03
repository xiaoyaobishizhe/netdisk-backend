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

    /**
     * 获取分享的文件列表。
     *
     * @param token 分享的token
     * @return 文件id列表
     */
    List<Long> getFileList(String token);
}
