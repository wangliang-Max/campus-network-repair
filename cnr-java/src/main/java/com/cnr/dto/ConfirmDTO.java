package com.cnr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 报障人验收请求：PASS 关闭工单，FAIL 退回返工
 */
@Data
@Schema(description = "验收确认请求")
public class ConfirmDTO {

    @NotBlank(message = "确认结果不能为空")
    @Pattern(regexp = "PASS|FAIL", message = "确认结果只能为 PASS 或 FAIL")
    @Schema(description = "PASS 通过 / FAIL 不通过")
    private String result;

    @Min(value = 1, message = "评分最低 1 分")
    @Max(value = 5, message = "评分最高 5 分")
    @Schema(description = "满意度 1-5 分，可空")
    private Integer score;

    @Size(max = 500, message = "评价内容最长 500 字")
    private String comment;
}
