package br.com.fiap.techchallenge.fase4.infrastructure.persistence;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

/**
 * Item DynamoDB da avaliação.
 * PK {@code id}; GSI {@code periodo-index} em ({@code entityType}, {@code dataEnvio}).
 */
@DynamoDbBean
public class AvaliacaoEntity {

    private String id;
    private String descricao;
    private Integer nota;
    private String urgencia;
    private String dataEnvio;
    private String entityType;

    public AvaliacaoEntity() {
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute(AvaliacoesTableSchema.ATTR_ID)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDbAttribute(AvaliacoesTableSchema.ATTR_DESCRICAO)
    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @DynamoDbAttribute(AvaliacoesTableSchema.ATTR_NOTA)
    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    @DynamoDbAttribute(AvaliacoesTableSchema.ATTR_URGENCIA)
    public String getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(String urgencia) {
        this.urgencia = urgencia;
    }

    @DynamoDbSecondarySortKey(indexNames = AvaliacoesTableSchema.PERIODO_INDEX)
    @DynamoDbAttribute(AvaliacoesTableSchema.ATTR_DATA_ENVIO)
    public String getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(String dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = AvaliacoesTableSchema.PERIODO_INDEX)
    @DynamoDbAttribute(AvaliacoesTableSchema.ATTR_ENTITY_TYPE)
    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

}
