package com.cnr.controller;

import com.cnr.common.Result;
import com.cnr.dto.LoginDTO;
import com.cnr.service.AuthService;
import com.cnr.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/login")
    @Operation(summary = "登录，返回 JWT")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }
}
