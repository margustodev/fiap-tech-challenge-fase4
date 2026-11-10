# Monitoramento e Segurança — Tarefa 7

## 1. Objetivo

Implementar mecanismos de monitoramento, geração de alertas e segurança na arquitetura serverless do projeto FIAP Tech Challenge — Fase 4.

A solução utiliza AWS Lambda, Amazon API Gateway, Amazon DynamoDB, Amazon CloudWatch, Amazon SNS e Amazon SES, com infraestrutura definida por meio do AWS SAM.

O objetivo é facilitar a identificação de falhas, melhorar a rastreabilidade das requisições e aplicar o princípio do menor privilégio aos serviços da aplicação.

## 2. Monitoramento com Amazon CloudWatch

### 2.1. Logs das funções Lambda

Foram definidos grupos de logs no arquivo `infrastructure/template.yaml` para as três funções:

- `FeedbackReceiverFunction`
- `CriticalNotifierFunction`
- `WeeklyReporterFunction`

Os grupos de logs possuem retenção configurada para **14 dias**.

As funções utilizam a política gerenciada `AWSLambdaBasicExecutionRole` para permitir o registro de logs no CloudWatch.

### 2.2. Logs do API Gateway

Foi configurado um grupo de logs de acesso para o API Gateway, também com retenção de 14 dias.

O formato dos registros contempla:

- Identificador da requisição (`requestId`);
- Identificador estendido (`extendedRequestId`);
- Data e horário da requisição;
- Método HTTP;
- Caminho do recurso;
- Status HTTP;
- Latência da resposta;
- Tamanho da resposta.

O registro detalhado dos dados das requisições permanece desabilitado por meio de `DataTraceEnabled: false`, reduzindo o risco de exposição de informações sensíveis.

### 2.3. Métricas personalizadas

O módulo `feedback-receiver` utiliza a classe `CloudWatchMetricsService` para produzir métricas no formato **CloudWatch Embedded Metric Format (EMF)**.

As métricas são emitidas como eventos JSON nos logs da função Lambda.

**Configuração:**

- Namespace: `FIAP/FeedbackCursos`
- Dimensão: `Service`
- Valor da dimensão: `feedback-receiver`

| Métrica | Finalidade |
|---|---|
| `avaliacao.recebida.sucesso` | Contabilizar avaliações recebidas com sucesso |
| `avaliacao.recebida.erro_validacao` | Contabilizar erros de validação |
| `avaliacao.recebida.erro_interno` | Contabilizar erros internos |

Foram criados testes automatizados para verificar o formato EMF e a rejeição de nomes e valores inválidos.

A disponibilização efetiva das métricas no CloudWatch precisa ser verificada após o deploy.

## 3. Alarmes e notificações

Foram configurados quatro alarmes no arquivo `infrastructure/template.yaml`.

| Alarme | Monitoramento |
|---|---|
| `FeedbackReceiverErrorAlarm` | Erros da Lambda de recebimento |
| `CriticalNotifierErrorAlarm` | Erros da Lambda de notificações |
| `WeeklyReporterErrorAlarm` | Erros da Lambda de relatório semanal |
| `FeedbackReceiverInternalErrorAlarm` | Métrica personalizada de erros internos |

Os três primeiros alarmes utilizam a métrica `Errors` do namespace `AWS/Lambda`.

O alarme personalizado utiliza a métrica `avaliacao.recebida.erro_interno` do namespace `FIAP/FeedbackCursos`.

Esse alarme adicional é importante porque permite monitorar erros tratados pela aplicação que resultam em HTTP 500, mas não são necessariamente contabilizados pela métrica padrão `AWS/Lambda Errors`.

Os alarmes estão associados ao tópico SNS `CriticalAlertsTopic`.

O destinatário das notificações é configurado pelo parâmetro `AdminEmail`.

**Observação:** após o deploy, será necessário confirmar a inscrição de e-mail no SNS e testar a entrega das notificações.

## 4. Dashboard de monitoramento

