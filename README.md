# FIAP Tech Challenge — Fase 4

Sistema serverless de avaliações de cursos on-line desenvolvido para a pós-graduação em Arquitetura e Desenvolvimento em Java da FIAP. Recebe avaliações, calcula urgência, persiste no DynamoDB, envia alertas por e-mail e gera relatório semanal.

> **Estado da entrega:** os três handlers possuem implementação. A infraestrutura SAM está versionada, mas o provisionamento e a operação na AWS não foram validados nesta revisão. CI/CD e deploy automatizado continuam pendentes. Consulte o [checklist de entrega e pendências](docs/entrega.md).

## Documentação

| Documento | Conteúdo |
|---|---|
| [Arquitetura](docs/arquitetura.md) | Componentes, fluxos, camadas, dados e decisões |
| [Funções serverless](docs/funcoes-serverless.md) | Responsabilidade, entrada, saída, dependências e falhas de cada Lambda |
| [API](docs/api.md) | Endpoint, exemplos, respostas e limitações atuais |
| [OpenAPI 3.0](docs/openapi.yaml) | Contrato de referência para Swagger Editor e clientes HTTP |
| [Instalação e execução local](docs/instalacao.md) | IntelliJ, Maven, Docker, SAM e testes |
| [Deploy e configuração](docs/deploy.md) | Procedimento manual proposto e dependências de DevOps |
| [Checklist de entrega](docs/entrega.md) | Evidências disponíveis, bloqueios de cloud e pendências de código |

## Tecnologias e organização

Java **25** (release definido no POM), Maven multimódulo, AWS SDK for Java v2, Jackson, JUnit 5 e Mockito. A arquitetura declarada no SAM utiliza Lambda, API Gateway REST, DynamoDB, SES, SNS, EventBridge e CloudWatch.

| Caminho | Responsabilidade |
|---|---|
| `common/` | Domínio, casos de uso, DTOs, portas e persistência compartilhada |
| `feedback-receiver/` | Lambda HTTP de recebimento de avaliações |
| `critical-notifier/` | Lambda consumidora do DynamoDB Streams, com alertas via SES |
| `weekly-reporter/` | Lambda de relatório semanal enviado via SES |
| `infrastructure/` | Templates SAM/CloudFormation, configuração SAM e variáveis locais |
| `events/` | Eventos de exemplo para invocação local |
| `scripts/` | Inicialização e validação local em PowerShell |
| `docs/` | Documentação técnica e checklist |

O SNS recebe alarmes na infraestrutura declarada. O envio de alertas de negócio pelo adaptador SNS ainda está em TODO; o handler atual usa SES.

## Início rápido

Pré-requisitos: Git, JDK 25, Maven 3.9+, Docker Desktop e AWS SAM CLI compatível com `java25`. Para os scripts, PowerShell. AWS CLI e credenciais são necessários apenas para operações na AWS.

```bash
git clone https://github.com/margustodev/fiap-tech-challenge-fase4.git
cd fiap-tech-challenge-fase4
mvn clean package
```

No IntelliJ: abra o `pom.xml` da raiz como projeto Maven, selecione **JDK 25** para Project SDK e Maven Runner e recarregue os módulos. O projeto é composto por handlers Lambda; a API local é iniciada pelo SAM, sem classe `main` de servidor web.

No PowerShell, com Docker ativo:

```powershell
.\scripts\start-api-local.ps1
```

Em outro terminal:

```powershell
Invoke-RestMethod -Method Post -Uri http://127.0.0.1:3000/avaliacao `
  -ContentType 'application/json' `
  -Body '{"descricao":"Curso com material desatualizado","nota":2}'
```

A resposta esperada é HTTP `201`, com `id`, `descricao`, `nota`, `urgencia` e `dataEnvio`. O `201` confirma a persistência; o alerta é assíncrono na AWS e seu envio não é confirmado pela resposta HTTP. O ambiente local não conecta automaticamente DynamoDB Streams ao SAM.

## API e regras

O único endpoint HTTP declarado é **`POST /avaliacao`**, com corpo JSON contendo `descricao` não vazia e `nota` de 0 a 10. Respostas do handler: `201`, `400` e `500`.

| Nota | Urgência | Alerta de negócio |
|---|---|---|
| 0–2 | `CRITICA` | Sim, via SES |
| 3–4 | `ALTA` | Sim, via SES |
| 5–7 | `MEDIA` | Não |
| 8–10 | `BAIXA` | Não |

O relatório consulta a semana anterior completa, de segunda a domingo em UTC, e inclui média, total, quantidades por dia e urgência e detalhamento das avaliações. O template agenda a execução para segunda-feira às 12h UTC.

## Testes e validação

```bash
mvn test
```

Para gerar os três JARs com suas dependências, execute `mvn clean package` na raiz. Para validar persistência com DynamoDB Local:

```powershell
.\scripts\validate-dynamodb-local.ps1
```

Veja [execução local e testes](docs/instalacao.md) para a seleção explícita do teste de integração e a conferência dos relatórios Surefire. **Nesta revisão documental, os testes Java não foram executados:** o ambiente disponível possui JDK 17 e não possui Maven; o projeto requer JDK 25.

## Deploy

Existe infraestrutura como código em [infrastructure/template.yaml](infrastructure/template.yaml) e uma alternativa de tabela isolada em [infrastructure/dynamodb-only.yaml](infrastructure/dynamodb-only.yaml). Isso comprova a existência dos templates, sem comprovar a implantação.

Após preparar a conta AWS e revisar o template:

```bash
mvn clean package
cd infrastructure
sam validate --lint --template-file template.yaml
sam deploy --guided --template-file template.yaml
```

O SAM aponta para JARs em `target/` e declara `SkipBuild: True`; compile com Maven antes do deploy. Informe e-mails reais verificados no SES e mantenha `DynamoDbEndpoint` vazio na cloud. O procedimento completo está em [deploy](docs/deploy.md).

**Ainda pendente de DevOps:** revisar/provisionar a stack, configurar CI/CD, automatizar deploy, configurar ambientes e coletar evidências de execução. Não há URL de cloud validada nem workflow versionado nesta base. Há também [pendências de aplicação](docs/entrega.md) que não serão resolvidas apenas com deploy.

## Repositório e revisão no IntelliJ

O repositório foi identificado como público pela integração GitHub nesta revisão. Antes da entrega, confirme acesso ao link em janela anônima. A documentação fica em Markdown e pode ser revisada no IntelliJ pela visualização e pelo diff de Git. Não versione credenciais AWS, arquivos pessoais ou resultados de build.
