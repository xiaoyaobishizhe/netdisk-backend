package com.xiaoyao.netdisk.common.web.interceptor;

import cn.hutool.core.util.StrUtil;
import com.xiaoyao.netdisk.common.exception.E;
import com.xiaoyao.netdisk.common.exception.NetdiskException;
import com.xiaoyao.netdisk.common.web.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {
    public static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    public TokenInterceptor(JwtUtil jwtUtil, StringRedisTemplate redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token)) {
            throw new NetdiskException(E.NO_LOGIN);
        } else if (!jwtUtil.isJwtValid(token)) {
            throw new NetdiskException(E.INVALID_TOKEN);
        }

        Long userId = jwtUtil.getUserId(token);
        if (jwtUtil.isJwtExpire(token)) {
            // 尝试续签
            String refreshToken = redisTemplate.opsForValue().get("refresh-token:" + userId);
            if (refreshToken == null || !refreshToken.equals(token)) {
                throw new NetdiskException(E.TOKEN_EXPIRED);
            }
            token = jwtUtil.createJwt(userId);
            redisTemplate.opsForValue().set("refresh-token:" + userId, token);
            response.setHeader("token", token);
        }

        USER_ID.set(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        USER_ID.remove();
    }
}
