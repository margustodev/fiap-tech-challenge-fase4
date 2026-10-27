# FIAP Tech Challenge - Fase 4

Sistema serverless de feedbacks de cursos on-line: recebimento de avaliações, notificações automáticas para itens críticos e relatório semanal com média e estatísticas.

## Modelo de cloud escolhido

**AWS (Amazon Web Services)** no modelo **serverless / FaaS**, com os seguintes motivos:

| Critério | Decisão |
|----------|---------|
| Execução | AWS Lambda (Java 25) — cobrança por uso, alinhada a créditos limitados |
| Entrada HTTP | Amazon API Gateway (REST) — `POST /avaliacao` |
| Persistência | Amazon DynamoDB — NoSQL gerenciado, criptografia em repouso, Streams |
| Eventos críticos | DynamoDB Streams → Lambda `critical-notifier` |
| Agendamento | Amazon EventBridge (cron semanal) → Lambda `weekly-reporter` |
| Notificações | Amazon SES (e-mail) e Amazon SNS (tópico de alertas) |
| Monitoramento | Amazon CloudWatch (logs, métricas, alarms, dashboard) |
| IaC / Deploy | AWS SAM (`infrastructure/template.yaml`) |
| Segurança | IAM least privilege por função, SSE no DynamoDB, segredos via parameters/secrets |

## Arquitetura da solução

```
                    ┌─────────────────────┐
                    │   API Gateway       │
                    │   POST /avaliacao   │
                    └─────────┬───────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │ feedback-receiver   │  (Lambda 1)
                    │ valida + persiste   │
                    └─────────┬───────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │ DynamoDB Avaliações │──Streams──► critical-notifier (Lambda 2)
                    │ (SSE + PITR)        │             e-mail/SNS urgência
                    └─────────┬───────────┘
                              │
                              │ leitura semanal
                              ▼
              EventBridge ──► weekly-reporter (Lambda 3)
              (cron MON)      média + qtd/dia + qtd/urgência → SES
                              │
                              ▼
                    CloudWatch Logs / Metrics / Alarms / Dashboard
```

### Separação de responsabilidades (SRP)

| Função | Responsabilidade única | Trigger |
|--------|------------------------|---------|
| `feedback-receiver` | Receber, validar e persistir avaliação | API Gateway `POST /avaliacao` |
| `critical-notifier` | Notificar administradores sobre urgência crítica | DynamoDB Streams (INSERT) |
| `weekly-reporter` | Gerar e enviar relatório semanal | EventBridge `cron(0 12 ? * MON *)` |

## Estrutura do projeto

```
fiap-tech-challenge-fase-4/
├── pom.xml                          # Parent Maven
├── common/                          # Domínio, DTOs, portas, persistência DynamoDB
├── feedback-receiver/               # Lambda de recebimento
├── critical-notifier/               # Lambda de notificação crítica
├── weekly-reporter/                 # Lambda de relatório semanal
├── events/                          # Payloads de exemplo para sam local invoke
├── scripts/validate-dynamodb-local.ps1
└── infrastructure/
    ├── template.yaml                # Stack completo (SAM)
    ├── dynamodb-only.yaml           # Somente tabela DynamoDB
    └── samconfig.toml
```

Pacote base: `br.com.fiap.techchallenge.fase4`

## Banco de dados (DynamoDB)

### Modelagem

| Campo | Tipo | Uso |
|-------|------|-----|
| `id` | S (PK) | Identificador da avaliação |
| `descricao` | S | Texto do feedback |
| `nota` | N | 0–10 |
| `urgencia` | S | `BAIXA` / `MEDIA` / `ALTA` / `CRITICA` |
| `dataEnvio` | S (ISO-8601) | Timestamp; SK do GSI |
| `entityType` | S | Sempre `AVALIACAO`; PK do GSI |

**GSI `periodo-index`:** PK `entityType` + SK `dataEnvio` — permite `Query` por intervalo (relatório semanal), sem Scan.

**Streams:** `NEW_IMAGE` para o `critical-notifier`.

**Segurança:** SSE (criptografia em repouso) + Point-in-Time Recovery.

### Regras de acesso (IAM)

