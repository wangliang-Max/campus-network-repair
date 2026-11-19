package com.cnr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 状态变更日志表 status_log：每次状态流转都必须写一条
 */
@Data
@TableName("status_log")
public class StatusLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String fromStatus;

    private String toStatus;

    private Long operatorId;

    private String remark;

    private LocalDateTime createdAt;
}
