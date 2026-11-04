# Funções serverless

[Voltar ao README](../README.md)

Os handlers abaixo estão implementados no código-fonte. Os nomes físicos recebem prefixo de ambiente (`dev`, `hml` ou `prod`).

## Resumo operacional declarado no SAM

| ID lógico | Módulo/nome físico | Gatilho | Memória | Timeout |
|---|---|---|---|---|
| `FeedbackReceiverFunction` | `feedback-receiver` / `${EnvironmentName}-feedback-receiver` | API Gateway `POST /avaliacao` | 512 MB | 90 s |
| `CriticalNotifierFunction` | `critical-notifier` / `${EnvironmentName}-critical-notifier` | Stream, `INSERT`, lote de 1 | 512 MB | 90 s |
| `WeeklyReporterFunction` | `weekly-reporter` / `${EnvironmentName}-weekly-reporter` | EventBridge `cron(0 12 ? * MON *)` | 512 MB | 60 s |

Runtime declarado: `java25`, arquitetura `x86_64`. O timeout da função HTTP não garante igual tempo de espera do API Gateway; validar limites de integração na implantação.

## 1. Receber avaliação

**Responsabilidade:** receber uma avaliação HTTP, validar campos, calcular urgência e persistir. Não envia e-mail diretamente.

**Handler:** `br.com.fiap.techchallenge.fase4.feedback.handler.FeedbackReceiverHandler::handleRequest`.

**Entrada:** `APIGatewayProxyRequestEvent`; o JSON da avaliação está em `body`. **Saída:** `APIGatewayProxyResponseEvent`, com `Content-Type: application/json` e status `201`, `400` ou `500`.

Fluxo:

1. Desserializa `body` usando `JsonMapper`.
2. Obtém o caso de uso, com dependências DynamoDB inicializadas sob demanda.
3. `AvaliacaoRequestValidator` exige descrição não vazia e nota de 0 a 10.
4. Gera UUID e `Instant.now()`, calcula urgência e mapeia a entidade.
5. `DynamoDbAvaliacaoRepository.salvar` executa `PutItem`.
6. Registra contador em log e devolve a avaliação persistida.

**Configuração:** `AVALIACOES_TABLE_NAME`, `AWS_REGION`/`AWS_DEFAULT_REGION` e `DYNAMODB_ENDPOINT` local. **Permissões declaradas:** `PutItem`, `GetItem` e `DescribeTable` na tabela, mais logs pela role básica.

**Falhas:** JSON inválido e falhas de validação geram `400`; outras exceções capturadas geram `500`. O corpo do erro interno atualmente concatena a mensagem da exceção. A inicialização de dependências pode falhar antes de chegar à validação de campos. O retorno HTTP é descrito em [API](api.md).

**Reprocessamento:** sem idempotência; uma nova tentativa após timeout pode gerar uma segunda avaliação, inclusive se a primeira gravação já ocorreu.

**Testes existentes:** `FeedbackReceiverHandlerTest`, `ReceberAvaliacaoUseCaseTest`, `AvaliacaoRequestValidatorTest`, `CalcularUrgenciaServiceTest`, testes do mapper e repositório DynamoDB e integração `DynamoDbAvaliacaoRepositoryIT`.

## 2. Notificar urgência

**Responsabilidade:** selecionar novas avaliações com urgência `ALTA` ou `CRITICA` e solicitar envio de e-mail ao administrador.

**Handler:** `br.com.fiap.techchallenge.fase4.notifier.handler.CriticalNotificationHandler::handleRequest`.

**Entrada:** `DynamodbEvent`, contendo `Records` e `dynamodb.NewImage`. O mapper exige `id`, `descricao`, `nota`, `urgencia` e `dataEnvio`. **Saída:** `Void`/`null`; não é endpoint HTTP.

Fluxo:

1. Ignora evento nulo/sem registros e registros que não sejam `INSERT`.
2. `DynamoDbStreamMapper` converte `NewImage` em `Avaliacao`.
3. Ignora `MEDIA` e `BAIXA`.
4. Para `ALTA`/`CRITICA`, monta `NotificacaoUrgenciaDto` com descrição, urgência e data.
5. `SesNotificacaoService` formata o e-mail; `SesEmailService` chama o SES.

**Configuração:** `ADMIN_EMAIL`, `SENDER_EMAIL`, região e credenciais AWS pela cadeia padrão do SDK. O template também injeta `SNS_TOPIC_ARN`, mas o caminho atual de negócio não utiliza essa variável.

**Permissões declaradas:** leitura do stream, envio SES, publicação SNS e logs. O handler não consulta a tabela DynamoDB.

**Falhas:** destinatário/remetente ausente, imagem inválida ou erro SES propagam exceção. Não há captura por registro, checkpoint de deduplicação ou resposta parcial. O SAM declara `StartingPosition: LATEST`: o mapeamento não é uma estratégia de reprocessamento de todo o histórico.

**Limitação de aplicação:** `SnsNotificacaoService`, `CriticalNotifierConfig` e `ProcessarNotificacaoCriticaUseCase` estão em TODO. O envio via SES ocorre diretamente pelo handler e não depende desses esqueletos. Não apresentar SES e SNS como dois envios implementados.

**Testes existentes:** `CriticalNotificationHandlerTest` e `SesNotificacaoServiceTest`, com serviços simulados; não comprovam entrega real pelo SES.

## 3. Gerar relatório semanal

**Responsabilidade:** consultar a última semana completa, calcular estatísticas e enviar relatório por e-mail.

**Handler:** `br.com.fiap.techchallenge.fase4.report.handler.WeeklyReportHandler::handleRequest`.

**Entrada:** `ScheduledEvent`; o código não usa os campos do evento para escolher o período. **Saída:** `Void`/`null`; não expõe endpoint de consulta.

Fluxo:

1. `WeeklyReporterConfig` monta repositório e serviços SES.
2. `GerarRelatorioSemanalUseCase` usa `Clock.systemUTC()` para determinar a segunda-feira da semana anterior.
3. Consulta o GSI por período e filtra o intervalo `[segunda 00h UTC, segunda seguinte 00h UTC)`.
4. Calcula média, total, quantidades por dia e por urgência.
5. `RelatorioMapper` cria o DTO; `SesRelatorioService` formata texto e `SesEmailService` envia.

O e-mail contém descrição, urgência e data de cada avaliação. Sem avaliações, a média é `0.0` e o relatório informa ausência de registros. O cron declarado executa segunda às 12h UTC; o cálculo do período e as datas do relatório usam UTC.

**Configuração:** `AVALIACOES_TABLE_NAME`, região, `ADMIN_EMAIL`, `SENDER_EMAIL` e endpoint DynamoDB apenas para ambiente local. **Permissões declaradas:** `Query`, `GetItem`, `DescribeTable` na tabela/índices, envio SES e logs.

**Falhas:** exceções de consulta, configuração e SES propagam para a Lambda; não há marcação de relatório já enviado. Invocações repetidas podem enviar o mesmo relatório. Reexecução em semana diferente altera a janela consultada.

**Limitação de aplicação:** `RelatorioAggregator` e `DateTimeUtils` ainda possuem TODOs, mas o caminho atual calcula período e agregações no caso de uso e nos serviços de domínio implementados.

**Testes existentes:** `WeeklyReportHandlerTest`, `GerarRelatorioSemanalUseCaseTest`, `RelatorioMapperTest`, testes de média/agrupamentos e `SesRelatorioServiceTest`.
