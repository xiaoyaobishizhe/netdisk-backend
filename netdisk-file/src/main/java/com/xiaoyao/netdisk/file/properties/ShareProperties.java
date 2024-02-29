package com.xiaoyao.netdisk.file.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("netdisk.share")
public class ShareProperties {
    private String urlMode;
}
