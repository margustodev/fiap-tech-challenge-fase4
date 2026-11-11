# Endpoints HTTP

[Voltar ao README](../README.md) · [Contrato OpenAPI](openapi.yaml)

## Endereços

| Ambiente | Endereço |
|---|---|
| Local com SAM | `http://127.0.0.1:3000/avaliacao` |
| AWS | [Consultar o output `ApiEndpoint` da stack](deploy.md#obter-o-endereço-da-api) |

Somente `POST /avaliacao` está declarado no template. As funções de alerta e relatório são acionadas por eventos, sem endpoints REST. Não há GET de avaliações, health check ou frontend neste código.

## POST /avaliacao

Recebe uma avaliação e retorna o objeto persistido. Envie `Content-Type: application/json`. O template atual não exige autenticação ou API key.

### Requisição

```json
{
  "descricao": "Curso com material desatualizado",
  "nota": 2
}
```

| Campo | Tipo do contrato | Obrigatório | Regra |
|---|---|---|---|
| `descricao` | string | Sim | Não pode ser nula, vazia ou conter apenas espaços |
| `nota` | integer | Sim | De 0 a 10, inclusive |

`id`, `urgencia` e `dataEnvio` são gerados pelo servidor. Campos JSON desconhecidos são ignorados pelo `JsonMapper`. O contrato define nota inteira, mas o ObjectMapper atual não desabilita coerções padrão para `Integer`; envie a nota como um número inteiro no JSON. A descrição não tem tamanho máximo validado no código.

### Resposta 201

Exemplo ilustrativo; UUID e data variam a cada envio:

```json
{
  "id": "a3e2d628-471d-4f59-8cf9-fd7b42a01d25",
  "descricao": "Curso com material desatualizado",
  "nota": 2,
  "urgencia": "CRITICA",
  "dataEnvio": "2026-11-03T15:00:00Z"
}
```

A confirmação da persistência ocorre antes do alerta assíncrono por SES e SNS. Alertas ocorrem para notas 0–4; notas 5–7 são `MEDIA` e 8–10 são `BAIXA`.

### Respostas de erro

Todas as respostas produzidas pela fábrica do handler usam `Content-Type: application/json`.

| Status | Situação | Exemplo de `mensagem` |
|---|---|---|
| `400` | Corpo nulo/vazio | `Corpo da requisição é obrigatório.` |
| `400` | Descrição ausente ou em branco | `O campo 'descricao' é obrigatório.` |
| `400` | Nota ausente | `O campo 'nota' é obrigatório.` |
| `400` | Nota fora da faixa | `O campo 'nota' deve estar entre 0 e 10.` |
| `400` | Erro de desserialização | Prefixo `JSON inválido:` seguido do detalhe Jackson |
| `500` | Falha não tratada como validação | `Erro interno ao processar a avaliação.` |

```json
{
  "mensagem": "O campo 'nota' deve estar entre 0 e 10."
}
```

Exemplo de `500` ilustrativo:

```json
{
  "mensagem": "Erro interno ao processar a avaliação."
}
```

Erros do API Gateway, IAM, timeout ou inicialização do runtime podem produzir respostas diferentes, como `502`; eles não são o contrato de erro gerado pelo handler. Falhas na criação das dependências podem impedir a validação de campos. Os detalhes técnicos das falhas internas ficam nos logs da função.

### Exemplos para teste

PowerShell:

```powershell
Invoke-RestMethod -Method Post -Uri http://127.0.0.1:3000/avaliacao `
  -ContentType 'application/json' `
  -Body '{"descricao":"Excelente conteúdo","nota":9}'
```

Bash:

```bash
curl -i -X POST http://127.0.0.1:3000/avaliacao \
  -H 'Content-Type: application/json' \
  -d '{"descricao":"Excelente conteúdo","nota":9}'
```

| Caso de teste | Resultado esperado no handler |
|---|---|
| Descrição válida, nota 2 | `201`, urgência `CRITICA` |
| Descrição válida, nota 4 | `201`, urgência `ALTA` |
| Descrição válida, nota 7 | `201`, urgência `MEDIA` |
| Descrição válida, nota 10 | `201`, urgência `BAIXA` |
| Descrição em branco | `400` se dependências estiverem disponíveis |
| Nota -1 ou 11 | `400` se dependências estiverem disponíveis |
| Corpo com JSON inválido | `400` |
| Falha de persistência | `500` quando capturada pelo handler |

## Limitações do contrato

Não há paginação ou consulta HTTP, controle de idempotência, autenticação configurada, CORS na resposta, normalização estrita de tipos ou limite de tamanho da descrição na aplicação. O OpenAPI documenta a interface de referência; não é carregado pelo SAM e não adiciona validação no API Gateway. Essas alterações exigem implementação/configuração própria.
