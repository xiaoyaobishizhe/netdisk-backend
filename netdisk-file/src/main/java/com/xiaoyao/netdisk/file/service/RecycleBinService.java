package com.xiaoyao.netdisk.file.service;

import com.xiaoyao.netdisk.file.dto.ListRecycleBinDTO;

import java.util.List;

public interface RecycleBinService {
    /**
     * 查看回收站中的文件和文件夹。
     */
    ListRecycleBinDTO list();

    /**
     * 将文件或文件夹移动到回收站中。
     *
     * @param ids 文件或文件夹的id
     */
    void delete(List<String> ids);

    /**
     * 彻底删除回收站中的文件或文件夹。
     *
     * @param ids 文件或文件夹的id
     */
    void remove(List<String> ids);

    /**
     * 将回收站中的文件或文件夹还原。
     *
     * @param ids 文件或文件夹的id
     */
    void restore(List<String> ids);

    /**
     * 清空回收站。
     */
    void clear();
}
