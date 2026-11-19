# 证据目录

在虚拟机/集群上执行 `scripts/capture-evidence.sh` 与 `scripts/incident-f3.sh` 后，把生成的目录放到这里。
每份证据保留命令、时间戳和观察结论，不得出现真实密码或 Token。

| 目录 | 内容 |
| --- | --- |
| `01-image/` | `docker images`、镜像构建日志、镜像标签 |
| `02-compose/` | `docker compose config`、`ps`、`logs --timestamps` |
| `03-k8s-deploy/` | `kubectl apply`、Pod/Service/PVC/HPA 状态、Endpoints |
| `04-logs-events/` | 三个服务的日志、`describe pod`、`get events` |
| `05-release-rollback/` | v1→v2 发布、异常发布、`rollout undo`、手动 scale |
| `06-hpa/` | `get hpa`、`describe hpa`、`top pods`、压测前后副本数 |
| `07-incident/` | F3 镜像故障的 Pod 状态、Events、回滚与恢复验证 |

采集时间与结论写在每个目录的 `README.md` 里，格式：

```text
采集时间：2026-11-XX HH:mm
命令：kubectl get pods -n enterprise-g18 -o wide
观察：3 个 Pod 均 Running，READY 2/2
```
