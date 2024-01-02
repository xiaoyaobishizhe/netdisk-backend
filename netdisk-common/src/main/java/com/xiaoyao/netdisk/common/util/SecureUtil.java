package com.xiaoyao.netdisk.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.jwt.JWT;
import cn.hutool.setting.Setting;

import java.nio.charset.StandardCharsets;

public class SecureUtil {
    private final static String SECURE_KEY;
    private final static HMac hmac;

    static {
        SECURE_KEY = new Setting("secure.setting").getStr("secure-key");
        hmac = DigestUtil.hmac(HmacAlgorithm.HmacSHA256, SECURE_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha256(String str) {
        return hmac.digestHex(str);
    }

    public static String createJwt(Long id) {
        return JWT.create()
                .setExpiresAt(DateUtil.date())
                .setPayload("userId", id)
                .setKey(SECURE_KEY.getBytes(StandardCharsets.UTF_8))
                .sign();
    }
}
