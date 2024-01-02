package com.xiaoyao.netdisk.common.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration("commonWebPropertiesConfig")
@PropertySource("classpath:common-web-application.properties")
public class PropertiesConfig {
}
