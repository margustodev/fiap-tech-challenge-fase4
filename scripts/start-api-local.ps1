# Sobe DynamoDB Local + API SAM com env vars corretas (evita timeout de 30s).
# Uso: na raiz do repo -> .\scripts\start-api-local.ps1

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$ContainerName = "fiap-dynamodb-local"
$EndpointHost = "http://localhost:8000"

Write-Host "==> 1/4 Verificando Docker..."
docker info | Out-Null

Write-Host "==> 2/4 DynamoDB Local..."
$existing = docker ps -a --filter "name=$ContainerName" --format "{{.Names}}"
if ($existing -eq $ContainerName) {
    $running = docker ps --filter "name=$ContainerName" --format "{{.Names}}"
    if ($running -ne $ContainerName) {
        docker start $ContainerName | Out-Null
    }
} else {
    # -sharedDb garante a mesma tabela independente de access key
    docker run -d --name $ContainerName -p 8000:8000 `
        amazon/dynamodb-local -jar DynamoDBLocal.jar -sharedDb -inMemory | Out-Null
}
Start-Sleep -Seconds 2

Write-Host "==> 3/4 Garantindo tabela dev-avaliacoes..."
$env:DYNAMODB_ENDPOINT = $EndpointHost
$env:AVALIACOES_TABLE_NAME = "dev-avaliacoes"
$env:AWS_REGION = "us-east-1"
$env:RUN_DYNAMODB_IT = "true"
Push-Location $Root
try {
    mvn -pl common -am -q test "-Dtest=DynamoDbAvaliacaoRepositoryIT"
} finally {
    Pop-Location
}

Write-Host "==> 4/4 Build + sam local start-api (env-local.json)..."
Push-Location $Root
try {
    mvn -q clean package -DskipTests
    Set-Location (Join-Path $Root "infrastructure")
    Write-Host ""
    Write-Host "API em http://127.0.0.1:3000/avaliacao"
    Write-Host "Exemplo:"
    Write-Host '  Invoke-RestMethod -Method Post -Uri http://127.0.0.1:3000/avaliacao -ContentType "application/json" -Body ''{"descricao":"Teste local","nota":2}'''
    Write-Host ""
    sam local start-api --port 3000 `
      --env-vars env-local.json `
      --parameter-overrides "DynamoDbEndpoint=http://host.docker.internal:8000 EnvironmentName=dev AdminEmail=admin@example.com SenderEmail=noreply@example.com"
} finally {
    Pop-Location
}
