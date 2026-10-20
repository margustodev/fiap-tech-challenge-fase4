package br.com.fiap.techchallenge.fase4.infrastructure.config;

/**
 * Configurações compartilhadas da aplicação (variáveis de ambiente, nomes de recursos, etc.).
 */
public class AppConfig {

    public String getNomeTabelaAvaliacoes() {
        // TODO: ler de variável de ambiente
        return null;
    }

    public String getEmailAdministradores() {
        // TODO: ler de variável de ambiente / Parameter Store / Secrets Manager
        return null;
    }

    public String getEmailRemetente() {
        // TODO: ler de variável de ambiente
        return null;
    }

    public String getAwsRegion() {
        // TODO: ler de variável de ambiente
        return null;
    }

}
