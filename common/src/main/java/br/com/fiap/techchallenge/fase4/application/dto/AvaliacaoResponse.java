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

    public AvaliacaoResponse(String id, String descricao, Integer nota,
                             Urgencia urgencia, Instant dataEnvio) {
        this.id = id;
        this.descricao = descricao;
        this.nota = nota;
        this.urgencia = urgencia;
        this.dataEnvio = dataEnvio;
    }

    public String getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public Integer getNota() {
        return nota;
    }

    public Urgencia getUrgencia() {
        return urgencia;
    }

    public Instant getDataEnvio() {
        return dataEnvio;
    }
}
