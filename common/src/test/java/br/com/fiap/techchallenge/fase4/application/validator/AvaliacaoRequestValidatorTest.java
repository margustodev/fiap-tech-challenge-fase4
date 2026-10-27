package br.com.fiap.techchallenge.fase4.application.validator;

import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoRequest;
import br.com.fiap.techchallenge.fase4.application.exception.AvaliacaoInvalidaException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AvaliacaoRequestValidatorTest {

    private final AvaliacaoRequestValidator validator = new AvaliacaoRequestValidator();

    @Test
    void deveAceitarRequestValido() {
        assertDoesNotThrow(() -> validator.validar(new AvaliacaoRequest("Bom curso", 8)));
    }

    @Test
    void deveRejeitarDescricaoVazia() {
        assertThrows(AvaliacaoInvalidaException.class,
                () -> validator.validar(new AvaliacaoRequest("  ", 5)));
    }

    @Test
    void deveRejeitarNotaForaDoIntervalo() {
        assertThrows(AvaliacaoInvalidaException.class,
                () -> validator.validar(new AvaliacaoRequest("Ok", 11)));
        assertThrows(AvaliacaoInvalidaException.class,
                () -> validator.validar(new AvaliacaoRequest("Ok", -1)));
    }

    @Test
    void deveRejeitarNotaNula() {
        assertThrows(AvaliacaoInvalidaException.class,
                () -> validator.validar(new AvaliacaoRequest("Ok", null)));
    }

}
