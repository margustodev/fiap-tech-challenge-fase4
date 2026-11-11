package br.com.fiap.techchallenge.fase4.infrastructure.security;

/**
 * Diretrizes e utilitários de segurança relacionados a dados de clientes.
 * Na nuvem, a governança é reforçada por IAM least privilege, criptografia
 * em repouso (DynamoDB SSE) e parâmetros fora do código (SAM Parameters / env).
 */
public class SecurityConfig {

    private final DataPrivacyHelper dataPrivacyHelper;

    public SecurityConfig() {
        this(new DataPrivacyHelper());
    }

    public SecurityConfig(DataPrivacyHelper dataPrivacyHelper) {
        this.dataPrivacyHelper = dataPrivacyHelper;
    }

    public boolean isDadoSensivelMascarado(String valor) {
        if (valor == null || valor.isBlank()) {
            return true;
        }
        return valor.contains("***");
    }

    public String mascararParaLog(String descricao) {
        String mascarado = dataPrivacyHelper.mascararDescricao(descricao);
        if (!isDadoSensivelMascarado(mascarado)) {
            return "***";
        }
        return mascarado;
    }

    /**
     * Obtém parâmetro sensível injetado via variável de ambiente
     * (SAM Parameters / secrets do ambiente), sem hardcode no código.
     */
    public String obterSegredo(String nomeSegredo) {
        if (nomeSegredo == null || nomeSegredo.isBlank()) {
            throw new IllegalArgumentException("Nome do segredo é obrigatório.");
        }

        String valor = System.getenv(nomeSegredo);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    "Segredo não configurado: " + nomeSegredo
            );
        }
        return valor;
    }
}