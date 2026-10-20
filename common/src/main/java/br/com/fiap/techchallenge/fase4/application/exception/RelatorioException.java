package br.com.fiap.techchallenge.fase4.application.exception;

/**
 * Exceção lançada quando falha a geração ou o envio do relatório.
 */
public class RelatorioException extends RuntimeException {

    public RelatorioException(String mensagem) {
        super(mensagem);
    }

    public RelatorioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

}
