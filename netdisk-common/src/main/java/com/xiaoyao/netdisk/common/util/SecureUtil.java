package com.xiaoyao.netdisk.common.util;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;
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
                .setExpiresAt(DateTime.now().offset(DateField.DAY_OF_YEAR, 7))
                .setPayload("userId", String.valueOf(id))
                .setKey(SECURE_KEY.getBytes(StandardCharsets.UTF_8))
                .sign();
    }

    public static void verifyJwt(String jwt) {
        // 验证jwt是否非法
        JWTUtil.verify(jwt, SECURE_KEY.getBytes(StandardCharsets.UTF_8));
        // 严重jwt是否过期
        JWTValidator.of(jwt).validateDate();
    }

    public static Long getUserId(String jwt) {
        return Long.parseLong((String) JWTUtil.parseToken(jwt).getPayload("userId"));
    }
}
