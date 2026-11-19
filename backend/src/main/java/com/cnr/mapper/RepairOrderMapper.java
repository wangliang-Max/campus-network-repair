package com.cnr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnr.dto.RepairOrderQuery;
import com.cnr.entity.RepairOrder;
import com.cnr.vo.NameCountVO;
import com.cnr.vo.RepairOrderVO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface RepairOrderMapper extends BaseMapper<RepairOrder> {

    /** 多条件分页：JOIN 用户与设备，一次查出展示字段，避免 N+1 */
    IPage<RepairOrderVO> selectPageVO(Page<RepairOrderVO> page,
                                      @Param("q") RepairOrderQuery query,
                                      @Param("reporterId") Long reporterId);

    /** 详情：同样一次 JOIN 查出报障人、设备、机房 */
    RepairOrderVO selectDetailVO(@Param("id") Long id);

    List<RepairOrderVO> selectByReporter(@Param("reporterId") Long reporterId);

    long countOpen();

    BigDecimal avgRepairHours();

    List<NameCountVO> countOpenByRoom();

    List<NameCountVO> countByStatus();

    List<NameCountVO> countCreatedByDay(@Param("days") int days);

    List<NameCountVO> countClosedByDay(@Param("days") int days);
}
