package com.xiaoyao.netdisk.file.service;

import com.xiaoyao.netdisk.file.dto.ListRecycleBinDTO;

public interface RecycleBinService {
    /**
     * 查看回收站中的文件和文件夹。
     */
    ListRecycleBinDTO list();

    /**
     * 将文件或文件夹移动到回收站中。
     *
     * @param id 文件或文件夹的id
     */
    void delete(String id);

    /**
     * 彻底删除回收站中的文件或文件夹。
     *
     * @param id 文件或文件夹的id
     */
    void remove(String id);

    /**
     * 将回收站中的文件或文件夹还原。
     *
     * @param id 文件或文件夹的id
     */
    void restore(String id);

    /**
     * 清空回收站。
     */
    void clear();
}
