package com.cnr.service.impl;

import com.cnr.mapper.RepairOrderMapper;
import com.cnr.service.StatsService;
import com.cnr.vo.NameCountVO;
import com.cnr.vo.StatsOverviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final RepairOrderMapper repairOrderMapper;

    @Override
    public StatsOverviewVO overview() {
        long total = repairOrderMapper.selectCount(null);
        long open = repairOrderMapper.countOpen();

        StatsOverviewVO vo = new StatsOverviewVO();
        vo.setTotalOrders(total);
        vo.setOpenOrders(open);
        vo.setClosedOrders(total - open);

        BigDecimal avg = repairOrderMapper.avgRepairHours();
        vo.setAvgRepairHours(avg == null ? BigDecimal.ZERO : avg);

        List<NameCountVO> byRoom = repairOrderMapper.countOpenByRoom();
        List<NameCountVO> byStatus = repairOrderMapper.countByStatus();
        vo.setByRoom(byRoom);
        vo.setByStatus(byStatus);
        return vo;
    }
}
