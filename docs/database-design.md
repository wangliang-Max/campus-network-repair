# 数据库设计说明（T05 校园网络与机房故障报修平台）

> 

## 1. 概述

本系统采用 MySQL 8.0，数据库名 `campus_repair`，字符集 `utf8mb4`。共 7 张表，覆盖方案要求的基本类型：

| 方案要求的表类型 | 本系统对应表                     |
| -------- | -------------------------- |
| 用户表      | `sys_user`（含角色 `sys_role`） |
| 主数据表     | `device`（机房设备档案）           |
| 业务单据表    | `repair_order`（报修单）        |
| 处理记录表    | `repair_handle`（处理记录）      |
| 状态变更日志表  | `status_log`（状态流转日志）       |
| 额外支撑表    | `confirm_record`（确认评价）     |

## 2. ER 图

![ER图](file:///D:/JAVA/Project/campus-network-repair/database/er-diagram.png)

**关系总览（乌鸦脚标注）：**

- `sys_role` 1─N `sys_user`：一个角色对应多个用户
- `sys_user` 1─N `repair_order`：报障人提交多张报修单
- `device` 1─N `repair_order`：一台设备可有多次故障报修
- `repair_order` 1─N `repair_handle`：一张报修单有多条处理记录
- `repair_order` 1─N `status_log`：一张报修单有多条状态变更日志
- `repair_order` 0..N `confirm_record`：一张报修单可多次确认（验收不通过→返工→再次确认）
- `sys_user` 1─N `repair_handle` / `status_log` / `confirm_record`：技术员处理、操作人记录、确认人评价

## 3. 表结构说明

### 3.1 sys_role —— 角色表

| 字段        | 类型          | 约束                 | 说明                             |
| --------- | ----------- | ------------------ | ------------------------------ |
| id        | BIGINT      | PK, AUTO_INCREMENT | 角色ID                           |
| role_code | VARCHAR(20) | **UNIQUE**         | 角色编码：REPORTER/TECHNICIAN/ADMIN |
| role_name | VARCHAR(50) | NOT NULL           | 角色名称                           |

**索引**：`uk_role_code`（唯一）：保证角色编码不重复，注册/鉴权按编码定位角色。

### 3.2 sys_user —— 用户表

| 字段         | 类型           | 约束                        | 说明                |
| ---------- | ------------ | ------------------------- | ----------------- |
| id         | BIGINT       | PK, AUTO_INCREMENT        | 用户ID              |
| username   | VARCHAR(50)  | **UNIQUE**                | 用户名               |
| password   | VARCHAR(100) | NOT NULL                  | BCrypt 密码哈希（不存明文） |
| real_name  | VARCHAR(50)  |                           | 真实姓名              |
| phone      | VARCHAR(20)  |                           | 手机号               |
| role_id    | BIGINT       | **FK → sys_role.id**      | 角色ID              |
| enabled    | TINYINT(1)   | DEFAULT 1                 | 是否启用：1启用/0禁用      |
| created_at | DATETIME     | DEFAULT CURRENT_TIMESTAMP | 创建时间              |

**索引**：`uk_username`（唯一）——登录查询用户名、防重名注册；`idx_user_role`——按角色筛选用户。

### 3.3 device —— 机房设备档案表（主数据）

| 字段          | 类型           | 约束                        | 说明                          |
| ----------- | ------------ | ------------------------- | --------------------------- |
| id          | BIGINT       | PK, AUTO_INCREMENT        | 设备ID                        |
| device_code | VARCHAR(30)  | **UNIQUE**                | 设备编号                        |
| device_name | VARCHAR(100) | NOT NULL                  | 设备名称                        |
| room_name   | VARCHAR(50)  | NOT NULL                  | 所在机房/房间                     |
| status      | VARCHAR(20)  | DEFAULT 'NORMAL'          | 设备状态：NORMAL/FAULT/REPAIRING |
| remark      | VARCHAR(200) |                           | 备注                          |
| created_at  | DATETIME     | DEFAULT CURRENT_TIMESTAMP | 建档时间                        |

**索引**：`uk_device_code`（唯一）——设备编号全局唯一；`idx_device_status`——按设备状态（正常/故障/维修中）筛选。

### 3.4 repair_order —— 报修单表（业务单据，核心）

| 字段          | 类型           | 约束                          | 说明                |
| ----------- | ------------ | --------------------------- | ----------------- |
| id          | BIGINT       | PK, AUTO_INCREMENT          | 报修单ID             |
| order_no    | VARCHAR(30)  | **UNIQUE**                  | 报修单号              |
| reporter_id | BIGINT       | **FK → sys_user.id**        | 报障人用户ID           |
| device_id   | BIGINT       | **FK → device.id**          | 故障设备ID            |
| fault_desc  | VARCHAR(500) | NOT NULL                    | 故障描述              |
| priority    | VARCHAR(10)  | DEFAULT 'NORMAL'            | 优先级：NORMAL/URGENT |
| status      | VARCHAR(20)  | DEFAULT 'PENDING'           | 单据状态（见 4.1）       |
| version     | INT          | DEFAULT 0                   | **乐观锁版本号**        |
| created_at  | DATETIME     | DEFAULT CURRENT_TIMESTAMP   | 创建时间              |
| updated_at  | DATETIME     | ON UPDATE CURRENT_TIMESTAMP | 更新时间              |

**索引**：`uk_order_no`（唯一）——报修单号唯一，是防重复提交的第一道闸；`idx_order_reporter`——查"我的报修"；`idx_order_device`——按设备查历史；`idx_order_status`——按状态列表/统计。

### 3.5 repair_handle —— 处理记录表

| 字段         | 类型           | 约束                        | 说明                                           |
| ---------- | ------------ | ------------------------- | -------------------------------------------- |
| id         | BIGINT       | PK, AUTO_INCREMENT        | 记录ID                                         |
| order_id   | BIGINT       | **FK → repair_order.id**  | 报修单ID                                        |
| handler_id | BIGINT       | **FK → sys_user.id**      | 处理人用户ID                                      |
| action     | VARCHAR(20)  | NOT NULL                  | 动作：ACCEPT/INVESTIGATE/REPAIR/COMPLETE/RETURN |
| content    | VARCHAR(500) |                           | 处理内容                                         |
| created_at | DATETIME     | DEFAULT CURRENT_TIMESTAMP | 记录时间                                         |

**索引**：`idx_handle_order`——按单查处理历程；`idx_handle_handler`——按处理人查工作量。

### 3.6 status_log —— 状态变更日志表

| 字段          | 类型           | 约束                        | 说明       |
| ----------- | ------------ | ------------------------- | -------- |
| id          | BIGINT       | PK, AUTO_INCREMENT        | 日志ID     |
| order_id    | BIGINT       | **FK → repair_order.id**  | 报修单ID    |
| from_status | VARCHAR(20)  |                           | 变更前状态    |
| to_status   | VARCHAR(20)  | NOT NULL                  | 变更后状态    |
| operator_id | BIGINT       | **FK → sys_user.id**      | 操作人用户ID  |
| remark      | VARCHAR(200) |                           | 备注（流转原因） |
| created_at  | DATETIME     | DEFAULT CURRENT_TIMESTAMP | 操作时间     |

**索引**：`idx_log_order`——按单回放状态轨迹；`idx_log_operator`——按操作人审计。

### 3.7 confirm_record —— 确认评价表

| 字段         | 类型           | 约束                        | 说明                |
| ---------- | ------------ | ------------------------- | ----------------- |
| id         | BIGINT       | PK, AUTO_INCREMENT        | 记录ID              |
| order_id   | BIGINT       | **FK → repair_order.id**  | 报修单ID             |
| confirm_by | BIGINT       | **FK → sys_user.id**      | 确认人用户ID           |
| result     | VARCHAR(20)  | NOT NULL                  | 结果：PASS通过/FAIL不通过 |
| score      | TINYINT      |                           | 满意度评分 1-5         |
| comment    | VARCHAR(500) |                           | 评价内容              |
| created_at | DATETIME     | DEFAULT CURRENT_TIMESTAMP | 确认时间              |

**索引**：`idx_confirm_order`——按单查验收记录；`idx_confirm_by`——按确认人统计。
**设计说明**：`order_id` 使用**普通索引而非唯一约束**——因为支持"验收不通过 → 返工 → 再次确认"的异常分支，同一张单允许存在 FAIL 与 PASS 多次确认记录，业务上以最新一条为准，单据是否关闭由 `repair_order.status` 决定。

## 4. 状态与枚举说明

### 4.1 repair_order.status（单据状态，共 6 个）

| 状态              | 含义          | 允许的下一状态                            |
| --------------- | ----------- | ---------------------------------- |
| PENDING         | 待受理（报障人已提交） | INVESTIGATING（技术员受理）、CLOSED（非法，拒绝） |
| INVESTIGATING   | 排查中         | REPAIRING、INFO_NEEDED（无法复现退回补充）    |
| REPAIRING       | 修复中         | PENDING_CONFIRM                    |
| PENDING_CONFIRM | 待确认         | CLOSED（验收通过）、REPAIRING（验收不通过返工）    |
| CLOSED          | 已关闭         | 终态                                 |
| INFO_NEEDED     | 待补充（异常分支）   | INVESTIGATING（补充后重新排查）             |

### 4.2 其他枚举

- `device.status`：NORMAL 正常 / FAULT 故障 / REPAIRING 维修中
- `repair_order.priority`：NORMAL 普通 / URGENT 紧急
- `repair_handle.action`：ACCEPT 受理 / INVESTIGATE 排查 / REPAIR 维修 / COMPLETE 完成 / RETURN 退回补充
- `confirm_record.result`：PASS 通过 / FAIL 不通过

## 5. 索引说明汇总

| 类型   | 字段                                   | 目的            |
| ---- | ------------------------------------ | ------------- |
| 唯一约束 | sys_role.role_code                   | 角色编码不重复       |
| 唯一约束 | sys_user.username                    | 登录查询、防重名      |
| 唯一约束 | device.device_code                   | 设备编号唯一        |
| 唯一约束 | repair_order.order_no                | **防重复提交第一道闸** |
| 普通索引 | sys_user.role_id                     | 按角色筛选用户       |
| 普通索引 | device.status                        | 按设备状态筛选       |
| 普通索引 | repair_order.reporter_id             | "我的报修"查询      |
| 普通索引 | repair_order.device_id               | 设备历史故障查询      |
| 普通索引 | repair_order.status                  | 状态列表、状态统计     |
| 普通索引 | repair_handle.order_id / handler_id  | 处理历程、人员工作量    |
| 普通索引 | status_log.order_id / operator_id    | 状态轨迹回放、操作审计   |
| 普通索引 | confirm_record.order_id / confirm_by | 验收记录、评价统计     |

## 6. 关键设计说明（对应方案要求）

1. **表类型齐全**：用户表、主数据表、业务单据表、处理记录表、状态变更日志表共 5 类齐全，加角色与确认评价共 7 张，满足"至少 6 张表"基线。
2. **状态机支撑**：`repair_order.status` 支撑 5 个以上状态的主流程；每次流转在 `status_log` 写入操作人、时间、备注（方案 4.6 状态日志要求）。
3. **并发控制**：`repair_order.version` 乐观锁——状态流转用 `UPDATE ... WHERE id=? AND status=? AND version=?`，影响行数为 0 即冲突拒绝（方案 4.6 并发/重复提交要求）。
4. **防重复提交**：`order_no` 唯一索引兜底；业务上再校验"同一用户同一设备存在未关闭单据时禁止重复创建"。
5. **返工异常分支**：`confirm_record` 不设唯一约束，支持验收不通过后返工再确认（异常分支"验收不通过"）。
6. **外键**：均使用物理外键约束（InnoDB），保证引用完整性；主要外键字段均建索引避免联表查询全表扫描。
7. **敏感数据**：`sys_user.password` 仅存 BCrypt 哈希；seed 数据全部为模拟数据，不含真实个人信息。

## 7. 数据规模（seed.sql）

| 表              | 条数  | 覆盖情况                   |
| -------------- | --- | ---------------------- |
| sys_role       | 3   | 三种角色                   |
| sys_user       | 11  | 管理员 1、技术员 3、报障人 7      |
| device         | 12  | 5 个机房，含正常/故障/维修中       |
| repair_order   | 12  | 6 个状态全覆盖（含 2 条紧急单）     |
| repair_handle  | 39  | 覆盖受理/排查/维修/完成/退回       |
| status_log     | 32  | 含返工、退回补充两条异常分支轨迹       |
| confirm_record | 5   | 含 1 次 FAIL 验收后返工再 PASS |
