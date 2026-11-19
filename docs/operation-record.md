# 运行与发布记录

组号：Docker G18
环境：CentOS 7.9 虚拟机（4 核 3.7GB）、Docker 26.1.4、Docker Compose v2.27.1、k3s v1.30.4+k3s1
采集时间：2026-11-19 17:00–17:40

## 1. 镜像

| 镜像 | 大小 | 说明 |
| --- | --- | --- |
| `g18-api:v1` | 258MB | Alpine 构建阶段安装 JDK 21 + Maven，运行阶段仅含 JRE |
| `g18-web:v1` | 49.8MB | Nginx 运行阶段，静态资源来自 `npm run build` 的 dist |
| `mysql:8.0` | 799MB | 官方镜像 |

证据：`evidence/01-image/images.txt`、`history-api.txt`、`history-web.txt`。

## 2. Docker Compose

```bash
cd /home/wangliang/cnr
docker compose up -d
docker compose ps
```

| 检查项 | 结果 |
| --- | --- |
| 三服务状态 | `g18-mysql`、`g18-backend` 均 healthy，`g18-frontend` Up |
| 端口 | 只暴露前端 `8081:80`，后端与 MySQL 仅在自定义网络内 |
| 首页 | `GET http://localhost:8081/` 返回 HTTP 200 |
| 登录 | `POST /api/auth/login` 经 Nginx 代理返回 HTTP 200 |
| 数据持久化 | 删除并重建 `g18-mysql` 容器前后，报修单数量均为 17 |

证据：`evidence/02-compose/config.txt`、`ps.txt`、`logs.txt`、`http-check.txt`、`persistence.txt`。

## 3. Kubernetes

```bash
kubectl apply -f k8s/
kubectl get pods,svc,endpoints,pvc,hpa,ingress -n enterprise-g18
```

| 检查项 | 结果 |
| --- | --- |
| 节点 | `wl100` Ready，v1.30.4+k3s1 |
| Pod | 2 个 backend、2 个 frontend、1 个 mysql，全部 1/1 Running |
| Endpoints | backend `10.42.0.9:8080,10.42.0.10:8080`；frontend 两个地址；mysql `10.42.0.13:3306` |
| PVC | `g18-mysql-pvc` Bound，5Gi，`g18-mysql-pv` |
| Ingress | `g18-ingress`，Host `g18.course.local`，`GET /` 返回 200，`/api/health` 返回 UP |
| HPA | 初始 `cpu: 2%/60%`，压测时升至 `223%/60%`，副本 2 → 4 |

说明：本机 k3s 自带 Traefik，证据采集时 Ingress 使用 `ingressClassName: traefik`；
提交的 `k8s/07-ingress.yaml` 保持 `nginx`，在标准 nginx ingress 集群直接使用。

证据：`evidence/03-k8s-deploy/`、`evidence/04-logs-events/`、`evidence/06-hpa/`。

## 4. 发布与回滚

| 步骤 | 命令 | 结果 |
| --- | --- | --- |
| 发布 v2 | `kubectl set image deployment/g18-backend backend=g18-api:v2` | `successfully rolled out` |
| 回滚 v1 | `kubectl set image deployment/g18-backend backend=g18-api:v1` | `successfully rolled out` |
| 手动扩容 | `kubectl scale deployment/g18-backend --replicas=3` | 3 个 backend Pod Running |
| 手动缩容 | `kubectl scale deployment/g18-backend --replicas=2` | 回到 2 个 backend Pod |

`kubectl rollout history` 记录了 revision 1 → 2 → 3 的变化。
证据：`evidence/05-release-rollback/`。

## 5. HPA 压测

启动 6 个 `load` Pod 循环请求 `http://g18-backend:8080/api/health`：

| 时间 | HPA 观测 |
| --- | --- |
| 压测前 | `cpu: 2%/60%`，replicas 2 |
| 10s | `cpu: 48%/60%`，replicas 2 |
| 20s | `cpu: 223%/60%`，replicas 4 |
| 70s | `cpu: 56%/60%`，replicas 4 |
| 停止压测后约 10 分钟 | `cpu: 2%/60%`，replicas 回到 2 |

扩容后 4 个 backend Pod 的 CPU 约 90–122m，说明 `requests.cpu: 200m` 是利用率计算的前提。
证据：`evidence/06-hpa/load-test.txt`、`hpa-after.txt`、`scale-down.txt`。

## 6. F3 故障工单

按 G18 对应 F3（发布镜像故障）执行：

```bash
kubectl set image deployment/g18-backend backend=g18-api:v9-not-exist
```

Events 记录：

```text
Failed to pull image "g18-api:v9-not-exist": ... 403 Forbidden
Error: ErrImagePull
Error: ImagePullBackOff
Back-off pulling image "g18-api:v9-not-exist"
```

恢复：

```bash
kubectl set image deployment/g18-backend backend=g18-api:v1
kubectl rollout status deployment/g18-backend
```

恢复后 2 个 backend Pod 1/1 Running，Endpoints 重新有地址。
证据：`evidence/07-incident/`。

## 7. 复现命令

```bash
# 镜像
docker build -t g18-api:v1 ./backend
cd frontend && npm ci && npm run build && cd ..
docker build -t g18-web:v1 ./frontend

# Compose
cp .env.example .env
docker compose up -d

# K8s
kubectl apply -f /tmp/g18-secret.yaml
kubectl apply -f k8s/

# 证据
bash scripts/capture-evidence.sh
bash scripts/incident-f3.sh
```
