package br.com.fiap.techchallenge.fase4.notifier.infrastructure.notification;

import br.com.fiap.techchallenge.fase4.application.dto.NotificacaoUrgenciaDto;
import br.com.fiap.techchallenge.fase4.application.port.out.NotificacaoService;

import java.util.List;
import java.util.Objects;

/**
 * Encaminha a notificação de urgência para SES e SNS.
 */
public class CompositeNotificacaoService implements NotificacaoService {

    private final List<NotificacaoService> canais;

    public CompositeNotificacaoService(NotificacaoService... canais) {
        if (canais == null || canais.length == 0) {
            throw new IllegalArgumentException(
                    "Ao menos um canal de notificação é obrigatório."
            );
        }
        this.canais = List.of(canais);
        this.canais.forEach(Objects::requireNonNull);
    }

    @Override
    public void notificarUrgencia(NotificacaoUrgenciaDto notificacao) {
        for (NotificacaoService canal : canais) {
            canal.notificarUrgencia(notificacao);
        }
    }
}
