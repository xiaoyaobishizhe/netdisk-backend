package com.xiaoyao.netdisk.file.service;

import com.xiaoyao.netdisk.file.dto.FileListDTO;
import com.xiaoyao.netdisk.file.dto.ListSharesDTO;

import java.util.List;

public interface ShareService {
    /**
     * 列出所有的分享。
     */
    ListSharesDTO listShares();

    /**
     * 创建分享并返回分享链接。
     *
     * @param name     分享的名称
     * @param password 分享的提取码
     * @param timeout  分享的有效时间，单位为天
     * @param fileList 文件列表
     * @return 分享链接
     */
    String createShare(String name, String password, int timeout, List<Long> fileList);

    /**
     * 获取分享的分享链接。
     *
     * @param id 分享的id
     * @return 分享链接
     */
    String getShareLink(String id);

    /**
     * 删除分享。
     *
     * @param id 分享的id
     */
    void deleteShare(String id);

    /**
     * 获取分享的访问token。
     *
     * @param code     分享的code
     * @param password 分享的提取码
     * @return 访问token
     */
    String getAccessToken(String code, String password);

    /**
     * 获取分享的文件列表。
     *
     * @param token    访问token
     * @param parentId 父文件夹id
     * @return 文件列表
     */
    FileListDTO list(String token, String parentId);
}
