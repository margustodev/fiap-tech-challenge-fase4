# Instalação e execução local

[Voltar ao README](../README.md)

## Pré-requisitos e build

Instale Git, JDK 25, Maven 3.9+, Docker e AWS SAM CLI com suporte a Java 25. Use PowerShell para executar os scripts do repositório. Confira o JDK ativo com `java -version` e `mvn -version`.

Na raiz do projeto:

```bash
mvn clean package
```

O comando executa os testes e gera os JARs das três Lambdas em `target/` de cada módulo. Para executar só os testes unitários, use `mvn test`.

## Subir a API local

Com o Docker em execução, rode na raiz:

```powershell
.\scripts\start-api-local.ps1
```

O script inicia o DynamoDB Local na porta 8000, cria e valida a tabela `dev-avaliacoes`, empacota as Lambdas e inicia o SAM na porta 3000. Use os [exemplos da API](api.md) para enviar uma avaliação.

O DynamoDB Local é iniciado em memória. A JVM do host usa `http://localhost:8000`; a função dentro do contêiner usa `http://host.docker.internal:8000`, definido pelo parâmetro SAM `DynamoDbEndpoint`.

## Testar as funções

Os arquivos em [events/](../events) são exemplos de eventos para invocações. Os testes unitários exercitam os handlers de alerta e relatório com dependências simuladas. O fluxo HTTP local cobre validação e persistência. O teste integrado de Streams, EventBridge, SES e SNS é descrito no [guia de deploy](deploy.md).
