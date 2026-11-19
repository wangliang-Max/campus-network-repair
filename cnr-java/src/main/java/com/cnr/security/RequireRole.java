package com.cnr.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色校验注解：标注在 Controller 方法或类上，由 RoleInterceptor 统一校验
 * 例：@RequireRole({"ADMIN"})
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /** 允许访问的角色编码：REPORTER / TECHNICIAN / ADMIN */
    String[] value();
}
