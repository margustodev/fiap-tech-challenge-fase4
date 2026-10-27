package br.com.fiap.techchallenge.fase4.infrastructure.persistence;

import br.com.fiap.techchallenge.fase4.infrastructure.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

/**
 * Cria a tabela de avaliações (útil para DynamoDB Local / validação).
 * Em produção a tabela é provisionada pelo SAM ({@code infrastructure/template.yaml}).
 */
public class AvaliacoesTableInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvaliacoesTableInitializer.class);

    private final DynamoDbClient client;
    private final String tableName;

    public AvaliacoesTableInitializer(DynamoDbClient client, AppConfig appConfig) {
        this.client = client;
        this.tableName = appConfig.getNomeTabelaAvaliacoes();
    }

    public void criarSeNaoExistir() {
        if (existe()) {
            LOGGER.info("Tabela {} já existe", tableName);
            return;
        }
        criar();
        aguardarAtiva();
    }

    public boolean existe() {
        try {
            client.describeTable(DescribeTableRequest.builder().tableName(tableName).build());
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    private void criar() {
        CreateTableRequest request = CreateTableRequest.builder()
                .tableName(tableName)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName(AvaliacoesTableSchema.ATTR_ID)
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName(AvaliacoesTableSchema.ATTR_ENTITY_TYPE)
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName(AvaliacoesTableSchema.ATTR_DATA_ENVIO)
                                .attributeType(ScalarAttributeType.S)
                                .build()
                )
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName(AvaliacoesTableSchema.ATTR_ID)
                                .keyType(KeyType.HASH)
                                .build()
                )
                .globalSecondaryIndexes(
                        GlobalSecondaryIndex.builder()
                                .indexName(AvaliacoesTableSchema.PERIODO_INDEX)
                                .keySchema(
                                        KeySchemaElement.builder()
                                                .attributeName(AvaliacoesTableSchema.ATTR_ENTITY_TYPE)
                                                .keyType(KeyType.HASH)
                                                .build(),
                                        KeySchemaElement.builder()
                                                .attributeName(AvaliacoesTableSchema.ATTR_DATA_ENVIO)
                                                .keyType(KeyType.RANGE)
                                                .build()
                                )
                                .projection(Projection.builder()
                                        .projectionType(ProjectionType.ALL)
                                        .build())
                                .build()
                )
                .build();

        try {
            client.createTable(request);
            LOGGER.info("Tabela {} criada com GSI {}", tableName, AvaliacoesTableSchema.PERIODO_INDEX);
        } catch (ResourceInUseException e) {
            LOGGER.info("Tabela {} já estava sendo criada", tableName);
        }
    }

    private void aguardarAtiva() {
        for (int i = 0; i < 30; i++) {
            String status = client.describeTable(DescribeTableRequest.builder()
                            .tableName(tableName)
                            .build())
                    .table()
                    .tableStatusAsString();
            if ("ACTIVE".equalsIgnoreCase(status)) {
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for table ACTIVE", e);
            }
        }
        throw new IllegalStateException("Tabela " + tableName + " não ficou ACTIVE a tempo");
    }

}
