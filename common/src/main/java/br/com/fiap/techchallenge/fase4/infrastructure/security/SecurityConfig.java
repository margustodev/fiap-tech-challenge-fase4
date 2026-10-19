package br.com.fiap.techchallenge.fase4.infrastructure.security;

/**
 * Diretrizes e utilitários de segurança relacionados a dados de clientes.
 * Na nuvem, a governança é reforçada por IAM least privilege, criptografia
 * em repouso (DynamoDB SSE) e segredos fora do código (Parameter Store / Secrets Manager).
 */
public class SecurityConfig {

    public boolean isDadoSensivelMascarado(String valor) {
        // TODO: aplicar máscara em dados sensíveis em logs
        return false;
    }

    public String obterSegredo(String nomeSegredo) {
        // TODO: obter segredo do AWS Secrets Manager / SSM Parameter Store
        return null;
    }

}
