
package br.com.fiap.techchallenge.fase4.domain.model;

import java.time.LocalDate;

/**
 * Representa a quantidade de avaliações
 * recebidas em um determinado dia.
 */
public class EstatisticaPorDia {

    private LocalDate data;
    private Long quantidade;

    public EstatisticaPorDia() {
    }

    public EstatisticaPorDia(LocalDate data, Long quantidade) {
        this.data = data;
        this.quantidade = quantidade;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Long quantidade) {
        this.quantidade = quantidade;
    }
}
