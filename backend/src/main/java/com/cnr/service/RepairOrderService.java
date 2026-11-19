package com.cnr.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cnr.common.PageResult;
import com.cnr.dto.ConfirmDTO;
import com.cnr.dto.OrderActionDTO;
import com.cnr.dto.RepairOrderCreateDTO;
import com.cnr.dto.RepairOrderQuery;
import com.cnr.dto.SupplementDTO;
import com.cnr.entity.RepairOrder;
import com.cnr.vo.RepairOrderDetailVO;
import com.cnr.vo.RepairOrderVO;

import java.util.List;

public interface RepairOrderService extends IService<RepairOrder> {

    RepairOrderVO create(RepairOrderCreateDTO dto);

    RepairOrderDetailVO detail(Long id);

    PageResult<RepairOrderVO> page(RepairOrderQuery query);

    List<RepairOrderVO> myOrders();

    /** 技术员受理：PENDING -> INVESTIGATING */
    RepairOrderDetailVO accept(Long id, OrderActionDTO dto);

    /** 开始维修：INVESTIGATING -> REPAIRING */
    RepairOrderDetailVO startRepair(Long id, OrderActionDTO dto);

    /** 修复完成：REPAIRING -> PENDING_CONFIRM */
    RepairOrderDetailVO finish(Long id, OrderActionDTO dto);

    /** 无法复现退回补充：INVESTIGATING -> INFO_NEEDED */
    RepairOrderDetailVO returnInfo(Long id, OrderActionDTO dto);

    /** 报障人补充信息：INFO_NEEDED -> PENDING */
    RepairOrderDetailVO supplement(Long id, SupplementDTO dto);

    /** 报障人验收：PENDING_CONFIRM -> CLOSED（PASS）或 REPAIRING（FAIL） */
    RepairOrderDetailVO confirm(Long id, ConfirmDTO dto);
}
