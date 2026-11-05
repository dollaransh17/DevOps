#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
KIND_BIN="${KIND_BIN:-kind}"
# All kubectl operations explicitly target this project's local cluster.
if ! "$KIND_BIN" get clusters | grep -qx 'devops-course'; then
  "$KIND_BIN" create cluster --name devops-course --wait 180s
fi
docker build -t unit-converter:local .
"$KIND_BIN" load docker-image unit-converter:local --name devops-course
kubectl --context kind-devops-course apply -f k8s/namespace.yaml
kubectl --context kind-devops-course apply -f k8s/configmap.yaml -f k8s/deployment.yaml -f k8s/service.yaml
kubectl --context kind-devops-course -n unit-converter rollout restart deployment/unit-converter
kubectl --context kind-devops-course -n unit-converter rollout status deployment/unit-converter --timeout=180s
kubectl --context kind-devops-course -n unit-converter get pods,svc
printf '\nOpen a second terminal and run:\n  kubectl --context kind-devops-course -n unit-converter port-forward svc/unit-converter 8082:80\nThen visit http://localhost:8082\n'
