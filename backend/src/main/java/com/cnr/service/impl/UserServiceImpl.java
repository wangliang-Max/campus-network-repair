package com.cnr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnr.common.ErrorCode;
import com.cnr.common.PageResult;
import com.cnr.dto.UserQuery;
import com.cnr.entity.SysRole;
import com.cnr.entity.SysUser;
import com.cnr.exception.BusinessException;
import com.cnr.mapper.SysRoleMapper;
import com.cnr.mapper.SysUserMapper;
import com.cnr.security.UserContext;
import com.cnr.service.UserService;
import com.cnr.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<UserVO> page(UserQuery query) {
        int pageNo = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int pageSize = query.getSize() == null || query.getSize() < 1 ? 10 : Math.min(query.getSize(), 100);

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysUser::getUsername, query.getKeyword())
                    .or().like(SysUser::getRealName, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getRoleCode())) {
            SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                    .eq(SysRole::getRoleCode, query.getRoleCode()));
            if (role == null) {
                return new PageResult<>(List.of(), 0, pageNo, pageSize, 0);
            }
            wrapper.eq(SysUser::getRoleId, role.getId());
        }
        wrapper.eq(query.getEnabled() != null, SysUser::getEnabled, query.getEnabled());
        wrapper.orderByAsc(SysUser::getId);

        Page<SysUser> result = userMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        Map<Long, SysRole> roles = roleMap();
        List<UserVO> records = result.getRecords().stream()
                .map(user -> toVO(user, roles.get(user.getRoleId())))
                .toList();
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public UserVO setEnabled(Long id, boolean enabled) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (user.getId().equals(UserContext.getUserId()) && !enabled) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "不能禁用当前登录账号");
        }
        user.setEnabled(enabled);
        userMapper.updateById(user);
        log.info("用户状态变更 userId={} username={} enabled={} operator={}",
                user.getId(), user.getUsername(), enabled, UserContext.getUserId());
        return toVO(user, roleMap().get(user.getRoleId()));
    }

    @Override
    public List<UserVO> technicians() {
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, "TECHNICIAN"));
        if (role == null) {
            return List.of();
        }
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRoleId, role.getId())
                .eq(SysUser::getEnabled, true)
                .orderByAsc(SysUser::getId));
        return users.stream().map(user -> toVO(user, role)).toList();
    }

    @Override
    public UserVO currentUser() {
        SysUser user = userMapper.selectById(UserContext.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return toVO(user, roleMap().get(user.getRoleId()));
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        SysUser user = userMapper.selectById(UserContext.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "原密码不正确");
        }
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(update);
        log.info("用户修改密码 userId={} username={}", user.getId(), user.getUsername());
    }

    private Map<Long, SysRole> roleMap() {
        return roleMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysRole::getId, Function.identity()));
    }

    private UserVO toVO(SysUser user, SysRole role) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setRoleCode(role == null ? null : role.getRoleCode());
        vo.setRoleName(role == null ? null : role.getRoleName());
        vo.setEnabled(user.getEnabled());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
