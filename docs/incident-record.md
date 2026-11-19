# 故障工单 INC-G18-001

| 项目 | 内容 |
| --- | --- |
| 事件编号 | INC-G18-001 |
| 故障类型 | F3 发布镜像故障 |
| 受影响服务 | g18-backend |
| 发现时间 | 执行发布后 1 分钟内（以 evidence 时间戳为准） |
| 影响范围 | 后端新 Pod 无法启动，旧版本 Pod 仍可服务，前端功能降级但不完全中断 |

## 1. 业务现象

把 `g18-backend` 的镜像从 `g18-api:v1` 更新为一个不存在的标签 `g18-api:v9-not-exist` 后，
`kubectl rollout status` 长时间不结束，新 Pod 处于 `ImagePullBackOff` / `ErrImagePull`，
`kubectl get pods` 中 READY 数量不增加。

## 2. 状态检查

```bash
kubectl get pods -n enterprise-g18 -o wide
kubectl get deployment g18-backend -n enterprise-g18
kubectl rollout status deployment/g18-backend -n enterprise-g18
```

## 3. 日志与 Events 证据

```bash
kubectl describe pod <新 Pod 名> -n enterprise-g18
kubectl get events -n enterprise-g18 --sort-by=.lastTimestamp
```

Events 中出现：

```text
Failed to pull image "g18-api:v9-not-exist": ... not found
Error: ErrImagePull
Back-off pulling image "g18-api:v9-not-exist"
```

具体输出见 `evidence/07-incident/`（由脚本采集，带时间戳）。

## 4. 根因

发布时使用了仓库中不存在的镜像标签。Kubernetes 只会拉取镜像，不会回退到上一个可用标签；
Deployment 保持新 ReplicaSet，旧 Pod 继续服务，因此表现为"发布卡住但业务未完全中断"。

## 5. 最小修复

```bash
kubectl set image deployment/g18-backend backend=g18-api:v1 -n enterprise-g18
kubectl rollout status deployment/g18-backend -n enterprise-g18
```

或：

```bash
kubectl rollout undo deployment/g18-backend -n enterprise-g18
```

## 6. 恢复验证

```bash
kubectl get pods -n enterprise-g18 -l app=g18-backend
kubectl get endpoints g18-backend -n enterprise-g18
curl -H "Host: g18.course.local" http://<集群入口>/api/health
```

Pod 全部 Ready，Endpoints 非空，健康检查返回 `status=UP`。

## 7. 预防

1. 发布前用 `docker manifest inspect g18-api:v2` 或 `docker images` 确认镜像存在；
2. CI 中构建、推送、部署使用同一个不可变 tag（如 Git commit SHA）；
3. 发布前执行 `kubectl apply --dry-run=client` 与 `kubectl diff`；
4. 配置 `progressDeadlineSeconds`，让失败发布尽快暴露；
5. 保留 `kubectl rollout undo` 预案，出现 ImagePullBackOff 先回滚再排查。

## 8. 证据清单

- [x] `evidence/07-incident/01-get-pods.txt`
- [x] `evidence/07-incident/02-describe-pod.txt`
- [x] `evidence/07-incident/03-events.txt`
- [x] `evidence/07-incident/04-rollback.txt`
- [x] `evidence/07-incident/05-recovery.txt`

实际 Events 已记录 `ErrImagePull`、`ImagePullBackOff` 和回滚后 Pod 恢复 Running，见上述文件。
