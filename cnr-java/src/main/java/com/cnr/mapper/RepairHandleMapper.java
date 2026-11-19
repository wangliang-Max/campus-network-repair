package com.cnr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cnr.entity.RepairHandle;
import com.cnr.vo.RepairHandleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RepairHandleMapper extends BaseMapper<RepairHandle> {

    @Select("""
            SELECT h.id, h.handler_id, u.real_name AS handler_name, h.action, h.content, h.created_at
            FROM repair_handle h
            LEFT JOIN sys_user u ON u.id = h.handler_id
            WHERE h.order_id = #{orderId}
            ORDER BY h.id
            """)
    List<RepairHandleVO> selectVOByOrderId(@Param("orderId") Long orderId);
}
