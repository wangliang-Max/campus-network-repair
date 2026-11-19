package com.cnr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象，不返回密码
 */
@Data
@Schema(description = "用户信息")
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String roleCode;
    private String roleName;
    private Boolean enabled;
    private LocalDateTime createdAt;
}
