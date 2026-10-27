package br.com.fiap.techchallenge.fase4.infrastructure.config;

import br.com.fiap.techchallenge.fase4.infrastructure.persistence.AvaliacoesTableSchema;

/**
 * Configurações compartilhadas da aplicação (variáveis de ambiente / defaults).
 */
public class AppConfig {

    private static final String DEFAULT_REGION = "us-east-1";
    private static final String DEFAULT_TABLE = "dev-avaliacoes";

    public String getNomeTabelaAvaliacoes() {
        return envOrDefault(AvaliacoesTableSchema.ENV_TABLE_NAME, DEFAULT_TABLE);
    }

    public String getEmailAdministradores() {
        return envOrDefault("ADMIN_EMAIL", "");
    }

    public String getEmailRemetente() {
        return envOrDefault("SENDER_EMAIL", "");
    }

    public String getAwsRegion() {
        return envOrDefault("AWS_REGION",
                envOrDefault("AWS_DEFAULT_REGION", DEFAULT_REGION));
    }

    private String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

}
