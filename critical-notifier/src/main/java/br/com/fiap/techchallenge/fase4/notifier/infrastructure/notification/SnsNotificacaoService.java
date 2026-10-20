package br.com.fiap.techchallenge.fase4.notifier.infrastructure.notification;

import br.com.fiap.techchallenge.fase4.application.dto.NotificacaoUrgenciaDto;
import br.com.fiap.techchallenge.fase4.application.port.out.NotificacaoService;

/**
 * Adaptador alternativo de notificação via Amazon SNS (tópico para administradores).
 */
public class SnsNotificacaoService implements NotificacaoService {

    @Override
    public void notificarUrgencia(NotificacaoUrgenciaDto notificacao) {
        // TODO: publicar mensagem no tópico SNS
    }

}
