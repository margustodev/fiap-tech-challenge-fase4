package br.com.fiap.techchallenge.fase4.infrastructure.persistence;

import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import br.com.fiap.techchallenge.fase4.infrastructure.config.AppConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validação de persistência/recuperação contra DynamoDB Local ou AWS.
 * Ativar com: DYNAMODB_ENDPOINT=http://localhost:8000 (Local) ou credenciais AWS reais.
 */
@EnabledIfEnvironmentVariable(named = "RUN_DYNAMODB_IT", matches = "true")
class DynamoDbAvaliacaoRepositoryIT {

    private static DynamoDbClient client;
    private static DynamoDbAvaliacaoRepository repository;

    @BeforeAll
    static void setUp() {
        AppConfig appConfig = new AppConfig();
        DynamoDbClientFactory factory = new DynamoDbClientFactory(appConfig);
        client = factory.createClient();
        new AvaliacoesTableInitializer(client, appConfig).criarSeNaoExistir();
        DynamoDbEnhancedClient enhanced = factory.createEnhancedClient(client);
        repository = new DynamoDbAvaliacaoRepository(enhanced, appConfig);
    }

    @AfterAll
    static void tearDown() {
        if (client != null) {
            client.close();
        }
    }

    @Test
    void deveSalvarBuscarEListarPorPeriodo() {
        String id = "it-" + UUID.randomUUID();
        Instant dataEnvio = Instant.parse("2026-10-27T18:00:00Z");
        Avaliacao avaliacao = new Avaliacao(
                id,
                "Validação de persistência DynamoDB",
                2,
                Urgencia.CRITICA,
                dataEnvio
        );

        repository.salvar(avaliacao);

        Optional<Avaliacao> porId = repository.buscarPorId(id);
        assertTrue(porId.isPresent());
        assertEquals(2, porId.get().getNota());
        assertEquals(Urgencia.CRITICA, porId.get().getUrgencia());
        assertEquals("Validação de persistência DynamoDB", porId.get().getDescricao());

        List<Avaliacao> periodo = repository.listarPorPeriodo(
                Instant.parse("2026-10-27T00:00:00Z"),
                Instant.parse("2026-10-27T23:59:59Z")
        );

        assertTrue(periodo.stream().anyMatch(a -> id.equals(a.getId())));
    }

}
