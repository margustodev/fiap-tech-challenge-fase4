package br.com.fiap.techchallenge.fase4.notifier.infrastructure.notification;

import br.com.fiap.techchallenge.fase4.application.dto.NotificacaoUrgenciaDto;
import br.com.fiap.techchallenge.fase4.application.port.out.NotificacaoService;

/**
 * Adaptador de notificação de urgência via Amazon SES.
 */
public class SesNotificacaoService implements NotificacaoService {

    @Override
    public void notificarUrgencia(NotificacaoUrgenciaDto notificacao) {
        // TODO: montar e enviar e-mail com descrição, urgência e data de envio
    }

}
