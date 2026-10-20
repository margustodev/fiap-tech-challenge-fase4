package br.com.fiap.techchallenge.fase4.application.dto;

import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;

import java.time.Instant;

/**
 * Dados do e-mail de aviso de urgência aos administradores.
 */
public class NotificacaoUrgenciaDto {

    private String descricao;
    private Urgencia urgencia;
    private Instant dataEnvio;

}
