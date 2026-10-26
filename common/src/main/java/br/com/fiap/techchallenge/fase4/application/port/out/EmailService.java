
package br.com.fiap.techchallenge.fase4.application.port.out;

/**
 * Interface responsável por definir o envio de e-mails.
 */
public interface EmailService {

    void enviar(String destinatario, String assunto, String corpo);

}
