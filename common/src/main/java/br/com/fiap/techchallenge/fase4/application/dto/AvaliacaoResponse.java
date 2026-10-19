package br.com.fiap.techchallenge.fase4.application.dto;

import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;

import java.time.Instant;

/**
 * Resposta de confirmação do recebimento de uma avaliação.
 */
public class AvaliacaoResponse {

    private String id;
    private String descricao;
    private Integer nota;
    private Urgencia urgencia;
    private Instant dataEnvio;

}
