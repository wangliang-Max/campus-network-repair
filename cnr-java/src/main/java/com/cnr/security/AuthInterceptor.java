package com.cnr.security;


import com.cnr.common.ErrorCode;
import com.cnr.exception.BusinessException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证拦截器：校验 Authorization: Bearer <token>，通过后写入 UserContext
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 静态资源等非接口请求直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        try {
            LoginUser user = jwtUtil.parse(header.substring(7));
            UserContext.set(user);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 请求结束必须清理，防止 ThreadLocal 内存泄漏 / 串号
        UserContext.clear();
    }
}
