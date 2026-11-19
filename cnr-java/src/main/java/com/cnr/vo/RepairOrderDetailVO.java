package com.cnr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 报修单详情：单据本身 + 状态轨迹 + 处理记录 + 验收记录 + 当前用户可执行动作
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "报修单详情")
public class RepairOrderDetailVO extends RepairOrderVO {

    @Schema(description = "状态变更轨迹")
    private List<StatusLogVO> statusLogs;

    @Schema(description = "技术员处理记录")
    private List<RepairHandleVO> handles;

    @Schema(description = "验收确认记录")
    private List<ConfirmRecordVO> confirms;

    @Schema(description = "当前登录用户可执行的动作")
    private List<String> allowedActions;
}
