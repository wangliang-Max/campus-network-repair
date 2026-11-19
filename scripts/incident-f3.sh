#!/usr/bin/env bash
# G18 / F3 发布镜像故障：触发 -> 采集 -> 回滚 -> 恢复验证
set -uo pipefail

NS="${NS:-enterprise-g18}"
OUT="${OUT:-evidence/07-incident}"
mkdir -p "${OUT}"

echo "1) 发布不存在的镜像标签"
kubectl set image deployment/g18-backend backend=g18-api:v9-not-exist -n "${NS}"

echo "2) 等待 45 秒观察失败状态"
sleep 45
kubectl get pods -n "${NS}" -o wide > "${OUT}/01-get-pods.txt" 2>&1
POD="$(kubectl get pods -n "${NS}" -l app=g18-backend -o jsonpath='{.items[0].metadata.name}')"
kubectl describe pod "${POD}" -n "${NS}" > "${OUT}/02-describe-pod.txt" 2>&1
kubectl get events -n "${NS}" --sort-by=.lastTimestamp > "${OUT}/03-events.txt" 2>&1

echo "3) 回滚到可用标签"
kubectl set image deployment/g18-backend backend=g18-api:v1 -n "${NS}"
kubectl rollout status deployment/g18-backend -n "${NS}" --timeout=180s > "${OUT}/04-rollback.txt" 2>&1

echo "4) 恢复验证"
kubectl get pods -n "${NS}" -l app=g18-backend > "${OUT}/05-recovery.txt" 2>&1
kubectl get endpoints g18-backend -n "${NS}" >> "${OUT}/05-recovery.txt" 2>&1
echo "F3 证据已写入 ${OUT}"
