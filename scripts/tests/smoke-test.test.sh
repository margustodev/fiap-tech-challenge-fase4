#!/usr/bin/env bash
# Testa scripts/smoke-test.sh contra uma API HTTP simulada.
# Uso: bash scripts/tests/smoke-test.test.sh
set -u

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SMOKE="$ROOT/scripts/smoke-test.sh"
WORK="$(mktemp -d)"
API_PID=""
FAILURES=0

for candidate in python3 python; do
  if "$candidate" --version >/dev/null 2>&1; then PY="$candidate"; break; fi
done

cat > "$WORK/api.py" <<'PY'
import json, os, sys
from http.server import BaseHTTPRequestHandler, HTTPServer

behavior, workdir = sys.argv[1], sys.argv[2]
state = {"requests": 0}

class Api(BaseHTTPRequestHandler):
    def do_POST(self):
        body = self.rfile.read(int(self.headers["Content-Length"])).decode()
        with open(os.path.join(workdir, "requests.log"), "a") as log:
            log.write(body + "\n")
        state["requests"] += 1
        data = json.loads(body)
        valid = bool(data.get("descricao")) and 0 <= data.get("nota", -1) <= 10
        if behavior == "accept-all":
            status = 201
        elif behavior == "down":
            status = 500
        elif behavior == "flaky" and state["requests"] == 1:
            status = 503
        else:
            status = 201 if valid else 400
        self.send_response(status)
        self.send_header("Content-Length", "0")
        self.end_headers()

    def log_message(self, *args):
        pass

server = HTTPServer(("127.0.0.1", 0), Api)
with open(os.path.join(workdir, "port.tmp"), "w") as port:
    port.write(str(server.server_address[1]))
os.replace(os.path.join(workdir, "port.tmp"), os.path.join(workdir, "port"))
server.serve_forever()
PY

start_api() {
  rm -f "$WORK/port" "$WORK/requests.log"
  "$PY" "$WORK/api.py" "$1" "$WORK" &
  API_PID=$!
  for _ in $(seq 1 100); do
    [ -s "$WORK/port" ] && break
    sleep 0.1
  done
  URL="http://127.0.0.1:$(cat "$WORK/port")/avaliacao"
}

stop_api() {
  kill "$API_PID" 2>/dev/null
  wait "$API_PID" 2>/dev/null
  API_PID=""
}

cleanup() {
  [ -n "$API_PID" ] && stop_api
  rm -rf "$WORK"
}
trap cleanup EXIT

run_smoke() {
  SMOKE_ATTEMPTS=2 SMOKE_RETRY_DELAY=0 bash "$SMOKE" "$URL" "$1" >/dev/null 2>&1
}

pass() { echo "ok   - $1"; }
fail() { echo "FAIL - $1"; FAILURES=$((FAILURES + 1)); }

test_modo_completo_passa_com_api_saudavel() {
  local name="modo completo passa com API saudavel"
  start_api healthy
  if run_smoke full; then pass "$name"; else fail "$name"; fi
  stop_api
}

test_modo_completo_falha_se_api_aceita_payload_invalido() {
  local name="modo completo falha se API aceita payload invalido"
  start_api accept-all
  if run_smoke full; then fail "$name"; else pass "$name"; fi
  stop_api
}

test_modo_readonly_nao_envia_avaliacao_valida() {
  local name="modo readonly nao envia avaliacao valida"
  start_api healthy
  if run_smoke readonly && [ "$(grep -c '"nota":10' "$WORK/requests.log")" -eq 0 ]; then
    pass "$name"
  else
    fail "$name"
  fi
  stop_api
}

test_falha_transitoria_nao_reprova_o_deploy() {
  local name="falha transitoria nao reprova o deploy"
  start_api flaky
  if run_smoke full; then pass "$name"; else fail "$name"; fi
  stop_api
}

test_modo_completo_passa_com_api_saudavel
test_modo_completo_falha_se_api_aceita_payload_invalido
test_modo_readonly_nao_envia_avaliacao_valida
test_falha_transitoria_nao_reprova_o_deploy

[ "$FAILURES" -eq 0 ]
