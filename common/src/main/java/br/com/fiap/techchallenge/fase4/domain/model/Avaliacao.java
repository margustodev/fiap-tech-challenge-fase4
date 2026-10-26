
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

    // Construtor vazio
    public Avaliacao() {
    }

    // Construtor com todos os atributos
    public Avaliacao(String id, String descricao, Integer nota,
                     Urgencia urgencia, Instant dataEnvio) {
        this.id = id;
        this.descricao = descricao;
        this.nota = nota;
        this.urgencia = urgencia;
        this.dataEnvio = dataEnvio;
    }

    // Getters
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
