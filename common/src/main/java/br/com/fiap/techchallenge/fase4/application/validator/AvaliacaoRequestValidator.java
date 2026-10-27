package br.com.fiap.techchallenge.fase4.application.validator;

import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoRequest;
import br.com.fiap.techchallenge.fase4.application.exception.AvaliacaoInvalidaException;

/**
 * Validador do payload de entrada POST /avaliacao.
 */
public class AvaliacaoRequestValidator {

    public void validar(AvaliacaoRequest request) {
        // TODO: validar descricao obrigatória e nota entre 0 e 10
        if (request == null) {
            throw new AvaliacaoInvalidaException("A avaliação é obrigatória.");
        }

        if (request.getDescricao() == null || request.getDescricao().isBlank()) {
            throw new AvaliacaoInvalidaException("A descrição é obrigatória.");
        }

        if (request.getNota() == null || request.getNota() < 0 || request.getNota() > 10) {
            throw new AvaliacaoInvalidaException("A nota deve ser um inteiro entre 0 e 10.");
        }
    }

}
