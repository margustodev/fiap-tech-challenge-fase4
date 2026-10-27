package br.com.fiap.techchallenge.fase4.application.validator;

import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoRequest;
import br.com.fiap.techchallenge.fase4.application.exception.AvaliacaoInvalidaException;

/**
 * Validador do payload de entrada POST /avaliacao.
 */
public class AvaliacaoRequestValidator {

    private static final int NOTA_MINIMA = 0;
    private static final int NOTA_MAXIMA = 10;

    public void validar(AvaliacaoRequest request) {
        if (request == null) {
            throw new AvaliacaoInvalidaException("Corpo da requisição é obrigatório.");
        }

        if (request.getDescricao() == null || request.getDescricao().isBlank()) {
            throw new AvaliacaoInvalidaException("O campo 'descricao' é obrigatório.");
        }

        if (request.getNota() == null) {
            throw new AvaliacaoInvalidaException("O campo 'nota' é obrigatório.");
        }

        if (request.getNota() < NOTA_MINIMA || request.getNota() > NOTA_MAXIMA) {
            throw new AvaliacaoInvalidaException(
                    "O campo 'nota' deve estar entre " + NOTA_MINIMA + " e " + NOTA_MAXIMA + "."
            );
        }
    }

}
