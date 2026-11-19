package com.cnr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cnr.common.ErrorCode;
import com.cnr.common.PageResult;
import com.cnr.dto.DeviceQuery;
import com.cnr.dto.DeviceSaveDTO;
import com.cnr.entity.Device;
import com.cnr.entity.RepairOrder;
import com.cnr.exception.BusinessException;
import com.cnr.mapper.DeviceMapper;
import com.cnr.mapper.RepairOrderMapper;
import com.cnr.service.DeviceService;
import com.cnr.vo.DeviceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {

    private final RepairOrderMapper repairOrderMapper;

    @Override
    public PageResult<DeviceVO> page(DeviceQuery query) {
        int pageNo = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int pageSize = query.getSize() == null || query.getSize() < 1 ? 10 : Math.min(query.getSize(), 100);

        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Device::getDeviceCode, query.getKeyword())
                    .or().like(Device::getDeviceName, query.getKeyword()));
        }
        wrapper.eq(StringUtils.hasText(query.getRoomName()), Device::getRoomName, query.getRoomName());
        wrapper.eq(StringUtils.hasText(query.getStatus()), Device::getStatus, query.getStatus());
        wrapper.orderByAsc(Device::getRoomName).orderByAsc(Device::getDeviceCode);

        Page<Device> result = baseMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<DeviceVO> records = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public DeviceVO detail(Long id) {
        Device device = getById(id);
        if (device == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "设备不存在");
        }
        return toVO(device);
    }

    @Override
    public DeviceVO create(DeviceSaveDTO dto) {
        checkDeviceCodeUnique(dto.getDeviceCode(), null);
        Device device = new Device();
        copy(dto, device);
        device.setCreatedAt(LocalDateTime.now().withNano(0));
        save(device);
        return toVO(device);
    }

    @Override
    public DeviceVO update(Long id, DeviceSaveDTO dto) {
        Device device = getById(id);
        if (device == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "设备不存在");
        }
        checkDeviceCodeUnique(dto.getDeviceCode(), id);
        copy(dto, device);
        updateById(device);
        return toVO(getById(id));
    }

    @Override
    public void delete(Long id) {
        Device device = getById(id);
        if (device == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "设备不存在");
        }
        long orderCount = repairOrderMapper.selectCount(
                new LambdaQueryWrapper<RepairOrder>().eq(RepairOrder::getDeviceId, id));
        if (orderCount > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR,
                    "该设备已有 " + orderCount + " 条报修记录，不能删除");
        }
        removeById(id);
    }

    @Override
    public List<String> rooms() {
        List<Object> rooms = baseMapper.selectObjs(new QueryWrapper<Device>()
                .select("DISTINCT room_name")
                .orderByAsc("room_name"));
        return rooms.stream().map(String::valueOf).toList();
    }

    private void checkDeviceCodeUnique(String deviceCode, Long excludeId) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<Device>()
                .eq(Device::getDeviceCode, deviceCode)
                .ne(excludeId != null, Device::getId, excludeId);
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "设备编号已存在");
        }
    }

    private void copy(DeviceSaveDTO dto, Device device) {
        device.setDeviceCode(dto.getDeviceCode());
        device.setDeviceName(dto.getDeviceName());
        device.setRoomName(dto.getRoomName());
        device.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : "NORMAL");
        device.setRemark(dto.getRemark());
    }

    private DeviceVO toVO(Device device) {
        DeviceVO vo = new DeviceVO();
        vo.setId(device.getId());
        vo.setDeviceCode(device.getDeviceCode());
        vo.setDeviceName(device.getDeviceName());
        vo.setRoomName(device.getRoomName());
        vo.setStatus(device.getStatus());
        vo.setStatusLabel(statusLabel(device.getStatus()));
        vo.setRemark(device.getRemark());
        vo.setCreatedAt(device.getCreatedAt());
        return vo;
    }

    private String statusLabel(String status) {
        return switch (status == null ? "" : status) {
            case "NORMAL" -> "正常";
            case "FAULT" -> "故障";
            case "REPAIRING" -> "维修中";
            default -> status;
        };
    }
}
