package com.cnr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报修单视图对象 VO：面向接口输出，可携带关联展示字段
 * （阶段五做多表关联查询时由 JOIN 填充 reporterName / deviceName / roomName）
 */
@Data
@Schema(description = "报修单视图对象")
public class RepairOrderVO {

    @Schema(description = "报修单ID")
    private Long id;

    @Schema(description = "报修单号")
    private String orderNo;

    @Schema(description = "报障人ID")
    private Long reporterId;

    @Schema(description = "报障人姓名（关联 sys_user）")
    private String reporterName;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "设备名称（关联 device）")
    private String deviceName;

    @Schema(description = "所在机房")
    private String roomName;

    @Schema(description = "故障描述")
    private String faultDesc;

    @Schema(description = "优先级")
    private String priority;

    @Schema(description = "单据状态")
    private String status;

    @Schema(description = "提交时间")
    private LocalDateTime createdAt;
}
