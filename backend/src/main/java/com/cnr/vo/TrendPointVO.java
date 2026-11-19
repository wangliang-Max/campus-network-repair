package com.cnr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 按天统计的新增/关闭单量
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "趋势数据点")
public class TrendPointVO {

    private String date;
    private Long created;
    private Long closed;
}
