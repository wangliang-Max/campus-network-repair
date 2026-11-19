package com.cnr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 启用/禁用用户请求
 */
@Data
@Schema(description = "启用或禁用用户")
public class UserEnabledDTO {

    @NotNull(message = "enabled 不能为空")
    private Boolean enabled;
}
