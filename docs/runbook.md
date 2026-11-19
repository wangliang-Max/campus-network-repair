# 部署与运维手册

组号：Docker G18
命名：Namespace `enterprise-g18`，Ingress Host `g18.course.local`，镜像 `g18-api:v1` / `g18-web:v1`，服务 `g18-frontend` / `g18-backend` / `g18-mysql`。

## 1. 环境

- Docker Desktop（WSL2 后端）或 Linux 虚拟机中的 Docker
- Minikube / kind / k3s 任一集群
- kubectl、metrics-server、Ingress Controller

## 2. 镜像构建

```bash
docker build -t g18-api:v1 ./backend
cd frontend && npm ci && npm run build && cd ..
docker build -t g18-web:v1 ./frontend
docker images | grep g18
```

后端是 Maven → JRE 多阶段构建；前端先在本机执行 `npm run build`，Dockerfile 再把 dist 复制到 Nginx。
镜像标签不使用 latest。

## 3. Docker Compose

```bash
cp .env.example .env          # 修改密码后再启动，.env 不提交
docker compose config
docker compose up -d --build
docker compose ps
docker compose logs --timestamps
```

访问 <http://localhost:8081>，只暴露前端端口；后端通过 `backend:8080` 内部访问 MySQL。

验证数据持久化：

```bash
docker compose down
docker compose up -d
docker compose exec mysql mysql -uroot -p"$DB_PASSWORD" -e "select count(*) from campus_repair.repair_order;"
```

删除并重建 MySQL 容器（保留具名卷）后数据仍在；如果删除卷 `mysql-data`，数据会重新由 `schema.sql` 与 `seed.sql` 初始化。

## 4. Kubernetes 部署

### 4.1 准备镜像

Minikube：

```bash
eval $(minikube docker-env)
docker build -t g18-api:v1 ./backend
docker build -t g18-web:v1 ./frontend
```

kind：

```bash
kind load docker-image g18-api:v1
kind load docker-image g18-web:v1
```

### 4.2 启用插件

```bash
minikube addons enable ingress
minikube addons enable metrics-server
kubectl top nodes
```

### 4.3 应用资源

先复制 Secret 示例并改成自己的密码：

```bash
cp k8s/02-secret.example.yaml /tmp/g18-secret.yaml
kubectl apply -f /tmp/g18-secret.yaml
kubectl apply -f k8s/
kubectl get pods,svc,pvc,hpa -n enterprise-g18
```

`k8s/09-db-init-configmap.yaml` 内含 `schema.sql` 与 `seed.sql`，MySQL 首次启动会自动初始化。

### 4.4 访问

```bash
minikube ip
# 把 <minikube-ip> g18.course.local 写入 hosts
curl -H "Host: g18.course.local" http://<minikube-ip>/
```

## 5. 发布与回滚

构建 v2 镜像后：

```bash
docker build -t g18-api:v2 ./backend
kubectl set image deployment/g18-backend backend=g18-api:v2 -n enterprise-g18
kubectl rollout status deployment/g18-backend -n enterprise-g18
kubectl rollout history deployment/g18-backend -n enterprise-g18
```

回滚：

```bash
kubectl rollout undo deployment/g18-backend -n enterprise-g18
kubectl rollout status deployment/g18-backend -n enterprise-g18
```

手动扩缩容：

```bash
kubectl scale deployment/g18-backend --replicas=3 -n enterprise-g18
kubectl get pods -n enterprise-g18 -l app=g18-backend
kubectl scale deployment/g18-backend --replicas=2 -n enterprise-g18
```

## 6. HPA

前提：Backend 配置了 `requests.cpu`，metrics-server 正常。

```bash
kubectl get hpa -n enterprise-g18
kubectl describe hpa g18-backend-hpa -n enterprise-g18
kubectl top pods -n enterprise-g18
```

压测（任选）：

```bash
kubectl run load --image=busybox:1.36 --restart=Never -n enterprise-g18 -- \
  sh -c "while true; do wget -q -O- http://g18-backend:8080/api/health; done"
```

观察副本从 2 扩到 3–4，停止压测后回落到 2。

## 7. 常见问题

| 现象 | 排查 |
| --- | --- |
| Pod `ImagePullBackOff` | `kubectl describe pod` 看 Events；确认镜像标签存在、`imagePullPolicy` 与集群内镜像一致 |
| Pod `CrashLoopBackOff` | `kubectl logs --previous`；检查 DB_URL、DB_PASSWORD、JWT_SECRET |
| `Endpoints` 为空 | 对比 Service `selector` 与 Pod 标签 |
| PVC `Pending` | 检查 StorageClass、访问模式、PV 容量 |
| HPA `<unknown>` | 确认 `requests.cpu` 存在、metrics-server 正常、Pod 处于 Ready |
| Ingress 404 | 检查 Host、Ingress class、Service 端口 |

## 8. F3 故障工单

G18：18 ÷ 5 余 3 → **F3 发布镜像故障**。

执行脚本 `scripts/incident-f3.sh`，会自动发布不存在的镜像标签、采集 Events、再回滚到 v1，证据写入 `evidence/07-incident/`。
