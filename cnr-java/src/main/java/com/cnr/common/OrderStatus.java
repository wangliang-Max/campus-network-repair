package com.cnr.common;

import lombok.Getter;

import java.util.Map;
import java.util.Set;

/**
 * 报修单状态机：集中定义状态含义与合法流转，避免散落在 if/else 里
 *
 * <pre>
 * PENDING(待受理) -> INVESTIGATING(排查中) -> REPAIRING(修复中) -> PENDING_CONFIRM(待确认) -> CLOSED(已关闭)
 *                          |                        ^
 *                          v                        |
 *                    INFO_NEEDED(待补充)      PENDING_CONFIRM 验收不通过 -> REPAIRING
 *                          |
 *                          v
 *                    PENDING(报障人补充后重新受理)
 * </pre>
 */
@Getter
public enum OrderStatus {

    PENDING("待受理"),
    INVESTIGATING("排查中"),
    REPAIRING("修复中"),
    PENDING_CONFIRM("待确认"),
    CLOSED("已关闭"),
    INFO_NEEDED("待补充");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    /** 合法流转表：key 为当前状态，value 为允许到达的状态 */
    private static final Map<String, Set<String>> ALLOWED = Map.of(
            "PENDING", Set.of("INVESTIGATING"),
            "INVESTIGATING", Set.of("REPAIRING", "INFO_NEEDED"),
            "REPAIRING", Set.of("PENDING_CONFIRM"),
            "PENDING_CONFIRM", Set.of("CLOSED", "REPAIRING"),
            "INFO_NEEDED", Set.of("PENDING"),
            "CLOSED", Set.of()
    );

    public static boolean canTransition(String from, String to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    public static String labelOf(String code) {
        for (OrderStatus status : values()) {
            if (status.name().equals(code)) {
                return status.label;
            }
        }
        return code;
    }
}
