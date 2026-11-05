package com.cnr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录成功返回 VO：只返回必要信息，不返回密码等敏感字段
 */
@Data
@AllArgsConstructor
@Schema(description = "登录成功返回")
public class LoginVO {

    @Schema(description = "JWT 令牌，后续请求放入 Authorization: Bearer <token>")
    private String token;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "角色编码")
    private String roleCode;
}
