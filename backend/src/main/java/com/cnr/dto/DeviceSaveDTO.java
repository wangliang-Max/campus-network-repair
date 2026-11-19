package com.cnr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 设备新增/修改请求
 */
@Data
@Schema(description = "设备新增或修改请求")
public class DeviceSaveDTO {

    @NotBlank(message = "设备编号不能为空")
    @Size(max = 30, message = "设备编号最长 30 位")
    @Schema(description = "设备编号，唯一")
    private String deviceCode;

    @NotBlank(message = "设备名称不能为空")
    @Size(max = 100, message = "设备名称最长 100 字")
    private String deviceName;

    @NotBlank(message = "机房不能为空")
    @Size(max = 50, message = "机房名称最长 50 字")
    private String roomName;

    @Pattern(regexp = "NORMAL|FAULT|REPAIRING", message = "设备状态只能为 NORMAL / FAULT / REPAIRING")
    @Schema(description = "设备状态，默认 NORMAL")
    private String status;

    @Size(max = 200, message = "备注最长 200 字")
    private String remark;
}
