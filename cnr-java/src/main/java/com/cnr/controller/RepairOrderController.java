package com.cnr.controller;

import com.cnr.common.Result;
import com.cnr.dto.RepairOrderCreateDTO;
import com.cnr.service.RepairOrderService;
import com.cnr.vo.RepairOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报修单接口（阶段二示例切片，后续阶段继续扩展状态流转、分页等）
 */
@RestController
@RequestMapping("/api/repair-orders")
@Tag(name = "报修单管理")
@RequiredArgsConstructor
public class RepairOrderController {

    private final RepairOrderService repairOrderService;

    @PostMapping
    @Operation(summary = "提交报修单")
    public Result<RepairOrderVO> create(@Valid @RequestBody RepairOrderCreateDTO dto) {
        return Result.success(repairOrderService.create(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询报修单详情")
    public Result<RepairOrderVO> detail(@PathVariable Long id) {
        return Result.success(repairOrderService.detail(id));
    }

    @GetMapping("/my")
    @Operation(summary = "我的报修列表")
    public Result<List<RepairOrderVO>> my() {
        return Result.success(repairOrderService.myOrders());
    }
}
