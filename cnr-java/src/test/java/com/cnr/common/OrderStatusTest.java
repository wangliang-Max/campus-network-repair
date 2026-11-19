package com.cnr.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 状态机单元测试：主流程、异常分支、非法流转
 */
class OrderStatusTest {

    @Test
    @DisplayName("主流程：待受理 -> 排查中 -> 修复中 -> 待确认 -> 已关闭")
    void mainFlowIsAllowed() {
        assertTrue(OrderStatus.canTransition("PENDING", "INVESTIGATING"));
        assertTrue(OrderStatus.canTransition("INVESTIGATING", "REPAIRING"));
        assertTrue(OrderStatus.canTransition("REPAIRING", "PENDING_CONFIRM"));
        assertTrue(OrderStatus.canTransition("PENDING_CONFIRM", "CLOSED"));
    }

    @Test
    @DisplayName("异常分支：退回补充后可重新受理，验收不通过可返工")
    void abnormalBranchesAreAllowed() {
        assertTrue(OrderStatus.canTransition("INVESTIGATING", "INFO_NEEDED"));
        assertTrue(OrderStatus.canTransition("INFO_NEEDED", "PENDING"));
        assertTrue(OrderStatus.canTransition("PENDING_CONFIRM", "REPAIRING"));
    }

    @Test
    @DisplayName("非法流转必须被拒绝")
    void illegalTransitionsAreRejected() {
        assertFalse(OrderStatus.canTransition("PENDING", "CLOSED"));
        assertFalse(OrderStatus.canTransition("PENDING", "REPAIRING"));
        assertFalse(OrderStatus.canTransition("REPAIRING", "PENDING"));
        assertFalse(OrderStatus.canTransition("CLOSED", "PENDING"));
        assertFalse(OrderStatus.canTransition("CLOSED", "REPAIRING"));
        assertFalse(OrderStatus.canTransition("UNKNOWN", "PENDING"));
    }

    @Test
    @DisplayName("状态中文名映射")
    void labelMapping() {
        assertEquals("待受理", OrderStatus.labelOf("PENDING"));
        assertEquals("已关闭", OrderStatus.labelOf("CLOSED"));
        assertEquals("待补充", OrderStatus.labelOf("INFO_NEEDED"));
    }
}
