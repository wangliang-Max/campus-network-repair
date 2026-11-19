package com.cnr.controller;

import com.cnr.common.Result;
import com.cnr.dto.LoginDTO;
import com.cnr.dto.RegisterDTO;
import com.cnr.dto.ChangePasswordDTO;
import com.cnr.service.AuthService;
import com.cnr.service.UserService;
import com.cnr.vo.LoginVO;
import com.cnr.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "登录，返回 JWT")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @PostMapping("/register")
    @Operation(summary = "报障人自助注册，成功后直接返回 JWT")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success(authService.register(dto));
    }

    @GetMapping("/me")
    @Operation(summary = "当前登录用户信息")
    public Result<UserVO> me() {
        return Result.success(userService.currentUser());
    }

    @PutMapping("/password")
    @Operation(summary = "修改当前用户密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        userService.changePassword(dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }
}
