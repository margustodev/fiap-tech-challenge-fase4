package br.com.fiap.techchallenge.fase4.domain.service;

import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;

/**
 * Serviço de domínio responsável por calcular a urgência a partir da nota.
 *
 * <ul>
 *   <li>0–2 → CRITICA</li>
 *   <li>3–4 → ALTA</li>
 *   <li>5–7 → MEDIA</li>
 *   <li>8–10 → BAIXA</li>
 * </ul>
 */
public class CalcularUrgenciaService {

    public Urgencia calcular(Integer nota) {
        if (nota == null) {
            throw new IllegalArgumentException("Nota é obrigatória para calcular urgência.");
        }
        if (nota < 0 || nota > 10) {
            throw new IllegalArgumentException("Nota deve estar entre 0 e 10.");
        }

        if (nota <= 2) {
            return Urgencia.CRITICA;
        }
        if (nota <= 4) {
            return Urgencia.ALTA;
        }
        if (nota <= 7) {
            return Urgencia.MEDIA;
        }
        return Urgencia.BAIXA;
    }

}
