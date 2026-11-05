#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p evidence
for count in 3 5 3; do
  kubectl --context kind-devops-course -n unit-converter scale deployment/unit-converter --replicas="$count"
  kubectl --context kind-devops-course -n unit-converter rollout status deployment/unit-converter --timeout=180s
  # Wait until terminated pods disappear too, so the captured count is unambiguous.
  for attempt in {1..60}; do
    actual=$(kubectl --context kind-devops-course -n unit-converter get pods -l app=unit-converter -o name | wc -l | tr -d ' ')
    [[ "$actual" == "$count" ]] && break
    sleep 2
  done
  [[ "$actual" == "$count" ]] || { echo 'Pod count did not settle'; exit 1; }
  kubectl --context kind-devops-course -n unit-converter get pods -l app=unit-converter | tee "evidence/pods-${count}.txt"
done
