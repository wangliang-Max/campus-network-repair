-- ============================================================
-- 校园网络与机房故障报修平台 (T05) —— 数据库结构脚本
-- 适用：MySQL 8.0+
-- 说明：在空库执行一次即可成功；先执行 schema.sql，再执行 seed.sql
-- ============================================================

DROP DATABASE IF EXISTS campus_repair;
CREATE DATABASE campus_repair DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_repair;

-- ------------------------------------------------------------
-- 1. 角色表
-- ------------------------------------------------------------
CREATE TABLE sys_role (
    id        BIGINT      NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(20) NOT NULL COMMENT '角色编码：REPORTER报障人 / TECHNICIAN技术员 / ADMIN管理员',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色表';

-- ------------------------------------------------------------
-- 2. 用户表
-- ------------------------------------------------------------
CREATE TABLE sys_user (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username   VARCHAR(50)  NOT NULL COMMENT '用户名',
    password   VARCHAR(100) NOT NULL COMMENT 'BCrypt密码哈希',
    real_name  VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    phone      VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    role_id    BIGINT       NOT NULL COMMENT '角色ID',
    enabled    TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用：1启用 0禁用',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_user_role (role_id),
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ------------------------------------------------------------
-- 3. 机房设备档案表（主数据表）
-- ------------------------------------------------------------
CREATE TABLE device (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '设备ID',
    device_code VARCHAR(30)  NOT NULL COMMENT '设备编号',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    room_name   VARCHAR(50)  NOT NULL COMMENT '所在机房/房间',
    status      VARCHAR(20)  NOT NULL DEFAULT 'NORMAL' COMMENT '设备状态：NORMAL正常 / FAULT故障 / REPAIRING维修中',
    remark      VARCHAR(200) DEFAULT NULL COMMENT '备注',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建档时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_device_code (device_code),
    KEY idx_device_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '机房设备档案表（主数据）';

-- ------------------------------------------------------------
-- 4. 报修单表（业务单据表）
--    状态：PENDING待受理 / INVESTIGATING排查中 / REPAIRING修复中
--          PENDING_CONFIRM待确认 / CLOSED已关闭 / INFO_NEEDED待补充
-- ------------------------------------------------------------
CREATE TABLE repair_order (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '报修单ID',
    order_no    VARCHAR(30)  NOT NULL COMMENT '报修单号',
    reporter_id BIGINT       NOT NULL COMMENT '报障人用户ID',
    device_id   BIGINT       NOT NULL COMMENT '故障设备ID',
    fault_desc  VARCHAR(500) NOT NULL COMMENT '故障描述',
    priority    VARCHAR(10)  NOT NULL DEFAULT 'NORMAL' COMMENT '优先级：NORMAL普通 / URGENT紧急',
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '单据状态（见表头注释）',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_order_reporter (reporter_id),
    KEY idx_order_device (device_id),
    KEY idx_order_status (status),
    CONSTRAINT fk_order_reporter FOREIGN KEY (reporter_id) REFERENCES sys_user (id),
    CONSTRAINT fk_order_device FOREIGN KEY (device_id) REFERENCES device (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '报修单表（业务单据）';

-- ------------------------------------------------------------
-- 5. 处理记录表
--    动作：ACCEPT受理 / INVESTIGATE排查 / REPAIR维修 / COMPLETE完成 / RETURN退回补充
-- ------------------------------------------------------------
CREATE TABLE repair_handle (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    order_id   BIGINT       NOT NULL COMMENT '报修单ID',
    handler_id BIGINT       NOT NULL COMMENT '处理人用户ID',
    action     VARCHAR(20)  NOT NULL COMMENT '处理动作（见表头注释）',
    content    VARCHAR(500) DEFAULT NULL COMMENT '处理内容',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (id),
    KEY idx_handle_order (order_id),
    KEY idx_handle_handler (handler_id),
    CONSTRAINT fk_handle_order FOREIGN KEY (order_id) REFERENCES repair_order (id),
    CONSTRAINT fk_handle_handler FOREIGN KEY (handler_id) REFERENCES sys_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '处理记录表';

-- ------------------------------------------------------------
-- 6. 状态变更日志表
-- ------------------------------------------------------------
CREATE TABLE status_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    order_id    BIGINT       NOT NULL COMMENT '报修单ID',
    from_status VARCHAR(20)  DEFAULT NULL COMMENT '变更前状态',
    to_status   VARCHAR(20)  NOT NULL COMMENT '变更后状态',
    operator_id BIGINT       NOT NULL COMMENT '操作人用户ID',
    remark      VARCHAR(200) DEFAULT NULL COMMENT '备注',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_log_order (order_id),
    KEY idx_log_operator (operator_id),
    CONSTRAINT fk_log_order FOREIGN KEY (order_id) REFERENCES repair_order (id),
    CONSTRAINT fk_log_operator FOREIGN KEY (operator_id) REFERENCES sys_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '状态变更日志表';

-- ------------------------------------------------------------
-- 7. 确认评价表
--    说明：一张报修单允许多次确认（验收不通过→返工→再次确认），
--          因此 order_id 使用普通索引而非唯一约束
-- ------------------------------------------------------------
CREATE TABLE confirm_record (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    order_id   BIGINT       NOT NULL COMMENT '报修单ID',
    confirm_by BIGINT       NOT NULL COMMENT '确认人用户ID',
    result     VARCHAR(20)  NOT NULL COMMENT '确认结果：PASS通过 / FAIL不通过',
    score      TINYINT      DEFAULT NULL COMMENT '满意度评分1-5',
    comment    VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '确认时间',
    PRIMARY KEY (id),
    KEY idx_confirm_order (order_id),
    KEY idx_confirm_by (confirm_by),
    CONSTRAINT fk_confirm_order FOREIGN KEY (order_id) REFERENCES repair_order (id),
    CONSTRAINT fk_confirm_by FOREIGN KEY (confirm_by) REFERENCES sys_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '确认评价表';
