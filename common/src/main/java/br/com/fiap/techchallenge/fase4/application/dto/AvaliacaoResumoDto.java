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

    public AvaliacaoResumoDto() {
    }

    public AvaliacaoResumoDto(String descricao, Urgencia urgencia, Instant dataEnvio) {
        this.descricao = descricao;
        this.urgencia = urgencia;
        this.dataEnvio = dataEnvio;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Urgencia getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(Urgencia urgencia) {
        this.urgencia = urgencia;
    }

    public Instant getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(Instant dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

}
