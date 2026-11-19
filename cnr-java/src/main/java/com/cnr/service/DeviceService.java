package com.cnr.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cnr.common.PageResult;
import com.cnr.dto.DeviceQuery;
import com.cnr.dto.DeviceSaveDTO;
import com.cnr.entity.Device;
import com.cnr.vo.DeviceVO;

import java.util.List;

public interface DeviceService extends IService<Device> {

    PageResult<DeviceVO> page(DeviceQuery query);

    DeviceVO detail(Long id);

    DeviceVO create(DeviceSaveDTO dto);

    DeviceVO update(Long id, DeviceSaveDTO dto);

    void delete(Long id);

    List<String> rooms();
}
