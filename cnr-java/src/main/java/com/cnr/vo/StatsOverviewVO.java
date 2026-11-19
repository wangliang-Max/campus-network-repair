package com.cnr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 统计概览：总量、未关闭数、平均修复时长、按机房/状态分组
 */
@Data
@Schema(description = "统计概览")
public class StatsOverviewVO {

    private Long totalOrders;
    private Long openOrders;
    private Long closedOrders;

    @Schema(description = "平均修复时长（小时，保留两位小数）")
    private BigDecimal avgRepairHours;

    @Schema(description = "未关闭单据按机房分组")
    private List<NameCountVO> byRoom;

    @Schema(description = "全部单据按状态分组")
    private List<NameCountVO> byStatus;
}
