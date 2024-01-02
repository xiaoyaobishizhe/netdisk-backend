package com.xiaoyao.netdisk.user.interceptor;

import cn.hutool.core.util.StrUtil;
import com.xiaoyao.netdisk.common.exception.E;
import com.xiaoyao.netdisk.common.exception.NetdiskException;
import com.xiaoyao.netdisk.common.util.SecureUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class TokenInterceptor implements HandlerInterceptor {
    public static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token)) {
            throw new NetdiskException(E.NO_LOGIN);
        }

        try {
            SecureUtil.verifyJwt(token);
            USER_ID.set(SecureUtil.getUserId(token));
        } catch (Exception e) {
            throw new NetdiskException(E.INVALID_TOKEN);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        USER_ID.remove();
    }
}
