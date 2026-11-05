package com.cnr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表 sys_user（密码为 BCrypt 哈希，绝不存明文）
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** BCrypt 哈希 */
    private String password;

    private String realName;

    private String phone;

    private Long roleId;

    /** 是否启用：1 启用 / 0 禁用 */
    private Boolean enabled;

    private LocalDateTime createdAt;
}
