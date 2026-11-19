package com.cnr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cnr.entity.StatusLog;
import com.cnr.vo.StatusLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StatusLogMapper extends BaseMapper<StatusLog> {

    @Select("""
            SELECT l.id, l.from_status, l.to_status, l.operator_id,
                   u.real_name AS operator_name, l.remark, l.created_at
            FROM status_log l
            LEFT JOIN sys_user u ON u.id = l.operator_id
            WHERE l.order_id = #{orderId}
            ORDER BY l.id
            """)
    List<StatusLogVO> selectVOByOrderId(@Param("orderId") Long orderId);
}
