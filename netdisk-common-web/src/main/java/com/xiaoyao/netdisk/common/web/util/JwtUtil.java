package com.xiaoyao.netdisk.common.web.util;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {
    private final String REFRESH_TOKEN_PREFIX = "refresh-token:";
    private final String USER_ID_PAYLOAD_KEY = "userId";

    @Value("${token.secure-key}")
    private String secureKey;

    @Value("${token.access-token.expire}")
    private int accessTokenExpire;

    @Value("${token.refresh-token.expire}")
    private int refreshTokenExpire;

    private final StringRedisTemplate redisTemplate;

    public JwtUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String createJwt(Long userId) {
        String token = JWT.create()
                .setExpiresAt(DateTime.now().offset(DateField.DAY_OF_YEAR, accessTokenExpire))
                .setPayload(USER_ID_PAYLOAD_KEY, String.valueOf(userId))
                .setKey(secureKey.getBytes(StandardCharsets.UTF_8))
                .sign();
        // 保存refresh-token
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + userId, token, refreshTokenExpire, TimeUnit.DAYS);
        return token;
    }

    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    public boolean isJwtValid(String jwt) {
        try {
            JWTUtil.verify(jwt, secureKey.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isJwtExpire(String jwt) {
        try {
            JWTValidator.of(jwt).validateDate();
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public Long getUserId(String jwt) {
        return Long.parseLong((String) JWTUtil.parseToken(jwt).getPayload(USER_ID_PAYLOAD_KEY));
    }
}
