package br.com.fiap.techchallenge.fase4.report.infrastructure.email;

import br.com.fiap.techchallenge.fase4.application.port.out.EmailService;

/**
 * Adaptador de envio de e-mail via Amazon SES para o relatório semanal.
 */
public class SesEmailService implements EmailService {

    @Override
    public void enviar(String destinatario, String assunto, String corpo) {
        // TODO: enviar e-mail via SES
    }

}
