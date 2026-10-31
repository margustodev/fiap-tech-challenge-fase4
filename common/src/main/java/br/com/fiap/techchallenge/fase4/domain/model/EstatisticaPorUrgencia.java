
package br.com.fiap.techchallenge.fase4.domain.model;

import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;

/**
 * Representa a quantidade de avaliações
 * agrupadas por nível de urgência.
 */
public class EstatisticaPorUrgencia {

    private Urgencia urgencia;
    private Long quantidade;

    public EstatisticaPorUrgencia() {
    }

    public EstatisticaPorUrgencia(
            Urgencia urgencia,
            Long quantidade) {
        this.urgencia = urgencia;
        this.quantidade = quantidade;
    }

    public Urgencia getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(Urgencia urgencia) {
        this.urgencia = urgencia;
    }

    public Long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Long quantidade) {
        this.quantidade = quantidade;
    }
}
