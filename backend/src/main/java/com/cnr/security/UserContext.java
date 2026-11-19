package com.cnr.security;

/**
 * 请求级用户上下文：拦截器写入，请求结束清除（ThreadLocal，避免线程串号）
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.userId();
    }

    public static String getRoleCode() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.roleCode();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
