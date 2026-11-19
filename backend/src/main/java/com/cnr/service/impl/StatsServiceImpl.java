package com.cnr.service.impl;

import com.cnr.mapper.RepairOrderMapper;
import com.cnr.service.StatsService;
import com.cnr.vo.NameCountVO;
import com.cnr.vo.StatsOverviewVO;
import com.cnr.vo.TrendPointVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public List<TrendPointVO> trend(int days) {
        int range = Math.max(1, Math.min(days, 30));
        Map<String, Long> created = repairOrderMapper.countCreatedByDay(range).stream()
                .collect(Collectors.toMap(NameCountVO::getName, NameCountVO::getCount, (a, b) -> a));
        Map<String, Long> closed = repairOrderMapper.countClosedByDay(range).stream()
                .collect(Collectors.toMap(NameCountVO::getName, NameCountVO::getCount, (a, b) -> a));

        List<TrendPointVO> points = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = range - 1; i >= 0; i--) {
            String date = today.minusDays(i).toString();
            points.add(new TrendPointVO(date, created.getOrDefault(date, 0L), closed.getOrDefault(date, 0L)));
        }
        return points;
    }
}
