package com.cnr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求：自助注册只允许报障人角色，技术员/管理员由管理员开通
 */
@Data
@Schema(description = "注册请求")
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]{4,20}$", message = "用户名只能由 4-20 位字母、数字或下划线组成")
    @Schema(description = "用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在 6-32 位之间")
    @Schema(description = "密码（明文传输由 HTTPS 保证，入库前 BCrypt 加密）")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名最长 50 字")
    @Schema(description = "真实姓名")
    private String realName;

    @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确")
    @Schema(description = "手机号，可空")
    private String phone;
}
