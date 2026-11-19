package com.cnr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 确认评价表 confirm_record：允许同一单多次确认（验收不通过返工后再确认）
 */
@Data
@TableName("confirm_record")
public class ConfirmRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long confirmBy;

    /** PASS / FAIL */
    private String result;

    private Integer score;

    private String comment;

    private LocalDateTime createdAt;
}
