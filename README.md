# FIAP Tech Challenge - Fase 4

Sistema serverless de feedbacks de cursos on-line: recebimento de avaliações, notificações automáticas para itens críticos e relatório semanal com média e estatísticas.

Link do repositório: https://github.com/margustodev/fiap-tech-challenge-fase4.git

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
| IaC / Deploy | AWS SAM (`infrastructure/template.yaml` + `samconfig.toml`) |
| Segurança | IAM least privilege por função, SSE no DynamoDB, parâmetros sensíveis via Parameters do SAM |

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
                    │ (SSE + PITR)        │             e-mail/SNS (ALTA/CRITICA)
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
| `critical-notifier` | Notificar administradores sobre urgência ALTA ou CRITICA | DynamoDB Streams (INSERT) |
| `weekly-reporter` | Gerar e enviar relatório semanal | EventBridge `cron(0 12 ? * MON *)` |

## Estrutura do projeto

```
fiap-tech-challenge-fase4/
├── pom.xml                              # Parent Maven
├── common/                              # Domínio, DTOs, portas, persistência DynamoDB
├── feedback-receiver/                   # Lambda de recebimento
├── critical-notifier/                   # Lambda de notificação crítica
├── weekly-reporter/                     # Lambda de relatório semanal
├── docs/                                # Documentação técnica
│   └── MONITORAMENTO_SEGURANCA.md
├── events/                              # Payloads de exemplo para sam local invoke
├── scripts/
│   ├── start-api-local.ps1              # Sobe API local (SAM + DynamoDB Local)
│   └── validate-dynamodb-local.ps1
└── infrastructure/
    ├── template.yaml                    # Stack completo (SAM)
    ├── dynamodb-only.yaml               # Somente tabela DynamoDB
    ├── env-local.json                   # Variáveis para sam local
    └── samconfig.toml                   # Parâmetros salvos do deploy
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
| `feedback-receiver` | `PutItem` (somente tabela) |
| `weekly-reporter` | `Query` (tabela + `index/*`) |
| `critical-notifier` | Apenas DynamoDB Streams (`DescribeStream`, `GetRecords`, `GetShardIterator`, `ListStreams`) — sem leitura/escrita direta na tabela |

### Criar na cloud

Com credenciais AWS (`aws configure`):

```bash
# Somente a tabela
aws cloudformation deploy \
  --template-file infrastructure/dynamodb-only.yaml \
  --stack-name fiap-fase4-dynamodb \
  --parameter-overrides EnvironmentName=dev

# Ou stack completo (API + Lambdas + tabela) — ver seção "Instruções de deploy"
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
- **Fluxo:** deserializar → validar → calcular urgência → salvar no DynamoDB → registrar métricas → retornar resposta HTTP

### 2. CriticalNotificationHandler

- **Módulo:** `critical-notifier`
- **Handler:** `br.com.fiap.techchallenge.fase4.notifier.handler.CriticalNotificationHandler`
- **Entrada:** `DynamodbEvent`
- **Fluxo:** ler NewImage → filtrar urgência ALTA/CRITICA → enviar SES e SNS

### 3. WeeklyReportHandler

- **Módulo:** `weekly-reporter`
- **Handler:** `br.com.fiap.techchallenge.fase4.report.handler.WeeklyReportHandler`
- **Entrada:** `ScheduledEvent`
- **Fluxo:** listar período semanal → agregar estatísticas → calcular média → enviar e-mail

## Segurança e governança de acesso

- **IAM:** role dedicada por Lambda, apenas ações necessárias (least privilege)
- **Dados:** DynamoDB com SSE e Point-in-Time Recovery
- **Parâmetros sensíveis:** e-mails e configurações via `Parameters` do SAM / `samconfig.toml` (não versionar credenciais AWS)
- **Logs:** descrições mascaradas em logs via `DataPrivacyHelper` / `SecurityConfig`
- **API Gateway:** métricas habilitadas; data trace desabilitado por padrão
- **Tratamento de erros:** respostas HTTP 500 não expõem mensagens técnicas de exceções

## Monitoramento

Configurado em `infrastructure/template.yaml`:

- Alarmes de **Errors** para cada Lambda (notificam o tópico SNS)
- Alarme personalizado para erros internos do `feedback-receiver`
- **Dashboard** CloudWatch com invocações e erros
- Grupos de logs com retenção de 14 dias
- Logs de acesso do API Gateway
- Métricas personalizadas utilizando CloudWatch Embedded Metric Format (EMF)

**Documentação detalhada:** [Monitoramento e Segurança — Tarefa 7](docs/MONITORAMENTO_SEGURANCA.md)

## Execução local

### Pré-requisitos

- JDK 25+, Maven 3.9+
- Docker Desktop em execução
- AWS SAM CLI (`sam --version`)
- AWS CLI (necessário para deploy na nuvem)

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

## Instruções de deploy

O deploy é automatizado via **AWS SAM** (`infrastructure/template.yaml`), com parâmetros salvos em `infrastructure/samconfig.toml` (stack `fiap-tech-challenge-fase-4`, região `us-east-1`).

O template usa `SkipBuild: True` e aponta para os JARs gerados pelo Maven. Por isso o build Maven é obrigatório **antes** do `sam deploy`.

### Pré-requisitos (nuvem)

- AWS CLI configurado (`aws configure`)
- AWS SAM CLI instalado
- Identidades de e-mail verificadas no Amazon SES (sandbox): remetente (`SenderEmail`) e destino (`AdminEmail`)
- Confirmação da inscrição do e-mail no tópico SNS após o primeiro deploy

### Deploy (SAM)

```bash
# 1. Empacotar as Lambdas (JARs em */target)
mvn clean package

