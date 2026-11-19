package com.cnr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 处理记录表 repair_handle：技术员每次处理动作的留痕
 */
@Data
@TableName("repair_handle")
public class RepairHandle {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long handlerId;

    /** ACCEPT / INVESTIGATE / REPAIR / COMPLETE / RETURN */
    private String action;

    private String content;

    private LocalDateTime createdAt;
}
