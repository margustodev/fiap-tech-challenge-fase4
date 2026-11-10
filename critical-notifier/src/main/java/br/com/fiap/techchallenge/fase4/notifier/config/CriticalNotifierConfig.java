package br.com.fiap.techchallenge.fase4.notifier.config;

import br.com.fiap.techchallenge.fase4.application.port.out.NotificacaoService;
import br.com.fiap.techchallenge.fase4.application.usecase.ProcessarNotificacaoCriticaUseCase;
import br.com.fiap.techchallenge.fase4.notifier.infrastructure.mapper.DynamoDbStreamMapper;
import br.com.fiap.techchallenge.fase4.notifier.infrastructure.notification.CompositeNotificacaoService;
import br.com.fiap.techchallenge.fase4.notifier.infrastructure.notification.SesNotificacaoService;
import br.com.fiap.techchallenge.fase4.notifier.infrastructure.notification.SnsNotificacaoService;

/**
 * Montagem de dependências da função critical-notifier.
 */
public class CriticalNotifierConfig {

    public DynamoDbStreamMapper dynamoDbStreamMapper() {
        return new DynamoDbStreamMapper();
    }

    public NotificacaoService notificacaoService() {
        return new CompositeNotificacaoService(
                new SesNotificacaoService(),
                new SnsNotificacaoService()
        );
    }

    public ProcessarNotificacaoCriticaUseCase processarNotificacaoCriticaUseCase() {
        return new ProcessarNotificacaoCriticaUseCase(notificacaoService());
    }
}