Foi definido o recurso `FeedbackDashboard` no Amazon CloudWatch.

O dashboard apresenta indicadores relacionados a:

- Quantidade de invocações das funções Lambda;
- Quantidade de erros das funções Lambda.

O painel permite acompanhar o comportamento geral da aplicação e auxiliar na identificação de falhas.

A apresentação de métricas reais dependerá da implantação e execução das funções na AWS.

## 5. Segurança e controle de acesso

### 5.1. Princípio do menor privilégio

Cada função Lambda possui uma IAM Role dedicada.

As permissões foram revisadas para manter apenas as operações necessárias aos fluxos analisados.

| Função | Permissões principais |
|---|---|
| Feedback Receiver | `dynamodb:PutItem` |
| Critical Notifier | Leitura do DynamoDB Streams, `ses:SendEmail` e `sns:Publish` |
| Weekly Reporter | `dynamodb:Query` e `ses:SendEmail` |

As permissões do DynamoDB foram limitadas aos recursos correspondentes.

No envio de e-mails pelo Amazon SES, foi configurada uma condição para restringir o endereço remetente ao valor informado em `SenderEmail`.

Essas configurações foram validadas estaticamente no template SAM, mas ainda precisam ser verificadas durante a execução na AWS.

### 5.2. Proteção dos dados no DynamoDB

A tabela de avaliações possui:

- Criptografia em repouso habilitada (SSE);
- Point-in-Time Recovery (PITR) habilitado.

As configurações estão declaradas no template de infraestrutura.

### 5.3. Tratamento seguro de erros HTTP

O arquivo `FeedbackReceiverHandler.java` foi atualizado para evitar a exposição de detalhes técnicos nas respostas HTTP 500.

Quando ocorre uma exceção inesperada:

1. A métrica `avaliacao.recebida.erro_interno` é registrada;
2. O erro é registrado nos logs técnicos com o identificador da requisição;
3. O usuário recebe HTTP 500 com uma mensagem genérica.

**Mensagem retornada ao usuário:**

`Erro interno ao processar a avaliação.`

A mensagem original da exceção não é incluída na resposta HTTP.

A chamada redundante a `printStackTrace()` também foi removida, mantendo o registro da exceção pelo SLF4J.

Foi atualizado um teste automatizado que simula a exceção `falha dynamodb` e verifica que essa informação não aparece na resposta da API.

### 5.4. Autenticação do endpoint

Durante a revisão do `template.yaml`, não foi identificada configuração explícita de JWT, Cognito, API Key ou outro mecanismo de autorização para o endpoint `POST /avaliacao`.

O README também não estabelece uma exigência específica de autenticação para esse endpoint.

Por esse motivo, a Tarefa 7 não acrescentou autenticação.

Antes de disponibilizar a API publicamente em produção, recomenda-se confirmar os requisitos de acesso e avaliar medidas de proteção contra uso abusivo, como limitação de requisições.

### 5.5. Parâmetros e informações sensíveis

O template utiliza parâmetros para configurações como:

- `EnvironmentName`;
- `AdminEmail`;
- `SenderEmail`;
- `DynamoDbEndpoint`.

Credenciais e segredos não devem ser inseridos diretamente no código-fonte nem versionados no repositório Git.

Caso a aplicação passe a exigir armazenamento de credenciais, recomenda-se utilizar um serviço específico de gerenciamento de segredos.

## 6. Testes automatizados

A suíte de testes do projeto foi executada após as alterações.

**Comando utilizado:**

`mvn test`

**Resultados:**

| Módulo | Testes aprovados |
|---|---:|
| common | 37 |
| feedback-receiver | 8 |
| critical-notifier | 15 |
| weekly-reporter | 6 |
| **Total** | **66** |

**Resultado geral:** 66 testes aprovados, sem falhas ou erros.

Entre os testes relacionados à Tarefa 7 estão:

