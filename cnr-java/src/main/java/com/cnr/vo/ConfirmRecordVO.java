package com.cnr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 验收确认记录视图对象
 */
@Data
@Schema(description = "验收确认记录")
public class ConfirmRecordVO {

    private Long id;
    private Long confirmBy;
    private String confirmName;
    private String result;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;
}
