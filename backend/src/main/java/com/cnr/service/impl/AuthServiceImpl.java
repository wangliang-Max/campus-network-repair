package com.cnr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cnr.common.ErrorCode;
import com.cnr.dto.LoginDTO;
import com.cnr.dto.RegisterDTO;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
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
            log.warn("登录失败 username={}", dto.getUsername());
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名或密码错误");
        }
        SysRole role = sysRoleMapper.selectById(user.getRoleId());
        String roleCode = role == null ? "REPORTER" : role.getRoleCode();
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), roleCode);
        String token = jwtUtil.generate(loginUser);
        log.info("登录成功 userId={} username={} role={}", user.getId(), user.getUsername(), roleCode);
        return new LoginVO(token, user.getUsername(), user.getRealName(), roleCode);
    }

    @Override
    public LoginVO register(RegisterDTO dto) {
        Long exists = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (exists > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "用户名已存在");
        }
        SysRole role = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, "REPORTER"));
        if (role == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setRoleId(role.getId());
        user.setEnabled(true);
        sysUserMapper.insert(user);
        log.info("用户注册 userId={} username={} role=REPORTER", user.getId(), user.getUsername());

        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), "REPORTER");
        String token = jwtUtil.generate(loginUser);
        return new LoginVO(token, user.getUsername(), user.getRealName(), "REPORTER");
    }
}
