package com.xiaoyao.netdisk.common.util;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SecureUtil {
    private HMac hmac;

    @Value("${secure-key}")
    private String secureKey;

    @PostConstruct
    public void init() {
        hmac = DigestUtil.hmac(HmacAlgorithm.HmacSHA256, secureKey.getBytes(StandardCharsets.UTF_8));
    }

    public String sha256(String str) {
        return hmac.digestHex(str);
    }
}