- Validação da estrutura JSON das métricas EMF;
- Rejeição de métricas com nomes inválidos;
- Rejeição de valores numéricos inválidos;
- Resposta HTTP 201 para avaliações válidas;
- Resposta HTTP 400 para avaliações inválidas;
- Resposta HTTP 500 para erros internos;
- Registro da métrica de erro interno;
- Verificação de que detalhes técnicos de exceções não aparecem nas respostas HTTP.

## 7. Compilação e validação da infraestrutura

### 7.1. Compilação Maven

Comando executado:

`mvn clean package`

**Resultado:** `BUILD SUCCESS`.

Os arquivos JAR das três funções foram gerados com sucesso.

### 7.2. Empacotamento AWS SAM

Comando executado:

`sam build -t infrastructure/template.yaml --region us-east-1`

**Resultado:** `Build Succeeded`.

### 7.3. Validação do template SAM

Comando executado:

`sam validate --lint -t infrastructure/template.yaml --region us-east-1`

**Resultado:** template SAM válido.

Essas verificações confirmam a compilação e a validade estática da infraestrutura, mas não substituem os testes em ambiente AWS.

## 8. Validações pendentes na AWS

Até esta etapa, os testes foram realizados localmente.

Ainda será necessário verificar:

- Criação dos grupos de logs e retenção configurada;
- Recebimento dos logs de acesso do API Gateway;
- Publicação e visualização das métricas EMF;
- Funcionamento dos quatro alarmes;
- Entrega das notificações pelo SNS;
- Visualização de dados reais no dashboard;
- Funcionamento das três Lambdas com as IAM Roles restritas;
- Ausência de conflitos com grupos de logs preexistentes;
- Configuração de permissões necessárias para os logs do API Gateway.

Essas atividades deverão ser consideradas concluídas somente após a validação efetiva na AWS.

## 9. Arquivos principais

| Arquivo | Responsabilidade |
|---|---|
| `infrastructure/template.yaml` | Configuração de infraestrutura, logs, alarmes, IAM e dashboard |
| `CloudWatchMetricsService.java` | Emissão de métricas no formato EMF |
| `CloudWatchMetricsServiceTest.java` | Testes das métricas personalizadas |
| `FeedbackReceiverHandler.java` | Processamento HTTP e tratamento seguro de erros |
| `FeedbackReceiverHandlerTest.java` | Testes das respostas HTTP e métricas |

Os arquivos Java estão localizados no módulo `feedback-receiver`.

## 10. Conclusão

A Tarefa 7 acrescentou recursos de observabilidade e reforçou as configurações de segurança da aplicação serverless do FIAP Tech Challenge — Fase 4.

Foram implementadas ou configuradas as seguintes melhorias:

- **Monitoramento:** logs no Amazon CloudWatch, com retenção de 14 dias e métricas personalizadas em EMF;
- **Alertas:** alarmes para erros das três funções Lambda e para erros internos tratados pela aplicação;
- **Notificações:** alertas de urgência crítica via Amazon SES e Amazon SNS; alarmes CloudWatch também notificados pelo SNS;
- **Observabilidade:** dashboard com indicadores de invocações e erros;
- **Segurança IAM:** revisão das permissões aplicando o princípio do menor privilégio;
- **Proteção de dados:** criptografia em repouso e recuperação para um ponto no tempo no DynamoDB;
- **Tratamento seguro de erros:** proteção das respostas HTTP 500 contra exposição de informações internas;
- **Testes:** validação automatizada da geração de métricas e do tratamento de respostas HTTP.

**Resultados obtidos:**

| Verificação | Resultado |
|---|---|
| Testes automatizados | 66 aprovados |
| Falhas nos testes | 0 |
| Erros nos testes | 0 |
| Compilação Maven | BUILD SUCCESS |
| Empacotamento AWS SAM | Build Succeeded |
| Validação do template SAM | Aprovada |

A Tarefa 7 está implementada e validada localmente. A confirmação do funcionamento dos recursos provisionados permanece pendente de testes no ambiente AWS.

Esta documentação registra as funcionalidades desenvolvidas, os mecanismos de segurança adotados e as verificações necessárias para apoiar a manutenção e evolução da solução.