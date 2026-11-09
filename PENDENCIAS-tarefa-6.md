# Tarefa 6 - Deploy e DevOps: status e pendências

Documento de passagem para quem vai continuar a tarefa. Não é preciso ter acompanhado o trabalho anterior.

## Resumo

A pipeline, a infraestrutura como código e a documentação estão prontas e validadas localmente. **Nada foi publicado na AWS ainda**, porque a conta AWS que seria usada no teste aparentemente foi suspensa.

Para concluir, falta usar uma conta AWS válida, fazer a configuração inicial, rodar o primeiro deploy e validar o funcionamento na nuvem.

**Onde está o código:** branch `feature/task-06-deploy-devops`, no repositório do grupo, com PR em draft.

## O que já foi feito

### Entregue

| Item | Descrição |
|---|---|
| Validação de PRs | Build e testes em JDK 25, teste de integração com DynamoDB Local e validação dos templates, sem credenciais AWS |
| Deploy automatizado | Build único dos JARs, deploy automático em `dev`, verificação da API e aprovação manual para `prod` |
| Bootstrap da conta | `infrastructure/bootstrap.yaml`: OIDC, uma role de deploy por ambiente, bucket de artefatos, identidades SES, role de logs do API Gateway e alerta de custo |
| Ajustes no template | Throttling da API, e-mails obrigatórios no deploy e regra agendada com nome por ambiente |
| Ambientes | Perfis `dev` e `prod` em `infrastructure/samconfig.toml`, cada um com sua stack |
| Verificação pós-deploy | `scripts/smoke-test.sh`, com testes automatizados próprios |
| Configuração do GitHub | `scripts/setup-github.sh`: variáveis, Environments, secrets, aprovadores e proteção da `main` |
| Documentação | `docs/DEPLOY_CICD.md`: diagrama, justificativas, passo a passo, segurança, rollback e roteiro do vídeo |

### Validado

Estas verificações foram executadas na máquina local:

- 55 testes automatizados aprovados, com JDK 25.
- Teste de integração aprovado contra o DynamoDB Local.
- Os dois templates aprovados em `sam validate --lint`.
- Workflows aprovados no `actionlint`.
- 4 testes do script de verificação pós-deploy aprovados.
- Revisão de código feita contra a spec, com os achados principais corrigidos.

### Ainda não validado

- Nada rodou na AWS real nem no GitHub Actions.
- As permissões das roles de deploy só se confirmam no primeiro deploy, e pode ser necessário ajustá-las.
- `scripts/setup-github.sh` teve apenas a sintaxe verificada, porque exige admin no repositório.

### Decisões tomadas

- AWS em `us-east-1`, com AWS SAM como IaC e GitHub Actions como CI/CD.
- Dois ambientes, `dev` e `prod`, cada um com sua stack.
- Autenticação da pipeline por OIDC, sem access keys.
- SES em modo sandbox, e rollback a cargo do CloudFormation.

## Pendências, em ordem

### 1. Enviar o código ao GitHub

- [x] Enviar a branch `feature/task-06-deploy-devops` para o repositório do grupo e abrir o PR como draft.

O workflow de deploy vai falhar na etapa "Conferir configuração do ambiente" até o item 3 ser concluído. Isso é esperado. A etapa de build já comprova se o projeto compila no GitHub.

### 2. Preparar a conta AWS

- [ ] Ter uma conta AWS normal. **AWS Academy / Learner Lab não serve**, porque o bootstrap cria roles IAM e OIDC.
- [ ] Ativar MFA no usuário root.
- [ ] Definir o e-mail do administrador e o e-mail remetente. Podem ser o mesmo.
- [ ] No Console AWS, selecionar `us-east-1` e abrir o CloudShell.
- [ ] Baixar o código e aplicar o bootstrap:

