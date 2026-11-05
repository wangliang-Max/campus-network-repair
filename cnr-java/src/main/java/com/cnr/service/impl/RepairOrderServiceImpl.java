package com.cnr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cnr.common.ErrorCode;
import com.cnr.dto.RepairOrderCreateDTO;
import com.cnr.entity.Device;
import com.cnr.entity.RepairOrder;
import com.cnr.entity.SysUser;
import com.cnr.exception.BusinessException;
import com.cnr.mapper.DeviceMapper;
import com.cnr.mapper.RepairOrderMapper;
import com.cnr.mapper.SysUserMapper;
import com.cnr.security.UserContext;
import com.cnr.service.RepairOrderService;
import com.cnr.vo.RepairOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder>
        implements RepairOrderService {

    private final SysUserMapper sysUserMapper;
    private final DeviceMapper deviceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RepairOrderVO create(RepairOrderCreateDTO dto) {
        Long userId = UserContext.getUserId();
        // 防重复提交：同一报障人 + 同一设备 + 存在未关闭单据则拒绝（对应方案 4.6）
        Long openCount = baseMapper.selectCount(new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getReporterId, userId)
                .eq(RepairOrder::getDeviceId, dto.getDeviceId())
                .ne(RepairOrder::getStatus, "CLOSED"));
        if (openCount > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "该设备已有未关闭的报修单，请勿重复提交");
        }

        RepairOrder order = new RepairOrder();
        order.setOrderNo(genOrderNo());
        order.setReporterId(userId);
        order.setDeviceId(dto.getDeviceId());
        order.setFaultDesc(dto.getFaultDesc());
        order.setPriority(dto.getPriority() == null ? "NORMAL" : dto.getPriority());
        order.setStatus("PENDING");
        order.setVersion(0);
        save(order);
        return toVO(order);
    }

    @Override
    public RepairOrderVO detail(Long id) {
        RepairOrder order = getById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "报修单不存在");
        }
        // 防水平越权：非本人且非管理员/技术员，禁止查看他人单据（对应方案 4.5）
        String roleCode = UserContext.getRoleCode();
        boolean isOwner = order.getReporterId().equals(UserContext.getUserId());
        boolean isStaff = "ADMIN".equals(roleCode) || "TECHNICIAN".equals(roleCode);
        if (!isOwner && !isStaff) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看该报修单");
        }
        return toVO(order);
    }

    @Override
    public List<RepairOrderVO> myOrders() {
        List<RepairOrder> list = baseMapper.selectList(new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getReporterId, UserContext.getUserId())
                .orderByDesc(RepairOrder::getCreatedAt));
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    private String genOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "RO" + time + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    /**
     * 注意：此处逐个查设备/用户仅为阶段二演示；
     * 阶段五做多表关联查询时改为一次 JOIN 查出，避免 N+1（对应方案 4.7）
     */
    private RepairOrderVO toVO(RepairOrder order) {
        RepairOrderVO vo = new RepairOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setReporterId(order.getReporterId());
        vo.setDeviceId(order.getDeviceId());
        vo.setFaultDesc(order.getFaultDesc());
        vo.setPriority(order.getPriority());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt());
        SysUser user = sysUserMapper.selectById(order.getReporterId());
        if (user != null) {
            vo.setReporterName(user.getRealName());
        }
        Device device = deviceMapper.selectById(order.getDeviceId());
        if (device != null) {
            vo.setDeviceName(device.getDeviceName());
            vo.setRoomName(device.getRoomName());
        }
        return vo;
    }
}
