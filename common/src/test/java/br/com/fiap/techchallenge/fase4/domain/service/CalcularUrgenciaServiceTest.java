package br.com.fiap.techchallenge.fase4.domain.service;

import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalcularUrgenciaServiceTest {

    private final CalcularUrgenciaService service = new CalcularUrgenciaService();

    @Test
    void deveCalcularFaixasDeUrgencia() {
        assertEquals(Urgencia.CRITICA, service.calcular(0));
        assertEquals(Urgencia.CRITICA, service.calcular(2));
        assertEquals(Urgencia.ALTA, service.calcular(3));
        assertEquals(Urgencia.ALTA, service.calcular(4));
        assertEquals(Urgencia.MEDIA, service.calcular(5));
        assertEquals(Urgencia.MEDIA, service.calcular(7));
        assertEquals(Urgencia.BAIXA, service.calcular(8));
        assertEquals(Urgencia.BAIXA, service.calcular(10));
    }

}
