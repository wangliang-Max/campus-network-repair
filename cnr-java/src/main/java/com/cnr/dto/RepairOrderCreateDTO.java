package com.cnr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提交报修单请求 DTO
 */
@Data
@Schema(description = "提交报修单请求")
public class RepairOrderCreateDTO {

    @NotNull(message = "设备不能为空")
    @Schema(description = "故障设备ID")
    private Long deviceId;

    @NotBlank(message = "故障描述不能为空")
    @Size(max = 500, message = "故障描述最长 500 字")
    @Schema(description = "故障描述")
    private String faultDesc;

    @Pattern(regexp = "NORMAL|URGENT", message = "优先级只能为 NORMAL 或 URGENT")
    @Schema(description = "优先级，默认 NORMAL")
    private String priority;
}
