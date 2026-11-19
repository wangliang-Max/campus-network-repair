package com.cnr.controller;

import com.cnr.common.PageResult;
import com.cnr.common.Result;
import com.cnr.dto.UserEnabledDTO;
import com.cnr.dto.UserQuery;
import com.cnr.security.RequireRole;
import com.cnr.service.UserService;
import com.cnr.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理：管理员维护账号状态，登录用户可查技术员列表
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @RequireRole({"ADMIN"})
    @Operation(summary = "用户分页查询（管理员）")
    public Result<PageResult<UserVO>> page(UserQuery query) {
        return Result.success(userService.page(query));
    }

    @PutMapping("/{id}/enabled")
    @RequireRole({"ADMIN"})
    @Operation(summary = "启用或禁用用户（管理员）")
    public Result<UserVO> setEnabled(@PathVariable Long id, @Valid @RequestBody UserEnabledDTO dto) {
        return Result.success(userService.setEnabled(id, dto.getEnabled()));
    }

    @GetMapping("/technicians")
    @Operation(summary = "技术员列表（派单选择）")
    public Result<List<UserVO>> technicians() {
        return Result.success(userService.technicians());
    }
}
