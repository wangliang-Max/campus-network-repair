package com.cnr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设备分页查询条件
 */
@Data
@Schema(description = "设备分页查询条件")
public class DeviceQuery {

    @Schema(description = "页码，从 1 开始")
    private Integer page = 1;

    @Schema(description = "每页条数")
    private Integer size = 10;

    @Schema(description = "设备编号或名称关键字")
    private String keyword;

    @Schema(description = "机房名称")
    private String roomName;

    @Schema(description = "设备状态")
    private String status;
}
