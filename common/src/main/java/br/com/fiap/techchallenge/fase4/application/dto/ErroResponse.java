package br.com.fiap.techchallenge.fase4.application.dto;

/**
 * Corpo padronizado de erro da API.
 */
public class ErroResponse {

    private String mensagem;

    public ErroResponse() {
    }

    public ErroResponse(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

}