| Função | Permissões DynamoDB |
|--------|---------------------|
| `feedback-receiver` | `PutItem`, `GetItem`, `DescribeTable` (somente tabela) |
| `weekly-reporter` | `GetItem`, `Query`, `DescribeTable` (tabela + `index/*`) |
| `critical-notifier` | apenas Stream (`DescribeStream`, `GetRecords`, …) — sem leitura/escrita na tabela |

### Criar na cloud

Com credenciais AWS (`aws configure`):

```bash
# Somente a tabela
aws cloudformation deploy \
  --template-file infrastructure/dynamodb-only.yaml \
  --stack-name fiap-fase4-dynamodb \
  --parameter-overrides EnvironmentName=dev

# Ou stack completo (API + Lambdas + tabela)
cd infrastructure
sam deploy --guided
```

### Validar persistência e recuperação

**Opção A — DynamoDB Local (sem conta AWS):**

```powershell
.\scripts\validate-dynamodb-local.ps1
```

**Opção B — testes unitários (mapper + put/get):**

```bash
mvn -pl common -am test "-Dtest=AvaliacaoEntityMapperTest,DynamoDbAvaliacaoRepositoryTest"
```

Classes principais: `AvaliacaoEntity`, `DynamoDbAvaliacaoRepository`, `AvaliacoesTableInitializer`, `AvaliacoesTableSchema`.

## Contrato da API

### `POST /avaliacao`

```json
{
  "descricao": "string",
  "nota": 0
}
```

- `descricao`: obrigatória (não vazia)
- `nota`: inteiro obrigatório de **0 a 10**
- Respostas: **201** sucesso | **400** validação | **500** erro interno
- A urgência é calculada e persistida com a avaliação:

| Nota | Urgência |
|------|----------|
| 0–2 | CRITICA |
| 3–4 | ALTA |
| 5–7 | MEDIA |
| 8–10 | BAIXA |

### E-mail de aviso de urgência

- Descrição
- Urgência
- Data de envio

### Relatório semanal

- Descrição / Urgência / Data de envio (por avaliação)
- Quantidade de avaliações por dia
- Quantidade de avaliações por urgência
- Média de avaliações

## Funções criadas (documentação)

### 1. FeedbackReceiverHandler

- **Módulo:** `feedback-receiver`
- **Handler:** `br.com.fiap.techchallenge.fase4.feedback.handler.FeedbackReceiverHandler`
- **Entrada:** `APIGatewayProxyRequestEvent`
- **Fluxo previsto:** deserializar → validar → calcular urgência → salvar no DynamoDB → métricas → response HTTP

### 2. CriticalNotificationHandler

- **Módulo:** `critical-notifier`
- **Handler:** `br.com.fiap.techchallenge.fase4.notifier.handler.CriticalNotificationHandler`
- **Entrada:** `DynamodbEvent`
- **Fluxo previsto:** ler NewImage → filtrar urgência ALTA/CRITICA → enviar SES/SNS

### 3. WeeklyReportHandler

- **Módulo:** `weekly-reporter`
- **Handler:** `br.com.fiap.techchallenge.fase4.report.handler.WeeklyReportHandler`
- **Entrada:** `ScheduledEvent`
- **Fluxo previsto:** listar período semanal → agregar estatísticas → calcular média → enviar e-mail

## Segurança e governança de acesso

- **IAM:** role dedicada por Lambda, apenas ações necessárias (least privilege)
- **Dados:** DynamoDB com SSE e Point-in-Time Recovery
- **Segredos:** e-mails e parâmetros via `Parameters` do SAM (não versionar credenciais)
- **Logs:** evitar dados sensíveis; helpers em `SecurityConfig` / `DataPrivacyHelper`
- **API Gateway:** métricas habilitadas; data trace desabilitado por padrão

## Monitoramento

Configurado em `infrastructure/template.yaml`:

- Alarmes de **Errors** para cada Lambda (notificam o tópico SNS)
- **Dashboard** CloudWatch com invocações e erros
- Logs automáticos via `AWSLambdaBasicExecutionRole`

## Execução local

### Pré-requisitos

