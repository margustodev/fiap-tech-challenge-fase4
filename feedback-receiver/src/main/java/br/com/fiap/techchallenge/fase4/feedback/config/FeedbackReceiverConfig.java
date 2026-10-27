package br.com.fiap.techchallenge.fase4.feedback.config;

import br.com.fiap.techchallenge.fase4.application.mapper.AvaliacaoMapper;
import br.com.fiap.techchallenge.fase4.application.usecase.ReceberAvaliacaoUseCase;
import br.com.fiap.techchallenge.fase4.application.validator.AvaliacaoRequestValidator;
import br.com.fiap.techchallenge.fase4.domain.service.CalcularUrgenciaService;
import br.com.fiap.techchallenge.fase4.feedback.infrastructure.persistence.AvaliacaoEntity;
import br.com.fiap.techchallenge.fase4.feedback.infrastructure.persistence.DynamoDbAvaliacaoRepository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Montagem de dependências da função feedback-receiver.
 */
public class FeedbackReceiverConfig {

    public ReceberAvaliacaoUseCase receberAvaliacaoUseCase() {
        // TODO: instanciar repositório, services e caso de uso
        String nomeTabela = System.getenv("AVALIACOES_TABLE_NAME");

        if (nomeTabela == null || nomeTabela.isBlank()) {
            throw new IllegalStateException(
                    "A variável AVALIACOES_TABLE_NAME não foi configurada."
            );
        }

        DynamoDbClient dynamoDbClient = DynamoDbClient.builder().build();

        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

        DynamoDbTable<AvaliacaoEntity> tabela = enhancedClient.table(
                nomeTabela,
                TableSchema.fromBean(AvaliacaoEntity.class)
        );

        DynamoDbAvaliacaoRepository repository =
                new DynamoDbAvaliacaoRepository(tabela);

        return new ReceberAvaliacaoUseCase(
                repository,
                new AvaliacaoRequestValidator(),
                new CalcularUrgenciaService(),
                new AvaliacaoMapper()
        );
    }
}
