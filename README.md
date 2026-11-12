# FIAP Tech Challenge — Fase 4

Sistema serverless para coletar avaliações de cursos on-line, alertar administradores sobre avaliações urgentes e enviar um relatório semanal com média das notas e contagens por dia e por urgência.

O API Gateway recebe `POST /avaliacao`. A Lambda `feedback-receiver` valida e grava a avaliação no DynamoDB. O DynamoDB Streams aciona a `critical-notifier`, que envia alertas por SES e SNS para urgências `ALTA` e `CRITICA`. Toda segunda-feira às 12h UTC, o EventBridge aciona a `weekly-reporter` para consolidar a semana anterior e enviar o relatório por SES.

## Instalação e execução local

Requisitos: JDK 25, Maven 3.9+, Docker e AWS SAM CLI com suporte ao runtime Java 25. O script de execução local usa PowerShell.

```bash
git clone https://github.com/margustodev/fiap-tech-challenge-fase4.git
cd fiap-tech-challenge-fase4
mvn clean package
```

Na raiz do repositório, com o Docker em execução:

```powershell
.\scripts\start-api-local.ps1
```

A API fica disponível em `http://127.0.0.1:3000/avaliacao`. Consulte o [guia de instalação](docs/instalacao.md) para os pré-requisitos, testes e configuração do DynamoDB Local.

## API

| Método | Rota | Sucesso |
|---|---|---|
| POST | `/avaliacao` | `201 Created` com a avaliação persistida |

```bash
curl -i http://127.0.0.1:3000/avaliacao \
  -H 'Content-Type: application/json' \
  -d '{"descricao":"Curso com material desatualizado","nota":2}'
```

A nota vai de 0 a 10. O [contrato da API](docs/api.md) descreve as regras de urgência, os campos da resposta e os erros. O [OpenAPI](docs/openapi.yaml) reúne os schemas e exemplos.

## Deploy na AWS

O Maven gera os JARs referenciados pelo AWS SAM. Com AWS CLI e SAM CLI configurados para a conta de destino, execute:

```bash
mvn clean package
cd infrastructure
sam deploy
```

O [guia de deploy](docs/deploy.md) descreve os parâmetros, os outputs do CloudFormation e os testes após uma atualização da stack.

A entrega registra a stack `fiap-tech-challenge-fase-4` implantada em `dev`, na região `us-east-1`, com a API, notificações SES/SNS e relatório semanal validados nesse ambiente.

## Estrutura do repositório

| Caminho | Conteúdo |
|---|---|
| `pom.xml` | Build Maven multimódulo |
| `common/` | Domínio, casos de uso, DTOs, portas e persistência DynamoDB |
| `feedback-receiver/` | Lambda de recebimento de avaliações |
| `critical-notifier/` | Lambda de alertas por SES e SNS |
| `weekly-reporter/` | Lambda de relatório por SES |
| `infrastructure/` | Templates AWS SAM, parâmetros de deploy e configuração local |
| `events/` e `scripts/` | Eventos de exemplo e scripts para execução local |
| `docs/` | Documentação técnica |

## Documentação técnica

- [Arquitetura e modelo de dados](docs/arquitetura.md)
- [Funções serverless](docs/funcoes-serverless.md)
- [Monitoramento e segurança](docs/MONITORAMENTO_SEGURANCA.md)
