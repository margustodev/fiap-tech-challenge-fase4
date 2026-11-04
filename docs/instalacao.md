# Instalação e execução local

[Voltar ao README](../README.md)

## Pré-requisitos

| Ferramenta | Uso |
|---|---|
| Git | Obter código e revisar alterações |
| JDK 25 | Release exato configurado no POM; selecionar também no Maven Runner |
| Maven 3.9+ | Build e execução de testes; não há Maven Wrapper versionado |
| Docker Desktop | DynamoDB Local e containers Lambda do SAM |
| AWS SAM CLI compatível com `java25` | API e invocações locais |
| PowerShell | Scripts `.ps1` disponíveis no repositório |
| AWS CLI | Apenas para operações reais de cloud descritas em deploy |

Verifique `java -version`, `mvn -version`, `docker info` e `sam --version`. O Maven deve indicar JDK 25. A compatibilidade da versão instalada do SAM e da imagem de runtime deve ser validada antes de executar.

O projeto é composto por handlers Lambda, executados pelo runtime AWS ou pelo SAM local.

## Build e testes sem cloud

Na raiz:

```bash
mvn clean package
```

Os módulos Lambda usam Maven Shade e produzem:

- `feedback-receiver/target/feedback-receiver-1.0.0-SNAPSHOT.jar`
- `critical-notifier/target/critical-notifier-1.0.0-SNAPSHOT.jar`
- `weekly-reporter/target/weekly-reporter-1.0.0-SNAPSHOT.jar`

O template referencia esses caminhos diretamente. Alterar a versão Maven exige ajustar `CodeUri` no SAM.

Para executar somente a fase de testes:

```bash
mvn test
```

Confira a quantidade de testes e os resultados em cada `target/surefire-reports/`; build com zero testes não comprova validação. `critical-notifier` possui testes JUnit 5, mas não fixa Surefire como os outros módulos; a descoberta depende da versão efetiva do plugin/Maven. Como diagnóstico explícito:

```bash
mvn -pl critical-notifier -am org.apache.maven.plugins:maven-surefire-plugin:3.2.5:test -DfailIfNoTests=true
```

Execute esse diagnóstico depois de compilar (`mvn test`), para que as classes de teste já existam. A padronização do Surefire é uma pendência de build, não uma dependência da AWS.

## Iniciar a API local (PowerShell)

Com Docker Desktop ativo, na raiz:

```powershell
.\scripts\start-api-local.ps1
```

O script inicia/reutiliza `fiap-dynamodb-local`, cria/valida a tabela pelo teste de integração, gera os JARs e chama o SAM na porta 3000. Ele usa `-sharedDb -inMemory`: os dados do DynamoDB Local são temporários e podem desaparecer quando o container é reiniciado.

O teste IT grava um registro de exemplo com data fixa; utilize uma tabela descartável local. Os scripts não conferem explicitamente `$LASTEXITCODE` após cada programa nativo: confirme que Maven encerrou com `BUILD SUCCESS` antes de confiar na etapa seguinte.

### Procedimento manual para diagnóstico

Terminal PowerShell A, na raiz, se o container ainda não existir:

```powershell
docker run -d --name fiap-dynamodb-local -p 8000:8000 `
  amazon/dynamodb-local -jar DynamoDBLocal.jar -sharedDb -inMemory
```

Se já existir, use `docker start fiap-dynamodb-local`. Depois, com o serviço pronto:

```powershell
$env:DYNAMODB_ENDPOINT = 'http://localhost:8000'
$env:AVALIACOES_TABLE_NAME = 'dev-avaliacoes'
$env:AWS_REGION = 'us-east-1'
$env:RUN_DYNAMODB_IT = 'true'
mvn -pl common -am test '-Dtest=DynamoDbAvaliacaoRepositoryIT'
```

Esse IT cria a tabela se necessário, persiste uma avaliação, busca por ID e consulta o índice. Ele não valida CRUD completo: não há atualização/remoção nesse teste. A seleção explícita é necessária: a classe termina em `IT`, não há Failsafe configurado e a variável deve estar definida como `true`.

Terminal PowerShell B, na raiz:

```powershell
mvn clean package
cd infrastructure
sam local start-api --template template.yaml --port 3000 --env-vars env-local.json `
  --parameter-overrides 'EnvironmentName=dev DynamoDbEndpoint=http://host.docker.internal:8000'
```

`env-local.json` contém apenas dados fictícios para desenvolvimento. O endpoint visto pela JVM no host é `localhost:8000`; dentro da Lambda Docker, é `host.docker.internal:8000`. Em Linux pode ser necessário configurar esse alias no Docker ou usar rede compartilhada; os scripts atuais foram preparados para Docker Desktop/PowerShell.

As variáveis `$env:...` do host não são automaticamente repassadas ao container Lambda. Preserve `--env-vars` e o parâmetro do endpoint para evitar chamada acidental ao DynamoDB real.

Terminal C: utilize os exemplos de [API](api.md).

## Invocar funções isoladamente

Com JARs gerados, na pasta `infrastructure`:

```powershell
sam local invoke FeedbackReceiverFunction --template template.yaml `
  --event ../events/post-avaliacao.json --env-vars env-local.json `
  --parameter-overrides 'EnvironmentName=dev DynamoDbEndpoint=http://host.docker.internal:8000'

sam local invoke FeedbackReceiverFunction --template template.yaml `
  --event ../events/post-avaliacao-positiva.json --env-vars env-local.json `
  --parameter-overrides 'EnvironmentName=dev DynamoDbEndpoint=http://host.docker.internal:8000'
```

Para alerta e relatório, prefira os testes com serviços simulados quando não houver acesso ao SES. Os comandos abaixo usam os adaptadores reais de e-mail: só execute com região, credenciais e remetente/destinatário válidos configurados para os containers. Os valores fictícios de `env-local.json` não habilitam envio de e-mail.

```powershell
sam local invoke CriticalNotifierFunction --template template.yaml `
  --event ../events/dynamodb-stream.json --env-vars env-local.json

sam local invoke WeeklyReporterFunction --template template.yaml `
  --event ../events/schedule.json --env-vars env-local.json `
  --parameter-overrides 'EnvironmentName=dev DynamoDbEndpoint=http://host.docker.internal:8000'
```

O arquivo local define credenciais fictícias no reporter para DynamoDB Local; isso não autentica SES. Para teste real, use uma configuração própria não versionada e perfil/credenciais autorizadas. Não existe emulador SES configurado neste projeto.

O SAM local não conecta automaticamente Streams/EventBridge às funções. `null` é a saída normal dos handlers `Void` de alerta e relatório; não significa que o código esteja vazio. Confirme também logs e efeitos esperados.

## Diagnóstico rápido

| Sintoma | Conferir |
|---|---|
| `release version 25 not supported` | JDK do terminal e do Maven |
| JAR ausente no SAM | Rodar `mvn clean package` na raiz e conferir os três artefatos |
| Timeout, `500` ou `502` local | Endpoint no container, parâmetros SAM, Docker e tabela |
| Tabela não encontrada | `AVALIACOES_TABLE_NAME`, teste IT e `-sharedDb` |
| SES rejeita envio | Configuração/credenciais reais, identidades verificadas na mesma região |
| Nenhum alerta após POST local | Integração automática do stream não existe no ambiente local |
| Testes IT não executados | Variável `RUN_DYNAMODB_IT=true` e seleção explícita por `-Dtest` |
