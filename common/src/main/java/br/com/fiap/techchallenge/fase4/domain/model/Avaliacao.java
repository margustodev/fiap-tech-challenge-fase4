package br.com.fiap.techchallenge.fase4.domain.model;

import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;

import java.time.Instant;

/**
 * Entidade de domínio que representa uma avaliação/feedback de curso.
 */
public class Avaliacao {

    private String id;
    private String descricao;
    private Integer nota;
    private Urgencia urgencia;
    private Instant dataEnvio;

}
