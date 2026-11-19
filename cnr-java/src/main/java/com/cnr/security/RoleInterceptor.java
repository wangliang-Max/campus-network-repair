package com.cnr.security;

import com.cnr.common.ErrorCode;
import com.cnr.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 角色拦截器：读取 @RequireRole 注解，校验当前登录用户角色
 * 必须在 AuthInterceptor 之后执行（依赖 UserContext）
 */
@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole == null) {
            return true;
        }
        String roleCode = UserContext.getRoleCode();
        if (roleCode == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        for (String allowed : requireRole.value()) {
            if (allowed.equals(roleCode)) {
                return true;
            }
        }
        throw new BusinessException(ErrorCode.FORBIDDEN);
    }
}
