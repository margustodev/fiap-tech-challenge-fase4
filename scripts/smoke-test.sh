#!/usr/bin/env bash
# Verificacao pos-deploy da API de avaliacoes.
#
# Uso: bash scripts/smoke-test.sh <url-do-endpoint> <full|readonly>
#   full      valida a rejeicao de payload invalido e grava uma avaliacao de nota 10
#   readonly  valida somente a rejeicao de payload invalido, sem gravar dados
set -euo pipefail

if [ "$#" -ne 2 ] || { [ "$2" != "full" ] && [ "$2" != "readonly" ]; }; then
  echo "Uso: $0 <url-do-endpoint> <full|readonly>" >&2
  exit 2
fi

URL="$1"
MODE="$2"
ATTEMPTS="${SMOKE_ATTEMPTS:-6}"
RETRY_DELAY="${SMOKE_RETRY_DELAY:-5}"

# Tenta mais de uma vez porque o cold start da Lambda Java pode estourar o tempo da API.
expect_status() {
  local description="$1" payload="$2" expected="$3" code attempt
  for ((attempt = 1; attempt <= ATTEMPTS; attempt++)); do
    code="$(curl -sS -o /dev/null -w '%{http_code}' --max-time 35 -X POST \
      -H 'Content-Type: application/json' -d "$payload" "$URL" || true)"
    if [ "$code" = "$expected" ]; then
      echo "OK: $description (HTTP $code)"
      return 0
    fi
    echo "Tentativa $attempt/$ATTEMPTS de '$description': HTTP $code, esperado $expected"
    if [ "$attempt" -lt "$ATTEMPTS" ]; then
      sleep "$RETRY_DELAY"
    fi
  done
  echo "ERRO: $description falhou" >&2
  return 1
}

expect_status "rejeita payload invalido" '{"descricao":"","nota":11}' 400

if [ "$MODE" = "full" ]; then
  # Nota 10 tem urgencia BAIXA e por isso nao dispara e-mail critico.
  expect_status "aceita avaliacao valida" '{"descricao":"Smoke test pos-deploy","nota":10}' 201
fi
