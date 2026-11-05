package com.cnr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报修单表 repair_order（核心业务单据）
 */
@Data
@TableName("repair_order")
public class RepairOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 报修单号（唯一，防重复提交第一道闸） */
    private String orderNo;

    private Long reporterId;

    private Long deviceId;

    private String faultDesc;

    /** NORMAL / URGENT */
    private String priority;

    /** PENDING/INVESTIGATING/REPAIRING/PENDING_CONFIRM/CLOSED/INFO_NEEDED */
    private String status;

    /** 乐观锁版本号：状态流转时自动带 version 条件 */
    @Version
    private Integer version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
