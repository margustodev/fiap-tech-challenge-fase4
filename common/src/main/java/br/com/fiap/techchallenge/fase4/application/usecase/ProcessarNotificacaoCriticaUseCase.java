package br.com.fiap.techchallenge.fase4.application.usecase;

import br.com.fiap.techchallenge.fase4.application.dto.NotificacaoUrgenciaDto;
import br.com.fiap.techchallenge.fase4.application.port.out.NotificacaoService;
import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;

import java.util.Objects;

/**
 * Caso de uso: processar e enviar notificação para avaliações críticas.
 */
public class ProcessarNotificacaoCriticaUseCase {

    private final NotificacaoService notificacaoService;

    public ProcessarNotificacaoCriticaUseCase(NotificacaoService notificacaoService) {
        this.notificacaoService = Objects.requireNonNull(
                notificacaoService,
                "Serviço de notificação é obrigatório."
        );
    }

    public boolean executar(Avaliacao avaliacao) {
        Objects.requireNonNull(avaliacao, "Avaliação é obrigatória.");

        Urgencia urgencia = avaliacao.getUrgencia();
        if (urgencia != Urgencia.ALTA && urgencia != Urgencia.CRITICA) {
            return false;
        }

        NotificacaoUrgenciaDto notificacao = new NotificacaoUrgenciaDto(
                avaliacao.getDescricao(),
                urgencia,
                avaliacao.getDataEnvio()
        );

        notificacaoService.notificarUrgencia(notificacao);
        return true;
    }
}
