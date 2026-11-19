package com.cnr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 处理记录视图对象
 */
@Data
@Schema(description = "处理记录")
public class RepairHandleVO {

    private Long id;
    private Long handlerId;
    private String handlerName;
    private String action;
    private String content;
    private LocalDateTime createdAt;
}
