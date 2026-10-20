package br.com.fiap.techchallenge.fase4.feedback.infrastructure.persistence;

/**
 * Entidade de mapeamento da tabela DynamoDB de avaliações.
 */
public class AvaliacaoEntity {

    private String id;
    private String descricao;
    private Integer nota;
    private String urgencia;
    private String dataEnvio;

}
