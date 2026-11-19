package com.cnr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 修改设备状态请求
 */
@Data
@Schema(description = "修改设备状态")
public class DeviceStatusDTO {

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "NORMAL|FAULT|REPAIRING", message = "设备状态只能为 NORMAL / FAULT / REPAIRING")
    private String status;
}
