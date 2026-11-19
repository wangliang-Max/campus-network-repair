package com.cnr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备视图对象
 */
@Data
@Schema(description = "设备信息")
public class DeviceVO {

    private Long id;
    private String deviceCode;
    private String deviceName;
    private String roomName;
    private String status;
    private String statusLabel;
    private String remark;
    private LocalDateTime createdAt;
}
