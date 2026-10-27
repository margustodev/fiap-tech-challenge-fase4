package br.com.fiap.techchallenge.fase4.application.dto;

/**
 * Payload de entrada do endpoint POST /avaliacao.
 */
public class AvaliacaoRequest {

    public Integer getNota() {
        return nota;
    }

    public String getDescricao() {
        return descricao;
    }

    private String descricao;
    private Integer nota;

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }
}
