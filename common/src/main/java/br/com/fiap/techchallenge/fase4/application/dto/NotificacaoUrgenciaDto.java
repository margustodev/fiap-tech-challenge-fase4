
package br.com.fiap.techchallenge.fase4.application.dto;

import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;

import java.time.Instant;

/**
 * Dados utilizados para notificar os administradores
 * sobre avaliações com urgência.
 */
public class NotificacaoUrgenciaDto {

    private String descricao;
    private Urgencia urgencia;
    private Instant dataEnvio;

    // Construtor vazio
    public NotificacaoUrgenciaDto() {
    }

    // Construtor com os dados da notificação
    public NotificacaoUrgenciaDto(String descricao,
                                  Urgencia urgencia,
                                  Instant dataEnvio) {
        this.descricao = descricao;
        this.urgencia = urgencia;
        this.dataEnvio = dataEnvio;
    }

    // Getters
    public String getDescricao() {
        return descricao;
    }

    public Urgencia getUrgencia() {
        return urgencia;
    }

    public Instant getDataEnvio() {
        return dataEnvio;
    }
}
