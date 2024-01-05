package com.xiaoyao.netdisk.file.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("netdisk.sharding")
public class ShardingProperties {
    /**
     * 块的最大大小，单位为字节，默认为10MB。
     */
    private Integer chunkSize = 10 * 1024 * 1024;
}
