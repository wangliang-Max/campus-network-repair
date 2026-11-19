package com.cnr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分组统计结果：名称 + 数量
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分组统计项")
public class NameCountVO {

    private String name;
    private Long count;
}
