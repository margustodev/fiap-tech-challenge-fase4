
package br.com.fiap.techchallenge.fase4.application.port.out;

import br.com.fiap.techchallenge.fase4.application.dto.NotificacaoUrgenciaDto;

/**
 * Interface responsável pelo envio de notificações
 * para avaliações com urgência.
 */
public interface NotificacaoService {

    void notificarUrgencia(NotificacaoUrgenciaDto notificacao);

}
