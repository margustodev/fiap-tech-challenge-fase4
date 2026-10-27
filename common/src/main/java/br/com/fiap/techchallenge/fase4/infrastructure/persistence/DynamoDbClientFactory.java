package br.com.fiap.techchallenge.fase4.infrastructure.persistence;

import br.com.fiap.techchallenge.fase4.infrastructure.config.AppConfig;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;
import java.time.Duration;

/**
 * Fábrica do cliente DynamoDB (AWS real ou endpoint local via DYNAMODB_ENDPOINT).
 */
public class DynamoDbClientFactory {

    public static final String ENV_ENDPOINT = "DYNAMODB_ENDPOINT";

    private final AppConfig appConfig;

    public DynamoDbClientFactory(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public DynamoDbClient createClient() {
        String endpoint = resolveEndpoint();
        System.out.println("[DynamoDbClientFactory] endpoint=" + endpoint
                + " table=" + appConfig.getNomeTabelaAvaliacoes()
                + " region=" + appConfig.getAwsRegion());

        var builder = DynamoDbClient.builder()
                .region(Region.of(appConfig.getAwsRegion()))
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .apiCallTimeout(Duration.ofSeconds(10))
                        .apiCallAttemptTimeout(Duration.ofSeconds(5))
                        .build());

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("local", "local")));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }

    public DynamoDbEnhancedClient createEnhancedClient(DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(client)
                .build();
    }

    public DynamoDbEnhancedClient createEnhancedClient() {
        return createEnhancedClient(createClient());
    }

    /**
     * Ordem: DYNAMODB_ENDPOINT → AWS_ENDPOINT_URL_DYNAMODB → AWS_ENDPOINT_URL.
     */
    private String resolveEndpoint() {
        String endpoint = System.getenv(ENV_ENDPOINT);
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = System.getenv("AWS_ENDPOINT_URL_DYNAMODB");
        }
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = System.getenv("AWS_ENDPOINT_URL");
        }
        return endpoint;
    }

}
