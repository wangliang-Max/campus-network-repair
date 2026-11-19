package com.cnr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cnr.entity.ConfirmRecord;
import com.cnr.vo.ConfirmRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConfirmRecordMapper extends BaseMapper<ConfirmRecord> {

    @Select("""
            SELECT c.id, c.confirm_by, u.real_name AS confirm_name, c.result, c.score, c.comment, c.created_at
            FROM confirm_record c
            LEFT JOIN sys_user u ON u.id = c.confirm_by
            WHERE c.order_id = #{orderId}
            ORDER BY c.id
            """)
    List<ConfirmRecordVO> selectVOByOrderId(@Param("orderId") Long orderId);
}
