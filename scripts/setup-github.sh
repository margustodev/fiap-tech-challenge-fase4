#!/usr/bin/env bash
# Configura variaveis, Environments, secrets e protecao da main no GitHub.
# Exige GitHub CLI autenticado com permissao de admin no repositorio.
#
# Uso: bash scripts/setup-github.sh <role-dev> <role-prod> <bucket> <admin-email> <sender-email>
# Os tres primeiros valores sao os outputs da stack de bootstrap.
# Para usar em um fork, defina GITHUB_REPO=dono/repo e PROD_APPROVERS="login1 login2".
set -euo pipefail

if [ "$#" -ne 5 ]; then
  echo "Uso: $0 <role-dev> <role-prod> <bucket> <admin-email> <sender-email>" >&2
  exit 2
fi

DEV_ROLE_ARN="$1"
PROD_ROLE_ARN="$2"
BUCKET="$3"
ADMIN_EMAIL="$4"
SENDER_EMAIL="$5"
REPO="${GITHUB_REPO:-margustodev/fiap-tech-challenge-fase4}"
APPROVERS="${PROD_APPROVERS:-git-lucasoliveira margustodev}"

echo "==> Variavel do repositorio"
gh variable set ARTIFACT_BUCKET --repo "$REPO" --body "$BUCKET"

reviewers=""
for login in $APPROVERS; do
  id="$(gh api "users/$login" --jq .id)"
  reviewers="$reviewers{\"type\":\"User\",\"id\":$id},"
done
reviewers="${reviewers%,}"

echo "==> Environment dev"
gh api -X PUT "repos/$REPO/environments/dev" --silent

echo "==> Environment prod (com aprovacao e somente a partir da main)"
gh api -X PUT "repos/$REPO/environments/prod" --silent --input - <<JSON
{
  "reviewers": [$reviewers],
  "deployment_branch_policy": {
    "protected_branches": false,
    "custom_branch_policies": true
  }
}
JSON
if ! gh api "repos/$REPO/environments/prod/deployment-branch-policies" --jq '.branch_policies[].name' | grep -qx main; then
  gh api -X POST "repos/$REPO/environments/prod/deployment-branch-policies" \
    --silent -f name=main -f type=branch
fi

echo "==> Role de deploy de cada Environment"
gh variable set AWS_DEPLOY_ROLE_ARN --repo "$REPO" --env dev --body "$DEV_ROLE_ARN"
gh variable set AWS_DEPLOY_ROLE_ARN --repo "$REPO" --env prod --body "$PROD_ROLE_ARN"

echo "==> Secrets dos Environments"
for environment in dev prod; do
  gh secret set ADMIN_EMAIL --repo "$REPO" --env "$environment" --body "$ADMIN_EMAIL"
  gh secret set SENDER_EMAIL --repo "$REPO" --env "$environment" --body "$SENDER_EMAIL"
done

echo "==> Protecao da main"
gh api -X PUT "repos/$REPO/branches/main/protection" --silent --input - <<'JSON'
{
  "required_status_checks": {
    "strict": false,
    "contexts": ["Build e testes", "Integracao DynamoDB", "Validacao da infraestrutura"]
  },
  "enforce_admins": false,
  "required_pull_request_reviews": null,
  "restrictions": null
}
JSON

echo "Configuracao concluida."
