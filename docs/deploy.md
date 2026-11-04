# Deploy e configurações

[Voltar ao README](../README.md)

## O que já está versionado

| Arquivo | Finalidade |
|---|---|
| `infrastructure/template.yaml` | API, três Lambdas, roles IAM, tabela/GSI/stream, SNS, regra semanal, alarmes e dashboard |
| `infrastructure/dynamodb-only.yaml` | Alternativa para criar apenas a tabela |
| `infrastructure/samconfig.toml` | Defaults de SAM para dev e e-mails fictícios |
| `infrastructure/env-local.json` | Valores de desenvolvimento para SAM local |

## Pré-requisitos de conta e decisões

Antes do primeiro deploy, definir conta, região, ambiente, permissões do executor e e-mails de remetente/administrador. Verificar as identidades SES na região escolhida e as restrições da conta (incluindo destinatário no sandbox). A assinatura de e-mail do SNS exige confirmação pelo destinatário.

O template declara recursos com nomes fixos por ambiente (roles, funções, tabela e tópico). Use stacks/ambientes sem colisão. Não implante `dynamodb-only.yaml` e a stack completa com o mesmo `EnvironmentName` esperando que compartilhem a tabela automaticamente: ambas tentam gerenciá-la. Planeje propriedade/importação antes de reutilizar uma tabela existente.

## Configuração por ambiente

| Parâmetro/variável | Origem | Uso |
|---|---|---|
| `EnvironmentName` | Parameter SAM, default `dev` | Prefixos e stage; aceita `dev`, `hml`, `prod` |
| `AdminEmail` → `ADMIN_EMAIL` | Parameter SAM | Destinatário SES e assinatura SNS |
| `SenderEmail` → `SENDER_EMAIL` | Parameter SAM | Remetente SES verificado |
| `DynamoDbEndpoint` → `DYNAMODB_ENDPOINT` | Parameter SAM | Vazio na AWS; URL local apenas para desenvolvimento |
| `AVALIACOES_TABLE_NAME` | SAM, `${EnvironmentName}-avaliacoes` | Tabela de avaliações |
| `AWS_REGION` | Ambiente Lambda / configuração SDK local | Região dos clientes AWS |
| `SNS_TOPIC_ARN` | SAM no notifier | Injetado; adaptador SNS de negócio ainda em TODO |
| `ENVIRONMENT`, `POWERTOOLS_SERVICE_NAME` | Globals SAM | Valores declarados; não comprovam instrumentação adicional |

`env-local.json` só é usado com `sam local --env-vars`; não configura ambientes AWS. E-mails `example.com` não são configurações válidas de entrega. Credenciais não devem entrar no repositório. O acesso a Secrets Manager/SSM ainda não está implementado em `SecurityConfig`.

## Deploy manual da stack completa

Na raiz, usando JDK 25 e Maven:

```bash
mvn clean package
cd infrastructure
sam validate --lint --template-file template.yaml
sam deploy --guided --template-file template.yaml
```

Interrompa se o build, testes ou validação falharem. O template utiliza `SkipBuild: True` e `CodeUri` de JARs gerados: o Maven deve executar antes do SAM. Informe stack e região pretendidas, parâmetros reais, `DynamoDbEndpoint` vazio e permissão para criação de IAM nomeado (`CAPABILITY_NAMED_IAM`). Revise o changeset apresentado.

Alternativa explícita em Bash, substituindo e-mails ilustrativos:

```bash
sam deploy --template-file template.yaml \
  --stack-name fiap-fase4-dev \
  --region us-east-1 \
  --capabilities CAPABILITY_NAMED_IAM \
  --resolve-s3 \
  --confirm-changeset \
  --parameter-overrides \
    EnvironmentName=dev \
    AdminEmail=admin@seu-dominio.com \
    SenderEmail=remetente@seu-dominio.com \
    DynamoDbEndpoint=
```

`us-east-1` é o exemplo alinhado aos defaults locais; use a região efetivamente definida para a entrega. Não copie credenciais fictícias locais para o deploy. Antes de reutilizar `samconfig.toml`, revise os defaults e possíveis valores salvos de execuções anteriores.

A alternativa de tabela isolada é descrita pelo próprio arquivo `dynamodb-only.yaml`; ela não implanta aplicação, funções ou endpoint.

## Verificação após implantação

1. Confirmar stack concluída e registrar commit, stack, região e ambiente implantados.
2. Ler outputs:

```bash
aws cloudformation describe-stacks \
  --stack-name fiap-fase4-dev \
  --region us-east-1 \
  --query 'Stacks[0].Outputs'
```

3. Usar `ApiEndpoint` exatamente como retornado: o output já inclui stage e `/avaliacao`.
4. Enviar uma nota 2 e uma nota 9, conferir `201` e persistência no DynamoDB.
5. Verificar mapeamento do stream e recebimento do alerta SES somente para `ALTA`/`CRITICA`.
6. Confirmar assinatura SNS e alarmes/dashboard. O tópico de alarmes não comprova uso do adaptador SNS de negócio.
7. Conferir regra semanal habilitada e executar teste controlado do reporter. O relatório seleciona a semana anterior; dados enviados no mesmo dia podem não entrar nele.
8. Registrar logs e evidências de relatório, alerta e respostas inválidas, sem publicar credenciais ou dados pessoais.
