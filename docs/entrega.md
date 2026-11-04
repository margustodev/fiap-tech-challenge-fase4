# Checklist de entrega e pendências

[Voltar ao README](../README.md)

## Base da revisão

Documentação conferida com o código da `main`, commit `be004caabdf44cb3c7a0122d06f19451389bf56b`. Revisão documental realizada em 03/11/2026 (America/Sao_Paulo). A integração GitHub identificou o repositório como público. Não houve acesso ao IntelliJ da máquina do autor nem ao ambiente AWS.

Esta alteração organiza e documenta a implementação existente. A revisão do professor deve considerar o código, a validação executada e as evidências da cloud; documentação não substitui execução.

## Tarefas solicitadas

| Tarefa | Situação nesta revisão | Evidência/ação restante |
|---|---|---|
| Documentar arquitetura | Documentada | [Arquitetura](arquitetura.md), fluxos e decisões baseados no código/SAM |
| Documentar cada função e responsabilidade | Documentada | [Três funções](funcoes-serverless.md), entradas, saídas, falhas e limitações |
| README com instalação, documentação e deploy | Documentado | [README](../README.md), [instalação](instalacao.md) e [deploy](deploy.md) |
| Documentar endpoints | Documentado | [API](api.md) e [OpenAPI](openapi.yaml), único endpoint HTTP declarado |
| Repositório aberto e organizado | Público na consulta; documentação organizada | Confirmar acesso anônimo e revisar a branch antes de integrar à main |
| Infraestrutura/configuração como código | Parcial: templates já existiam | Revisar, validar e provisionar; não marcar como operacional apenas pela presença do YAML |
| Configurar CI/CD | Pendente | Não há workflow versionado nesta base |
| Automatizar deploy das funções/aplicação | Pendente | Validar pipeline que publica os três JARs e atualiza a stack |
| Criar ambientes/configurações necessárias | Parcial: parâmetros declarados | `dev`/`hml`/`prod` são valores aceitos, não evidência de ambientes criados |

## O que depende da infraestrutura e dos acessos

Ainda não é possível comprovar, com o código e os acessos desta revisão:

- URL pública funcional e resposta da API na AWS.
- Provisionamento real de tabela/GSI/stream, funções, roles, regra de agenda, tópico e alarmes.
- Entrega real de e-mails, verificação SES e confirmação da assinatura SNS.
- Execução agendada, métricas/dashboard/alertas e comportamento de falhas na cloud.
- Autenticação e execução do CI/CD, deploy automatizado, promoção e rollback por ambiente.

Esses pontos exigem preparação de DevOps/conta AWS e evidências de execução. Os templates e o pipeline podem ser escritos/revisados antes; o bloqueio é a validação operacional com configuração e permissões reais, não a impossibilidade de escrever código.

## Pendências que NÃO são resolvidas apenas por DevOps

| Item encontrado no código/build | Impacto e encaminhamento |
|---|---|
| `SnsNotificacaoService` em TODO | Alerta de negócio via SNS não está implementado; SES é o caminho atual |
| `ProcessarNotificacaoCriticaUseCase` e `CriticalNotifierConfig` em TODO | A orquestração atual ocorre no handler; decidir refatoração ou remoção dos esqueletos |
| `RelatorioAggregator` e `DateTimeUtils` em TODO | Não são usados no fluxo atual de relatório; consolidar ou remover para reduzir ambiguidade |
| `SecurityConfig` e `DataPrivacyHelper` em TODO | Não há busca de segredo ou mascaramento implementados nesses helpers |
| `CloudWatchMetricsService` escreve em log | Não afirmar publicação de métricas customizadas; decidir instrumentação posterior |
| `500` inclui mensagem da exceção | Revisar exposição de detalhes internos |
| Jackson permite coerções padrão para `Integer` | Contrato exige inteiro; adicionar validação estrita se essa garantia for exigida |
| Sem idempotência/deduplicação | Requisições ou eventos repetidos podem duplicar avaliações/e-mails |
| Reporter usa relógio da execução | Reprocessamento em outra semana seleciona outra janela |
| Surefire não fixado em `critical-notifier` | Conferir descoberta de JUnit 5 e padronizar configuração de build |
| Scripts PowerShell sem verificação explícita de exit code nativo | Não usar a última mensagem do script como comprovação automática de sucesso |

Nenhuma lógica Java, POM, script ou infraestrutura foi alterada nesta revisão documental. Os itens acima são identificados para planejamento, sem marcar funcionalidades não implementadas como concluídas.

## Validação desta alteração

- Revisão estática dos handlers, casos de uso, DTOs, mappers, persistência, testes existentes e templates.
- Conferência dos links locais e da estrutura do contrato OpenAPI, com resultado registrado na descrição da revisão/PR.
- Testes Java/build **não executados**: ambiente de revisão com JDK 17 e sem Maven, enquanto o POM exige release 25.
- DynamoDB Local, SAM local, deploy AWS, SES, Streams, EventBridge e pipeline **não executados/validados** nesta revisão.

Para fechar a entrega, rodar o build/testes com JDK 25, conferir os relatórios de teste e executar o roteiro de [deploy](deploy.md). Não há evidência suficiente para declarar a solução completa em produção.

## Roteiro de evidências para apresentação

1. Mostrar o repositório público, README e documentos técnicos.
2. Mostrar build e testes executados, incluindo quantidade de testes por módulo.
3. Apresentar stack, configurações e recursos implantados, sem expor credenciais.
4. Enviar avaliação válida e inválida pela API e mostrar a persistência.
5. Demonstrar alerta para nota baixa e ausência de alerta para nota alta.
6. Demonstrar relatório da semana anterior, com estatísticas e e-mail.
7. Mostrar execução do pipeline e um deploy rastreável ao commit.
8. Identificar limitações remanescentes e anexar o link do vídeo à entrega quando disponível.
