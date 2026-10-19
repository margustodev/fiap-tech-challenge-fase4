package br.com.fiap.techchallenge.fase4.application.dto;

import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;

import java.time.Instant;

/**
 * Resumo de avaliação utilizado no relatório semanal
 * (descrição, urgência e data de envio).
 */
public class AvaliacaoResumoDto {

    private String descricao;
    private Urgencia urgencia;
    private Instant dataEnvio;

}