- JDK 25+, Maven 3.9+
- Docker Desktop em execução
- AWS SAM CLI (`sam --version`)
- AWS CLI (opcional para deploy na nuvem)

### 1. Build

```bash
mvn clean package
```

### 2. Subir a API local (API Gateway + Lambda + DynamoDB Local)

**Importante:** variáveis `$env:...` no PowerShell **não entram** no container da Lambda.
Sem `DYNAMODB_ENDPOINT`, a função tenta a AWS real, fica pendurada e estoura **timeout 30s / 502**.

Forma recomendada (um comando):

```powershell
.\scripts\start-api-local.ps1
```

Ou manualmente:

```powershell
# Terminal A — DynamoDB Local (com -sharedDb)
docker start fiap-dynamodb-local
# se não existir:
# docker run -d --name fiap-dynamodb-local -p 8000:8000 amazon/dynamodb-local -jar DynamoDBLocal.jar -sharedDb -inMemory

# criar/validar tabela
$env:DYNAMODB_ENDPOINT="http://localhost:8000"
$env:AVALIACOES_TABLE_NAME="dev-avaliacoes"
$env:AWS_REGION="us-east-1"
$env:RUN_DYNAMODB_IT="true"
mvn -pl common -am test "-Dtest=DynamoDbAvaliacaoRepositoryIT"

# Terminal B — API (obrigatório: parameter-overrides do endpoint)
cd C:\Project\fiap-tech-challenge-fase4
mvn clean package -DskipTests
cd infrastructure
sam local start-api --port 3000 --env-vars env-local.json `
  --parameter-overrides "DynamoDbEndpoint=http://host.docker.internal:8000"
```

> Sem `DynamoDbEndpoint`, a Lambda tenta a AWS real e retorna 500/502.
> Se a API ficar estranha após erros, limpe containers travados:
> `docker ps -q --filter ancestor=public.ecr.aws/lambda/java:25-rapid-x86_64 | % { docker rm -f $_ }`

Em outro terminal, envie uma avaliação:

```powershell
Invoke-RestMethod -Method Post -Uri http://127.0.0.1:3000/avaliacao `
  -ContentType "application/json" `
  -Body '{"descricao":"Curso com material desatualizado","nota":2}'
```

### 3. Invocar funções isoladas com eventos de exemplo

Os payloads estão em `events/`:

| Arquivo | Função |
|---------|--------|
| `events/post-avaliacao.json` | `FeedbackReceiverFunction` (nota baixa / crítica) |
| `events/post-avaliacao-positiva.json` | `FeedbackReceiverFunction` (nota alta) |
| `events/dynamodb-stream.json` | `CriticalNotifierFunction` |
| `events/schedule.json` | `WeeklyReporterFunction` |

A partir da pasta `infrastructure`:

```bash
sam local invoke FeedbackReceiverFunction -e ../events/post-avaliacao.json
sam local invoke FeedbackReceiverFunction -e ../events/post-avaliacao-positiva.json
sam local invoke CriticalNotifierFunction -e ../events/dynamodb-stream.json
sam local invoke WeeklyReporterFunction -e ../events/schedule.json
```

> **Nota:** enquanto as classes forem esqueleto, a invocação sobe o runtime, mas a resposta de negócio ainda será vazia/`null` até a implementação.

## Instruções de deploy

### Pré-requisitos adicionais (nuvem)

- AWS CLI configurado (`aws configure`)
- E-mails verificados no Amazon SES (sandbox)

### Deploy (SAM)

```bash
cd infrastructure
sam deploy --guided
```

Ou com parâmetros:

```bash
sam deploy \
  --stack-name fiap-tech-challenge-fase-4 \
  --capabilities CAPABILITY_NAMED_IAM \
  --resolve-s3 \
  --parameter-overrides \
    EnvironmentName=dev \
    AdminEmail=seu-admin@email.com \
              SenderEmail=seu-remetente@email.com
```

## Artefatos de entrega

- Repositório com código-fonte e IaC
- Vídeo de demonstração: funções ativas, API, notificações, relatório e configurações de cloud/segurança

## Status atual

Estrutura de pacotes, classes, POMs e infraestrutura **criadas (esqueleto)**. A implementação da lógica de negócio das classes está pendente.
