package com.cnr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cnr.common.ErrorCode;
import com.cnr.common.OrderStatus;
import com.cnr.common.PageResult;
import com.cnr.dto.ConfirmDTO;
import com.cnr.dto.OrderActionDTO;
import com.cnr.dto.RepairOrderCreateDTO;
import com.cnr.dto.RepairOrderQuery;
import com.cnr.dto.SupplementDTO;
import com.cnr.entity.ConfirmRecord;
import com.cnr.entity.Device;
import com.cnr.entity.RepairHandle;
import com.cnr.entity.RepairOrder;
import com.cnr.entity.StatusLog;
import com.cnr.exception.BusinessException;
import com.cnr.mapper.ConfirmRecordMapper;
import com.cnr.mapper.DeviceMapper;
import com.cnr.mapper.RepairHandleMapper;
import com.cnr.mapper.RepairOrderMapper;
import com.cnr.mapper.StatusLogMapper;
import com.cnr.security.UserContext;
import com.cnr.service.RepairOrderService;
import com.cnr.vo.RepairOrderDetailVO;
import com.cnr.vo.RepairOrderVO;
import com.cnr.vo.StatusLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder>
        implements RepairOrderService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_TECHNICIAN = "TECHNICIAN";

    private final DeviceMapper deviceMapper;
    private final StatusLogMapper statusLogMapper;
    private final RepairHandleMapper repairHandleMapper;
    private final ConfirmRecordMapper confirmRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RepairOrderVO create(RepairOrderCreateDTO dto) {
        Long userId = UserContext.getUserId();
        Device device = deviceMapper.selectById(dto.getDeviceId());
        if (device == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "设备不存在");
        }

        // 防重复提交：同一报障人 + 同一设备 + 存在未关闭单据则拒绝
        long openCount = baseMapper.selectCount(new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getReporterId, userId)
                .eq(RepairOrder::getDeviceId, dto.getDeviceId())
                .ne(RepairOrder::getStatus, OrderStatus.CLOSED.name()));
        if (openCount > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "该设备已有未关闭的报修单，请勿重复提交");
        }

        LocalDateTime now = LocalDateTime.now().withNano(0);
        RepairOrder order = new RepairOrder();
        order.setOrderNo(genOrderNo());
        order.setReporterId(userId);
        order.setDeviceId(dto.getDeviceId());
        order.setFaultDesc(dto.getFaultDesc());
        order.setPriority(StringUtils.hasText(dto.getPriority()) ? dto.getPriority() : "NORMAL");
        order.setStatus(OrderStatus.PENDING.name());
        order.setVersion(0);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        save(order);

        insertStatusLog(order.getId(), null, OrderStatus.PENDING.name(), "报障人提交报修单");
        log.info("创建报修单 id={} orderNo={} reporter={} device={}",
                order.getId(), order.getOrderNo(), userId, order.getDeviceId());
        return buildDetail(order);
    }

    @Override
    public RepairOrderDetailVO detail(Long id) {
        RepairOrder order = requireOrder(id);
        if (!isOwner(order) && !isStaff(UserContext.getRoleCode())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看该报修单");
        }
        return buildDetail(order);
    }

    @Override
    public PageResult<RepairOrderVO> page(RepairOrderQuery query) {
        int pageNo = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int pageSize = query.getSize() == null || query.getSize() < 1 ? 10 : Math.min(query.getSize(), 100);

        // 报障人只能看自己的单；技术员/管理员可看全部，也可用 onlyMine 过滤
        Long reporterId = null;
        if (!isStaff(UserContext.getRoleCode()) || Boolean.TRUE.equals(query.getOnlyMine())) {
            reporterId = UserContext.getUserId();
        }

        IPage<RepairOrderVO> result = baseMapper.selectPageVO(
                new Page<>(pageNo, pageSize), query, reporterId);
        result.getRecords().forEach(this::fillLabels);
        return PageResult.of(result);
    }

    @Override
    public List<RepairOrderVO> myOrders() {
        List<RepairOrderVO> list = baseMapper.selectByReporter(UserContext.getUserId());
        list.forEach(this::fillLabels);
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RepairOrderDetailVO accept(Long id, OrderActionDTO dto) {
        requireStaff();
        RepairOrder order = requireOrder(id);
        return transition(order, OrderStatus.INVESTIGATING, "ACCEPT", dto, "技术员受理报修单");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RepairOrderDetailVO startRepair(Long id, OrderActionDTO dto) {
        requireStaff();
        RepairOrder order = requireOrder(id);
        return transition(order, OrderStatus.REPAIRING, "REPAIR", dto, "确认故障，开始维修");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RepairOrderDetailVO finish(Long id, OrderActionDTO dto) {
        requireStaff();
        RepairOrder order = requireOrder(id);
        return transition(order, OrderStatus.PENDING_CONFIRM, "COMPLETE", dto, "修复完成，等待报障人确认");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RepairOrderDetailVO returnInfo(Long id, OrderActionDTO dto) {
        requireStaff();
        RepairOrder order = requireOrder(id);
        return transition(order, OrderStatus.INFO_NEEDED, "RETURN", dto, "无法复现故障，退回补充信息");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RepairOrderDetailVO supplement(Long id, SupplementDTO dto) {
        RepairOrder order = requireOrder(id);
        if (!isOwner(order)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有报障人本人可以补充信息");
        }
        String from = order.getStatus();
        if (!OrderStatus.canTransition(from, OrderStatus.PENDING.name())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR,
                    "当前状态为「" + OrderStatus.labelOf(from) + "」，不能补充信息");
        }

        order.setFaultDesc(dto.getFaultDesc());
        order.setStatus(OrderStatus.PENDING.name());
        updateOrderOrThrow(order);
        insertStatusLog(order.getId(), from, OrderStatus.PENDING.name(), "报障人补充故障信息，重新提交受理");
        log.info("报修单补充信息 id={} operator={}", order.getId(), UserContext.getUserId());
        return buildDetail(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RepairOrderDetailVO confirm(Long id, ConfirmDTO dto) {
        RepairOrder order = requireOrder(id);
        if (!isOwner(order)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有报障人本人可以验收");
        }
        String from = order.getStatus();
        boolean pass = "PASS".equals(dto.getResult());
        OrderStatus target = pass ? OrderStatus.CLOSED : OrderStatus.REPAIRING;
        if (!OrderStatus.canTransition(from, target.name())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR,
                    "当前状态为「" + OrderStatus.labelOf(from) + "」，不能执行验收");
        }

        order.setStatus(target.name());
        updateOrderOrThrow(order);

        ConfirmRecord record = new ConfirmRecord();
        record.setOrderId(order.getId());
        record.setConfirmBy(UserContext.getUserId());
        record.setResult(dto.getResult());
        record.setScore(dto.getScore());
        record.setComment(dto.getComment());
        confirmRecordMapper.insert(record);

        insertStatusLog(order.getId(), from, target.name(),
                pass ? "报障人验收通过，工单关闭" : "报障人验收不通过，退回返工");
        syncDeviceStatus(order.getDeviceId(), target.name());
        log.info("报修单验收 id={} result={} operator={}", order.getId(), dto.getResult(), UserContext.getUserId());
        return buildDetail(order);
    }

    /**
     * 统一状态流转：校验合法流转 -> 乐观锁更新 -> 写状态日志 -> 写处理记录 -> 联动设备状态
     */
    private RepairOrderDetailVO transition(RepairOrder order, OrderStatus target, String action,
                                           OrderActionDTO dto, String defaultRemark) {
        String from = order.getStatus();
        if (!OrderStatus.canTransition(from, target.name())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR,
                    "当前状态为「" + OrderStatus.labelOf(from) + "」，不能执行该操作");
        }

        String remark = dto != null && StringUtils.hasText(dto.getRemark())
                ? dto.getRemark() : defaultRemark;
        order.setStatus(target.name());
        updateOrderOrThrow(order);

        insertStatusLog(order.getId(), from, target.name(), remark);

        RepairHandle handle = new RepairHandle();
        handle.setOrderId(order.getId());
        handle.setHandlerId(UserContext.getUserId());
        handle.setAction(action);
        handle.setContent(dto != null && StringUtils.hasText(dto.getContent())
                ? dto.getContent() : remark);
        repairHandleMapper.insert(handle);

        syncDeviceStatus(order.getDeviceId(), target.name());
        log.info("报修单状态流转 id={} {} -> {} action={} operator={}",
                order.getId(), from, target.name(), action, UserContext.getUserId());
        return buildDetail(order);
    }

    /** 带乐观锁的更新：version 不匹配说明被别人抢先处理 */
    private void updateOrderOrThrow(RepairOrder order) {
        if (order.getVersion() == null) {
            order.setVersion(0);
        }
        // 显式刷新更新时间：实体带上旧值会覆盖 ON UPDATE CURRENT_TIMESTAMP
        order.setUpdatedAt(LocalDateTime.now().withNano(0));
        if (!updateById(order)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "单据已被其他用户处理，请刷新后重试");
        }
    }

    private void insertStatusLog(Long orderId, String from, String to, String remark) {
        StatusLog log = new StatusLog();
        log.setOrderId(orderId);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setOperatorId(UserContext.getUserId());
        log.setRemark(remark);
        statusLogMapper.insert(log);
    }

    /** 设备状态联动：开始维修置为 REPAIRING，工单关闭恢复 NORMAL */
    private void syncDeviceStatus(Long deviceId, String orderStatus) {
        if (deviceId == null) {
            return;
        }
        String newStatus = switch (orderStatus) {
            case "REPAIRING" -> "REPAIRING";
            case "CLOSED" -> "NORMAL";
            default -> null;
        };
        if (newStatus == null) {
            return;
        }
        Device device = deviceMapper.selectById(deviceId);
        if (device != null && !newStatus.equals(device.getStatus())) {
            device.setStatus(newStatus);
            deviceMapper.updateById(device);
        }
    }

    private RepairOrderDetailVO buildDetail(RepairOrder order) {
        RepairOrderVO base = baseMapper.selectDetailVO(order.getId());
        RepairOrderDetailVO vo = new RepairOrderDetailVO();
        if (base != null) {
            BeanUtils.copyProperties(base, vo);
        } else {
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setReporterId(order.getReporterId());
            vo.setDeviceId(order.getDeviceId());
            vo.setFaultDesc(order.getFaultDesc());
            vo.setPriority(order.getPriority());
            vo.setStatus(order.getStatus());
            vo.setCreatedAt(order.getCreatedAt());
            vo.setUpdatedAt(order.getUpdatedAt());
        }
        fillLabels(vo);

        List<StatusLogVO> logs = statusLogMapper.selectVOByOrderId(order.getId());
        logs.forEach(log -> {
            log.setFromStatusLabel(log.getFromStatus() == null ? null : OrderStatus.labelOf(log.getFromStatus()));
            log.setToStatusLabel(OrderStatus.labelOf(log.getToStatus()));
        });
        vo.setStatusLogs(logs);
        vo.setHandles(repairHandleMapper.selectVOByOrderId(order.getId()));
        vo.setConfirms(confirmRecordMapper.selectVOByOrderId(order.getId()));
        vo.setAllowedActions(allowedActions(order));
        return vo;
    }

    /** 当前登录用户在该状态下可执行的动作，供前端按钮渲染 */
    private List<String> allowedActions(RepairOrder order) {
        List<String> actions = new ArrayList<>();
        boolean staff = isStaff(UserContext.getRoleCode());
        boolean owner = isOwner(order);
        switch (order.getStatus()) {
            case "PENDING" -> {
                if (staff) {
                    actions.add("accept");
                }
            }
            case "INVESTIGATING" -> {
                if (staff) {
                    actions.add("start-repair");
                    actions.add("return-info");
                }
            }
            case "REPAIRING" -> {
                if (staff) {
                    actions.add("finish");
                }
            }
            case "PENDING_CONFIRM" -> {
                if (owner) {
                    actions.add("confirm");
                }
            }
            case "INFO_NEEDED" -> {
                if (owner) {
                    actions.add("supplement");
                }
            }
            default -> {
                // CLOSED 无可用动作
            }
        }
        return actions;
    }

    private RepairOrder requireOrder(Long id) {
        RepairOrder order = getById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "报修单不存在");
        }
        return order;
    }

    private boolean isOwner(RepairOrder order) {
        return Objects.equals(order.getReporterId(), UserContext.getUserId());
    }

    private boolean isStaff(String roleCode) {
        return ROLE_ADMIN.equals(roleCode) || ROLE_TECHNICIAN.equals(roleCode);
    }

    private void requireStaff() {
        if (!isStaff(UserContext.getRoleCode())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有技术员或管理员可以执行该操作");
        }
    }

    private void fillLabels(RepairOrderVO vo) {
        vo.setStatusLabel(OrderStatus.labelOf(vo.getStatus()));
        vo.setPriorityLabel("URGENT".equals(vo.getPriority()) ? "紧急" : "普通");
    }

    private String genOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return "RO" + time + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
