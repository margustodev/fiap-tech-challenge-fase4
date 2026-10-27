# Valida criação da tabela, persistência e recuperação via DynamoDB Local + testes IT.
# Pré-requisito: Docker Desktop em execução.

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$ContainerName = "fiap-dynamodb-local"
$Endpoint = "http://localhost:8000"

Write-Host "==> Subindo DynamoDB Local (Docker)..."
$existing = docker ps -a --filter "name=$ContainerName" --format "{{.Names}}"
if ($existing -eq $ContainerName) {
    docker start $ContainerName | Out-Null
} else {
    docker run -d --name $ContainerName -p 8000:8000 `
        amazon/dynamodb-local -jar DynamoDBLocal.jar -sharedDb -inMemory | Out-Null
}

Start-Sleep -Seconds 2
Write-Host "==> Endpoint: $Endpoint"

$env:DYNAMODB_ENDPOINT = $Endpoint
$env:AVALIACOES_TABLE_NAME = "dev-avaliacoes"
$env:AWS_REGION = "us-east-1"
$env:RUN_DYNAMODB_IT = "true"

Write-Host "==> Executando testes de integração (persistência/recuperação)..."
Push-Location $Root
try {
    mvn -pl common -am test "-Dtest=AvaliacaoEntityMapperTest,DynamoDbAvaliacaoRepositoryTest,DynamoDbAvaliacaoRepositoryIT"
    Write-Host "==> OK: modelagem, índices (periodo-index) e CRUD validados."
}
finally {
    Pop-Location
}
