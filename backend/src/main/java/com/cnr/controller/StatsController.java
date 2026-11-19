package com.cnr.controller;

import com.cnr.common.Result;
import com.cnr.security.RequireRole;
import com.cnr.service.StatsService;
import com.cnr.vo.StatsOverviewVO;
import com.cnr.vo.TrendPointVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 统计接口：技术员与管理员可见
 */
@RestController
@RequestMapping("/api/stats")
@Tag(name = "统计报表")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    @RequireRole({"TECHNICIAN", "ADMIN"})
    @Operation(summary = "统计概览：总量、未关闭数、平均修复时长、按机房/状态分组")
    public Result<StatsOverviewVO> overview() {
        return Result.success(statsService.overview());
    }

    @GetMapping("/trend")
    @RequireRole({"TECHNICIAN", "ADMIN"})
    @Operation(summary = "近 N 天新增与关闭趋势")
    public Result<List<TrendPointVO>> trend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(statsService.trend(days));
    }
}
