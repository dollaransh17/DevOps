#!/usr/bin/env bash
set -euo pipefail
base="${1:-http://localhost:8080}"
python3 - "$base" <<'PY'
import json,sys,urllib.request,urllib.error
base=sys.argv[1].rstrip('/')
for path in ['/health','/units','/convert?from=km&to=miles&value=5']:
    with urllib.request.urlopen(base+path,timeout=10) as r:
        data=json.load(r)
        assert r.status==200
        if path=='/health': assert data['status']=='UP'
        if path.startswith('/convert'): assert abs(data['result']-3.1068559612)<1e-9
        print(path, json.dumps(data))
try:
    urllib.request.urlopen(base+'/convert?from=kg&to=m&value=1',timeout=10)
    raise AssertionError('Expected HTTP 400')
except urllib.error.HTTPError as e:
    assert e.code==400
    print('Invalid conversion correctly rejected:',e.code)
PY
