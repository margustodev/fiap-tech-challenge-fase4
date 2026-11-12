# Funções serverless

[Voltar ao README](../README.md)

O [template SAM](../infrastructure/template.yaml) usa Java 25, arquitetura `x86_64` e 512 MB por função. Os nomes físicos recebem o prefixo do ambiente.

| ID lógico | Módulo | Gatilho | Timeout Lambda |
|---|---|---|---|
| `FeedbackReceiverFunction` | `feedback-receiver` | API Gateway, `POST /avaliacao` | 90 s |
| `CriticalNotifierFunction` | `critical-notifier` | DynamoDB Streams, evento `INSERT`, lote de um registro | 90 s |
| `WeeklyReporterFunction` | `weekly-reporter` | EventBridge, `cron(0 12 ? * MON *)` | 60 s |

## Receber avaliação

**Handler** `br.com.fiap.techchallenge.fase4.feedback.handler.FeedbackReceiverHandler::handleRequest`

- Recebe o JSON no corpo de um `APIGatewayProxyRequestEvent`, valida descrição e nota, gera UUID e data de envio e calcula a urgência.
- Persiste a avaliação com `dynamodb:PutItem` na tabela definida por `AVALIACOES_TABLE_NAME`.
- Emite métricas CloudWatch no formato EMF e retorna JSON com HTTP `201`, `400` ou `500`. Consulte o [contrato da API](api.md) para os exemplos.

## Notificar urgência

**Handler** `br.com.fiap.techchallenge.fase4.notifier.handler.CriticalNotificationHandler::handleRequest`

- Recebe um `DynamodbEvent` e converte a imagem `NewImage` de cada inserção em avaliação. O mapeamento do stream começa em `LATEST`.
- Encaminha somente avaliações `ALTA` e `CRITICA` ao serviço de notificação. A mensagem inclui descrição, urgência e data de envio.
- Envia e-mail pelo SES e, em seguida, publica no tópico indicado por `SNS_TOPIC_ARN`. O e-mail usa `ADMIN_EMAIL` e `SENDER_EMAIL`.
- Retorna `Void`. Falhas de processamento propagam para a Lambda. Se o SES tiver sucesso e o SNS falhar, uma nova tentativa pode reenviar o e-mail.

## Gerar relatório semanal

**Handler** `br.com.fiap.techchallenge.fase4.report.handler.WeeklyReportHandler::handleRequest`

- Recebe um `ScheduledEvent` às segundas-feiras às 12h UTC. Calcula a última semana completa em UTC, da segunda-feira anterior às 00h até a segunda-feira seguinte às 00h, com fim exclusivo.
- Consulta o GSI `periodo-index` e calcula média das notas, total, quantidade por dia e por urgência. O relatório inclui a descrição, a urgência e a data de cada avaliação.
- Envia o relatório pelo SES usando `ADMIN_EMAIL` e `SENDER_EMAIL` e retorna `Void`. Uma invocação manual usa a mesma regra de seleção do período.
