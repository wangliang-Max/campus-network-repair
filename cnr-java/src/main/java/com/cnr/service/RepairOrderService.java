package com.cnr.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cnr.dto.RepairOrderCreateDTO;
import com.cnr.entity.RepairOrder;
import com.cnr.vo.RepairOrderVO;


import java.util.List;

public interface RepairOrderService extends IService<RepairOrder> {

    RepairOrderVO create(RepairOrderCreateDTO dto);

    RepairOrderVO detail(Long id);

    List<RepairOrderVO> myOrders();
}
