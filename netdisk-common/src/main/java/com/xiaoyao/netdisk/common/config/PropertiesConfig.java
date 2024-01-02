package com.xiaoyao.netdisk.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration("commonPropertiesConfig")
@PropertySource("classpath:common-application.properties")
public class PropertiesConfig {
}
