package br.com.fiap.techchallenge.fase4.application.exception;

/**
 * Exceção lançada quando falha o envio de notificação.
 */
public class NotificacaoException extends RuntimeException {

    public NotificacaoException(String mensagem) {
        super(mensagem);
    }

    public NotificacaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

}
