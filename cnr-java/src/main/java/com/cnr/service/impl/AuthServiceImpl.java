package com.cnr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cnr.common.ErrorCode;
import com.cnr.dto.LoginDTO;
import com.cnr.entity.SysRole;
import com.cnr.entity.SysUser;
import com.cnr.exception.BusinessException;
import com.cnr.mapper.SysRoleMapper;
import com.cnr.mapper.SysUserMapper;
import com.cnr.security.JwtUtil;
import com.cnr.security.LoginUser;
import com.cnr.service.AuthService;
import com.cnr.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        // 统一提示"用户名或密码错误"，避免暴露用户是否存在
        if (user == null || !user.getEnabled()
                || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名或密码错误");
        }
        SysRole role = sysRoleMapper.selectById(user.getRoleId());
        String roleCode = role == null ? "REPORTER" : role.getRoleCode();
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), roleCode);
        String token = jwtUtil.generate(loginUser);
        return new LoginVO(token, user.getUsername(), user.getRealName(), roleCode);
    }
}
