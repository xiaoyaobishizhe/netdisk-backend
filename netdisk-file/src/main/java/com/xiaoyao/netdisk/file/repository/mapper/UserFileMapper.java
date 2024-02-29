package com.xiaoyao.netdisk.file.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyao.netdisk.file.repository.entity.UserFile;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserFileMapper extends BaseMapper<UserFile> {
    void insertMany(List<UserFile> userFiles);
}




