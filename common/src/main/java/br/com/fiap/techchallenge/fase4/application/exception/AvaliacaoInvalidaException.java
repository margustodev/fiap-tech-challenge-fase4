package br.com.fiap.techchallenge.fase4.application.exception;

/**
 * Exceção lançada quando os dados da avaliação são inválidos.
 */
public class AvaliacaoInvalidaException extends RuntimeException {

    public AvaliacaoInvalidaException(String mensagem) {
        super(mensagem);
    }

}
