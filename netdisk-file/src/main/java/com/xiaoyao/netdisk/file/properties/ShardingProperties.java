package com.xiaoyao.netdisk.file.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("netdisk.sharding")
public class ShardingProperties {
    /**
     * 块的最小大小，单位为字节，必须要大于5MB，默认为10MB，块的最大大小为最小大小的2倍。
     */
    private Integer minChunkSize = 10 * 1024 * 1024;
}
