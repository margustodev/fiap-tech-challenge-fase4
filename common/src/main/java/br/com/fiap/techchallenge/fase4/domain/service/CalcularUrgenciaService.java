package br.com.fiap.techchallenge.fase4.domain.service;

import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;

/**
 * Serviço de domínio responsável por calcular a urgência a partir da nota.
 */
public class CalcularUrgenciaService {

    public Urgencia calcular(Integer nota) {
        if (nota == null || nota < 0 || nota > 10) {
            throw new IllegalArgumentException("A nota deve estar entre 0 e 10.");
        }

        if (nota <= 2) {
            return Urgencia.CRITICA;
        }

        if (nota <= 5) {
            return Urgencia.ALTA;
        }

        if (nota <= 7) {
            return Urgencia.MEDIA;
        }

        return Urgencia.BAIXA;
    }

}
