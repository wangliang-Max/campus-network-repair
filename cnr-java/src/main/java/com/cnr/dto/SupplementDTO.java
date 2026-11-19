package com.cnr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 报障人补充故障信息请求
 */
@Data
@Schema(description = "补充故障信息请求")
public class SupplementDTO {

    @NotBlank(message = "故障描述不能为空")
    @Size(max = 500, message = "故障描述最长 500 字")
    @Schema(description = "补充后的故障描述")
    private String faultDesc;
}