```bash
git clone -b feature/task-06-deploy-devops https://github.com/margustodev/fiap-tech-challenge-fase4
cd fiap-tech-challenge-fase4/infrastructure

aws cloudformation deploy \
  --template-file bootstrap.yaml \
  --stack-name fiap-tech-challenge-fase-4-bootstrap \
  --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
  --parameter-overrides \
    AdminEmail=seu-admin@email.com \
    SenderEmail=seu-remetente@email.com \
    MonthlyBudgetUsd=10
```

- [ ] Anotar os outputs `DeployRoleArndev`, `DeployRoleArnprod` e `ArtifactBucketName`:

```bash
aws cloudformation describe-stacks \
  --stack-name fiap-tech-challenge-fase-4-bootstrap \
  --query "Stacks[0].Outputs" --output table
```

- [ ] Clicar nos links de verificação que o SES enviar aos e-mails informados.

### 3. Configurar o GitHub (precisa de admin no repositório)

O Marcelo é o dono do repositório, e o Lucas tem apenas permissão de escrita.

- [ ] Com a branch baixada e o GitHub CLI autenticado, rodar na raiz do projeto, no Git Bash:

```bash
bash scripts/setup-github.sh <DeployRoleArndev> <DeployRoleArnprod> <ArtifactBucketName> <email-admin> <email-remetente>
```

Os aprovadores padrão de `prod` são Lucas e Marcelo.

### 4. Primeiro deploy em `dev`

- [ ] Em **Actions > Deploy**, reexecutar a execução da branch.
- [ ] Confirmar que a etapa **Verificar API publicada** passou.
- [ ] Confirmar o e-mail de inscrição do SNS.

| Se acontecer | O que fazer |
|---|---|
| `AccessDenied` na role | Adicionar a permissão indicada em `infrastructure/bootstrap.yaml`, reaplicar o bootstrap e reexecutar |
| Stack em `ROLLBACK_COMPLETE` | Rodar `aws cloudformation delete-stack --stack-name fiap-tech-challenge-fase-4-dev` no CloudShell e reexecutar |
| Falta de configuração no GitHub | Rodar novamente o `setup-github.sh` |

Possíveis permissões faltantes, apontadas na revisão: leitura de `logs:*` e `kms:*`, além das ações de Streams na role da função `critical-notifier`.

### 5. Validar na AWS

Esta lista completa as validações deixadas pela Tarefa 7.

- [ ] Uma avaliação com nota baixa gera o e-mail crítico.
- [ ] O relatório semanal é enviado ao invocar a função manualmente.
- [ ] Os alarmes notificam o tópico SNS.
- [ ] O dashboard e as métricas exibem dados reais.
- [ ] Os logs de acesso da API são gravados.
- [ ] As três funções operam com suas roles restritas.
- [ ] As requisições acima do limite recebem HTTP 429.

### 6. Finalizar e publicar em `prod`

- [ ] Remover de `.github/workflows/deploy.yml` a linha do gatilho temporário `feature/task-06-deploy-devops` e o comentário acima dela.
- [ ] Alinhar com o autor do PR #6, de documentação, que está em draft e altera o README.
- [ ] Tirar o PR de draft, aguardar as três validações e fazer o merge.
- [ ] Em **Actions > Deploy**, clicar em **Review deployments** e aprovar `prod`.
- [ ] Confirmar o e-mail de inscrição do SNS de `prod`.

### 7. Entrega

- [ ] Gravar o vídeo, usando o roteiro da seção 9 de `docs/DEPLOY_CICD.md`.
- [ ] Atualizar a seção "Status atual" do README, que ainda indica o deploy como pendente.

## Decisões a confirmar

Estas opções foram adotadas no código, mas ainda não foram confirmadas pelo grupo:

- Aprovadores de `prod`: Marcelo.
- Alerta de gasto de US$ 10 por mês, com avisos em 80% e 100%.
- Validações obrigatórias para integrar na `main`.

## Melhorias opcionais

Foram apontadas na revisão, mas não são necessárias para a entrega:

- `enforce_admins: false` permite que admins ignorem as validações da `main`.
- `workflow_dispatch` permite publicar qualquer branch em `dev`, embora não alcance `prod`.
- O bloco de `checkout` + `setup-java` se repete nos workflows.
