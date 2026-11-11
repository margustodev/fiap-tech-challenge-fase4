# Deploy na AWS

[Voltar ao README](../README.md)

## Stack da entrega

| Configuração | Valor |
|---|---|
| Stack CloudFormation | `fiap-tech-challenge-fase-4` |
| Região | `us-east-1` |
| Ambiente | `dev` |
| Tabela | `dev-avaliacoes` |
| Dashboard | `dev-feedback-dashboard` |

O [template SAM](../infrastructure/template.yaml) define a stack completa. O [samconfig.toml](../infrastructure/samconfig.toml) contém os parâmetros utilizados para a implantação em `dev`. O parâmetro `EnvironmentName` aceita `dev`, `hml` e `prod` e define o stage da API e o prefixo dos recursos.

## Implantar ou atualizar

Use JDK 25, Maven, AWS CLI e SAM CLI configurados para a conta de destino. O SES exige remetente verificado. Em sandbox, o destinatário também precisa ser verificado.

```bash
mvn clean package
cd infrastructure
sam validate --lint --template-file template.yaml
sam deploy
```

O template usa `SkipBuild: True` e os JARs gerados pelo Maven. Para configurar uma nova stack, execute `sam deploy --guided`, informe `EnvironmentName`, `AdminEmail` e `SenderEmail`, mantenha `DynamoDbEndpoint` vazio na AWS e autorize `CAPABILITY_NAMED_IAM`. Para receber alertas por SNS, confirme a inscrição enviada ao e-mail configurado.

## Obter o endereço da API

```bash
aws cloudformation describe-stacks \
  --stack-name fiap-tech-challenge-fase-4 \
  --region us-east-1 \
  --query "Stacks[0].Outputs[?OutputKey=='ApiEndpoint'].OutputValue" \
  --output text
```

Use o valor retornado diretamente: ele já inclui o stage e `/avaliacao`. Os demais outputs são `AvaliacoesTableName`, `CriticalAlertsTopicArn` e `FeedbackDashboardName`.

## Verificar uma atualização

1. Envie uma avaliação de nota 2 ao `ApiEndpoint`. Confira o `201`, a gravação no DynamoDB e o alerta por SES e SNS. Com nota 9, o resultado esperado é `201` sem alerta.
2. Confira logs e métricas no CloudWatch. Consulte o [guia de monitoramento](MONITORAMENTO_SEGURANCA.md) para os alarmes e indicadores.
3. Para testar o relatório fora do cron, invoque `dev-weekly-reporter` no console Lambda. O e-mail resume a última semana completa em UTC, inclusive quando o período está vazio.
