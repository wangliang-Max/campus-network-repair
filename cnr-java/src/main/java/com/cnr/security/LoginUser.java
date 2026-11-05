package com.cnr.security;

/**
 * 登录用户信息（由 JWT 解析而来，随请求上下文传递）
 */
public record LoginUser(Long userId, String username, String roleCode) {
}
