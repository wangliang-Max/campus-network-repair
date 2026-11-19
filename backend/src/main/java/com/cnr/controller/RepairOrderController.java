package com.cnr.controller;

import com.cnr.common.PageResult;
import com.cnr.common.Result;
import com.cnr.dto.ConfirmDTO;
import com.cnr.dto.OrderActionDTO;
import com.cnr.dto.RepairOrderCreateDTO;
import com.cnr.dto.RepairOrderQuery;
import com.cnr.dto.SupplementDTO;
import com.cnr.security.RequireRole;
import com.cnr.service.RepairOrderService;
import com.cnr.vo.RepairOrderDetailVO;
import com.cnr.vo.RepairOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 报修单：创建、查询、状态流转（状态机）
 */
@RestController
@RequestMapping("/api/repair-orders")
@Tag(name = "报修单管理")
@RequiredArgsConstructor
public class RepairOrderController {

    private final RepairOrderService repairOrderService;

    @PostMapping
    @Operation(summary = "提交报修单（同一设备存在未关闭单据时拒绝）")
    public Result<RepairOrderVO> create(@Valid @RequestBody RepairOrderCreateDTO dto) {
        return Result.success(repairOrderService.create(dto));
    }

    @GetMapping
    @Operation(summary = "报修单分页查询（报障人只能看自己的单）")
    public Result<PageResult<RepairOrderVO>> page(RepairOrderQuery query) {
        return Result.success(repairOrderService.page(query));
    }

    @GetMapping("/my")
    @Operation(summary = "我的报修列表")
    public Result<List<RepairOrderVO>> my() {
        return Result.success(repairOrderService.myOrders());
    }

    @GetMapping("/{id}")
    @Operation(summary = "报修单详情（含状态轨迹、处理记录、验收记录、可执行动作）")
    public Result<RepairOrderDetailVO> detail(@PathVariable Long id) {
        return Result.success(repairOrderService.detail(id));
    }

    @PostMapping("/{id}/accept")
    @RequireRole({"TECHNICIAN", "ADMIN"})
    @Operation(summary = "受理报修单：待受理 -> 排查中")
    public Result<RepairOrderDetailVO> accept(@PathVariable Long id,
                                              @RequestBody(required = false) OrderActionDTO dto) {
        return Result.success(repairOrderService.accept(id, dto));
    }

    @PostMapping("/{id}/start-repair")
    @RequireRole({"TECHNICIAN", "ADMIN"})
    @Operation(summary = "开始维修：排查中 -> 修复中")
    public Result<RepairOrderDetailVO> startRepair(@PathVariable Long id,
                                                   @RequestBody(required = false) OrderActionDTO dto) {
        return Result.success(repairOrderService.startRepair(id, dto));
    }

    @PostMapping("/{id}/finish")
    @RequireRole({"TECHNICIAN", "ADMIN"})
    @Operation(summary = "修复完成：修复中 -> 待确认")
    public Result<RepairOrderDetailVO> finish(@PathVariable Long id,
                                              @RequestBody(required = false) OrderActionDTO dto) {
        return Result.success(repairOrderService.finish(id, dto));
    }

    @PostMapping("/{id}/return-info")
    @RequireRole({"TECHNICIAN", "ADMIN"})
    @Operation(summary = "无法复现退回补充：排查中 -> 待补充")
    public Result<RepairOrderDetailVO> returnInfo(@PathVariable Long id,
                                                  @RequestBody(required = false) OrderActionDTO dto) {
        return Result.success(repairOrderService.returnInfo(id, dto));
    }

    @PostMapping("/{id}/supplement")
    @Operation(summary = "报障人补充故障信息：待补充 -> 待受理")
    public Result<RepairOrderDetailVO> supplement(@PathVariable Long id,
                                                  @Valid @RequestBody SupplementDTO dto) {
        return Result.success(repairOrderService.supplement(id, dto));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "报障人验收：PASS 关闭工单 / FAIL 退回返工")
    public Result<RepairOrderDetailVO> confirm(@PathVariable Long id,
                                               @Valid @RequestBody ConfirmDTO dto) {
        return Result.success(repairOrderService.confirm(id, dto));
    }
}
