package com.cnr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户分页查询条件
 */
@Data
@Schema(description = "用户分页查询条件")
public class UserQuery {

    private Integer page = 1;
    private Integer size = 10;

    @Schema(description = "用户名或姓名关键字")
    private String keyword;

    @Schema(description = "角色编码：REPORTER / TECHNICIAN / ADMIN")
    private String roleCode;

    @Schema(description = "是否启用")
    private Boolean enabled;
}