# 2. Implantar / atualizar o stack
cd infrastructure
sam deploy
```

Na primeira vez (ou para alterar parâmetros), use o modo guiado:

```bash
cd infrastructure
sam deploy --guided
```

Ou com parâmetros explícitos:

```bash
mvn clean package
cd infrastructure
sam deploy \
  --stack-name fiap-tech-challenge-fase-4 \
  --capabilities CAPABILITY_NAMED_IAM \
  --resolve-s3 \
  --parameter-overrides \
    EnvironmentName=dev \
    AdminEmail=seu-admin@email.com \
    SenderEmail=seu-remetente@email.com \
    DynamoDbEndpoint=
```

### Pós-deploy

1. Confirme a inscrição do e-mail no SNS (mensagem enviada pela AWS).
2. Obtenha o endpoint da API:

```bash
aws cloudformation describe-stacks \
  --stack-name fiap-tech-challenge-fase-4 \
  --region us-east-1 \
  --query "Stacks[0].Outputs[?OutputKey=='ApiEndpoint'].OutputValue" \
  --output text
```

3. Teste o recebimento de avaliação:

```powershell
$api = "https://<api-id>.execute-api.us-east-1.amazonaws.com/dev/avaliacao"
Invoke-RestMethod -Method Post -Uri $api `
  -ContentType "application/json" `
  -Body '{"descricao":"Curso com material desatualizado","nota":2}'
```

4. Valide no console AWS: tabela `dev-avaliacoes`, Lambdas ativas, dashboard `dev-feedback-dashboard`, e-mail SES/SNS de urgência ALTA/CRITICA.
5. Para o relatório semanal sem aguardar a segunda-feira 12:00 UTC, invoque `dev-weekly-reporter` manualmente no console ou via CLI.

## Artefatos de entrega

- Repositório com código-fonte e IaC
- Documentação de monitoramento e segurança da Tarefa 7
- Vídeo de demonstração: funções ativas, API, notificações, relatório e configurações de cloud/segurança

## Status atual

Os módulos `common`, `feedback-receiver`, `critical-notifier` e `weekly-reporter` possuem implementações e testes automatizados.

A suíte de testes foi executada com sucesso, totalizando **66 testes aprovados**, sem falhas ou erros.

A infraestrutura AWS SAM está implantada e validada na AWS (`CREATE_COMPLETE`):

| Item | Valor |
|------|-------|
| Stack | `fiap-tech-challenge-fase-4` |
| Região | `us-east-1` |
| Ambiente | `dev` |
| Tabela | `dev-avaliacoes` |
| Dashboard | `dev-feedback-dashboard` |

Verificações concluídas:

- Compilação Maven: **BUILD SUCCESS**
- Testes automatizados: **66 aprovados**
- Empacotamento / template SAM: **validado**
- Deploy na AWS: **concluído e em funcionamento**
- API (`POST /avaliacao`), notificações (SES + SNS) e relatório semanal: **validados no ambiente cloud**

O `critical-notifier` envia alerta por **SES e SNS** (composição dos dois canais).

A documentação técnica da Tarefa 7 está disponível em [Monitoramento e Segurança](docs/MONITORAMENTO_SEGURANCA.md).
