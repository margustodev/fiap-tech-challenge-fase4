package br.com.fiap.techchallenge.fase4.application.port.out;

import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepository {

    Avaliacao salvar(Avaliacao avaliacao);

    Optional<Avaliacao> buscarPorId(String id);

    List<Avaliacao> listarPorPeriodo(Instant inicio, Instant fim);
}