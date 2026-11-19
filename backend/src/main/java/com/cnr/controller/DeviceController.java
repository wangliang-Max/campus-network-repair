package com.cnr.controller;

import com.cnr.common.PageResult;
import com.cnr.common.Result;
import com.cnr.dto.DeviceQuery;
import com.cnr.dto.DeviceSaveDTO;
import com.cnr.dto.DeviceStatusDTO;
import com.cnr.security.RequireRole;
import com.cnr.service.DeviceService;
import com.cnr.vo.DeviceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 机房设备管理：查询对所有登录用户开放，增删改仅管理员
 */
@RestController
@RequestMapping("/api/devices")
@Tag(name = "机房设备管理")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    @Operation(summary = "设备分页查询（支持机房、状态、关键字）")
    public Result<PageResult<DeviceVO>> page(DeviceQuery query) {
        return Result.success(deviceService.page(query));
    }

    @GetMapping("/rooms")
    @Operation(summary = "机房列表（用于筛选项）")
    public Result<List<String>> rooms() {
        return Result.success(deviceService.rooms());
    }

    @GetMapping("/{id}")
    @Operation(summary = "设备详情")
    public Result<DeviceVO> detail(@PathVariable Long id) {
        return Result.success(deviceService.detail(id));
    }

    @PostMapping
    @RequireRole({"ADMIN"})
    @Operation(summary = "新增设备（管理员）")
    public Result<DeviceVO> create(@Valid @RequestBody DeviceSaveDTO dto) {
        return Result.success(deviceService.create(dto));
    }

    @PutMapping("/{id}")
    @RequireRole({"ADMIN"})
    @Operation(summary = "修改设备（管理员）")
    public Result<DeviceVO> update(@PathVariable Long id, @Valid @RequestBody DeviceSaveDTO dto) {
        return Result.success(deviceService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN"})
    @Operation(summary = "删除设备（管理员，存在报修记录时拒绝）")
    public Result<Void> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @RequireRole({"ADMIN"})
    @Operation(summary = "修改设备状态（管理员）")
    public Result<DeviceVO> updateStatus(@PathVariable Long id, @Valid @RequestBody DeviceStatusDTO dto) {
        return Result.success(deviceService.updateStatus(id, dto.getStatus()));
    }
}
