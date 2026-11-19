#!/usr/bin/env bash
# 在已启动 Docker / Kubernetes 的机器上运行，采集课程要求的运行证据。
set -uo pipefail

NS="${NS:-enterprise-g18}"
STAMP="$(date +%Y%m%d-%H%M%S)"
OUT="evidence/${STAMP}"
mkdir -p "${OUT}"/{01-image,02-compose,03-k8s-deploy,04-logs-events,05-release-rollback,06-hpa,07-incident}

section() {
  echo "== $* =="
}

section "image"
docker version > "${OUT}/01-image/docker-version.txt" 2>&1
docker images | grep -E "g18-(api|web)|mysql|nginx" > "${OUT}/01-image/images.txt" 2>&1

section "compose"
docker compose config > "${OUT}/02-compose/config.txt" 2>&1
docker compose ps > "${OUT}/02-compose/ps.txt" 2>&1
docker compose logs --timestamps --no-color > "${OUT}/02-compose/logs.txt" 2>&1

section "k8s state"
kubectl get pods -n "${NS}" -o wide > "${OUT}/03-k8s-deploy/pods.txt" 2>&1
kubectl get deployment,service,endpoints,pvc,hpa -n "${NS}" > "${OUT}/03-k8s-deploy/resources.txt" 2>&1
kubectl get ingress -n "${NS}" > "${OUT}/03-k8s-deploy/ingress.txt" 2>&1

section "logs and events"
kubectl logs -n "${NS}" -l app=g18-backend --tail=200 --timestamps > "${OUT}/04-logs-events/backend.log" 2>&1
kubectl logs -n "${NS}" -l app=g18-frontend --tail=200 --timestamps > "${OUT}/04-logs-events/frontend.log" 2>&1
kubectl logs -n "${NS}" -l app=g18-mysql --tail=200 --timestamps > "${OUT}/04-logs-events/mysql.log" 2>&1
kubectl get events -n "${NS}" --sort-by=.lastTimestamp > "${OUT}/04-logs-events/events.txt" 2>&1

section "hpa"
kubectl get hpa -n "${NS}" > "${OUT}/06-hpa/get-hpa.txt" 2>&1
kubectl describe hpa g18-backend-hpa -n "${NS}" > "${OUT}/06-hpa/describe-hpa.txt" 2>&1
kubectl top pods -n "${NS}" > "${OUT}/06-hpa/top-pods.txt" 2>&1

echo "证据已写入 ${OUT}"
