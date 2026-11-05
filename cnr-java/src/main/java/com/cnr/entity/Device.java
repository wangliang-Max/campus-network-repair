package com.cnr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 机房设备档案表 device（主数据）
 */
@Data
@TableName("device")
public class Device {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String deviceCode;

    private String deviceName;

    /** 所在机房/房间 */
    private String roomName;

    /** NORMAL 正常 / FAULT 故障 / REPAIRING 维修中 */
    private String status;

    private String remark;

    private LocalDateTime createdAt;
}
