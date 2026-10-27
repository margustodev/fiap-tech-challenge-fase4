package br.com.fiap.techchallenge.fase4.application.dto;

/**
 * Payload de entrada do endpoint POST /avaliacao.
 */
public class AvaliacaoRequest {

    private String descricao;
    private Integer nota;

    public AvaliacaoRequest() {
    }

    public AvaliacaoRequest(String descricao, Integer nota) {
        this.descricao = descricao;
        this.nota = nota;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

}
