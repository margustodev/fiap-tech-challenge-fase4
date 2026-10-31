
package br.com.fiap.techchallenge.fase4.report.infrastructure.report;

import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoResumoDto;
import br.com.fiap.techchallenge.fase4.application.dto.RelatorioSemanalDto;
import br.com.fiap.techchallenge.fase4.application.port.out.EmailService;
import br.com.fiap.techchallenge.fase4.application.port.out.RelatorioService;
import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.EstatisticaPorDia;
import br.com.fiap.techchallenge.fase4.domain.model.EstatisticaPorUrgencia;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Responsável por formatar e enviar o relatório
 * semanal de avaliações por e-mail.
 */
public class SesRelatorioService implements RelatorioService {

    private static final Locale LOCALE_BR =
            Locale.forLanguageTag("pt-BR");

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .withZone(ZoneOffset.UTC);

    private final EmailService emailService;
    private final String destinatario;

    public SesRelatorioService(
            EmailService emailService,
            String destinatario) {

        this.emailService = Objects.requireNonNull(
                emailService,
                "O serviço de e-mail é obrigatório."
        );

        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException(
                    "E-mail do administrador não configurado."
            );
        }

        this.destinatario = destinatario;
    }

    @Override
    public void enviarRelatorio(RelatorioSemanalDto relatorio) {

        Objects.requireNonNull(
                relatorio,
                "O relatório não pode ser nulo."
        );

        String assunto = String.format(
                "Relatório Semanal de Avaliações - %s a %s",
                FORMATO_DATA.format(relatorio.getDataInicio()),
                FORMATO_DATA.format(relatorio.getDataFim())
        );

        String corpo = formatarRelatorio(relatorio);

        emailService.enviar(
                destinatario,
                assunto,
                corpo
        );
    }

    private String formatarRelatorio(
            RelatorioSemanalDto relatorio) {

        StringBuilder texto = new StringBuilder();

        texto.append("RELATÓRIO SEMANAL DE AVALIAÇÕES\n");
        texto.append("================================\n\n");

        texto.append("Período: ")
                .append(FORMATO_DATA.format(relatorio.getDataInicio()))
                .append(" a ")
                .append(FORMATO_DATA.format(relatorio.getDataFim()))
                .append(" (UTC)\n\n");

        texto.append("RESUMO GERAL\n");
        texto.append("--------------------------------\n");

        texto.append("Total de avaliações: ")
                .append(relatorio.getTotalAvaliacoes())
                .append("\n");

        texto.append("Média das avaliações: ")
                .append(String.format(
                        LOCALE_BR,
                        "%.2f",
                        relatorio.getMediaAvaliacoes()
                ))
                .append("\n\n");

        texto.append("QUANTIDADE POR DIA\n");
        texto.append("--------------------------------\n");

        List<EstatisticaPorDia> porDia =
                relatorio.getQuantidadePorDia();

        if (porDia == null || porDia.isEmpty()) {
            texto.append("Nenhuma avaliação registrada.\n");
        } else {
            for (EstatisticaPorDia estatistica : porDia) {
                texto.append(
                                FORMATO_DATA.format(estatistica.getData())
                        )
                        .append(": ")
                        .append(estatistica.getQuantidade())
                        .append(" avaliação(ões)\n");
            }
        }

        texto.append("\nQUANTIDADE POR URGÊNCIA\n");
        texto.append("--------------------------------\n");

        List<EstatisticaPorUrgencia> porUrgencia =
                relatorio.getQuantidadePorUrgencia();

        for (Urgencia urgencia : new Urgencia[]{
                Urgencia.CRITICA,
                Urgencia.ALTA,
                Urgencia.MEDIA,
                Urgencia.BAIXA
        }) {

            long quantidade = porUrgencia == null
                    ? 0L
                    : porUrgencia.stream()
                    .filter(item ->
                            item.getUrgencia() == urgencia
                    )
                    .mapToLong(
                            EstatisticaPorUrgencia::getQuantidade
                    )
                    .sum();

            texto.append(urgencia.name())
                    .append(": ")
                    .append(quantidade)
                    .append("\n");
        }

        texto.append("\nDETALHAMENTO DAS AVALIAÇÕES\n");
        texto.append("--------------------------------\n");

        List<AvaliacaoResumoDto> avaliacoes =
                relatorio.getAvaliacoes();

        if (avaliacoes == null || avaliacoes.isEmpty()) {

            texto.append(
                    "Nenhuma avaliação recebida no período.\n"
            );

        } else {

            int numero = 1;

            for (AvaliacaoResumoDto avaliacao : avaliacoes) {

                texto.append("\nAvaliação ")
                        .append(numero++)
                        .append("\n");

                texto.append("Descrição: ")
                        .append(avaliacao.getDescricao())
                        .append("\n");

                texto.append("Urgência: ")
                        .append(avaliacao.getUrgencia())
                        .append("\n");

                texto.append("Data de envio: ")
                        .append(FORMATO_DATA_HORA.format(
                                avaliacao.getDataEnvio()
                        ))
                        .append(" UTC\n");
            }
        }

        texto.append("\n================================\n");
        texto.append("Relatório gerado automaticamente.\n");
        texto.append("FIAP Tech Challenge - Fase 4\n");

        return texto.toString();
    }
}
