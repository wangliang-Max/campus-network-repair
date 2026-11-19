package com.cnr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 报修单分页查询条件
 */
@Data
@Schema(description = "报修单分页查询条件")
public class RepairOrderQuery {

    @Schema(description = "页码，从 1 开始")
    private Integer page = 1;

    @Schema(description = "每页条数")
    private Integer size = 10;

    @Schema(description = "状态：PENDING/INVESTIGATING/REPAIRING/PENDING_CONFIRM/CLOSED/INFO_NEEDED")
    private String status;

    @Schema(description = "优先级：NORMAL/URGENT")
    private String priority;

    @Schema(description = "关键字：单号、故障描述、设备名、报障人姓名")
    private String keyword;

    @Schema(description = "机房名称")
    private String roomName;

    @Schema(description = "是否只看自己的报修单")
    private Boolean onlyMine;
}
