package com.cnr.service;

import com.cnr.vo.StatsOverviewVO;
import com.cnr.vo.TrendPointVO;

import java.util.List;

public interface StatsService {

    StatsOverviewVO overview();

    List<TrendPointVO> trend(int days);
}
