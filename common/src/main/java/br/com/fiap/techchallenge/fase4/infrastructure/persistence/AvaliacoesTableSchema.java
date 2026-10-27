package br.com.fiap.techchallenge.fase4.infrastructure.persistence;

/**
 * Contrato da tabela DynamoDB de avaliações (atributos, índices e constantes).
 *
 * <pre>
 * Tabela: {env}-avaliacoes
 * PK: id (S)
 *
 * GSI periodo-index (consulta por intervalo de datas — relatório semanal):
 *   PK: entityType (S) = "AVALIACAO"
 *   SK: dataEnvio (S)  = Instant ISO-8601
 *
 * Atributos: descricao (S), nota (N), urgencia (S), dataEnvio (S), entityType (S)
 * </pre>
 */
public final class AvaliacoesTableSchema {

    public static final String ENV_TABLE_NAME = "AVALIACOES_TABLE_NAME";
    public static final String ENTITY_TYPE_AVALIACAO = "AVALIACAO";
    public static final String PERIODO_INDEX = "periodo-index";

    public static final String ATTR_ID = "id";
    public static final String ATTR_DESCRICAO = "descricao";
    public static final String ATTR_NOTA = "nota";
    public static final String ATTR_URGENCIA = "urgencia";
    public static final String ATTR_DATA_ENVIO = "dataEnvio";
    public static final String ATTR_ENTITY_TYPE = "entityType";

    private AvaliacoesTableSchema() {
    }

}
