package com.cnr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 状态流转通用请求：remark 写状态日志，content 写处理记录
 */
@Data
@Schema(description = "状态流转请求")
public class OrderActionDTO {

    @Size(max = 200, message = "备注最长 200 字")
    @Schema(description = "状态日志备注")
    private String remark;

    @Size(max = 500, message = "处理内容最长 500 字")
    @Schema(description = "处理内容（写 repair_handle）")
    private String content;
}
