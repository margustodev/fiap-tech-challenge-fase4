package br.com.fiap.techchallenge.fase4.infrastructure.util;

import br.com.fiap.techchallenge.fase4.application.exception.AvaliacaoInvalidaException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utilitário compartilhado de serialização/deserialização JSON.
 */
public class JsonMapper {

    private final ObjectMapper objectMapper;

    public JsonMapper() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .findAndRegisterModules();
    }

    public <T> T fromJson(String json, Class<T> tipo) {
        if (json == null || json.isBlank()) {
            throw new AvaliacaoInvalidaException("Corpo da requisição é obrigatório.");
        }
        try {
            return objectMapper.readValue(json, tipo);
        } catch (JsonProcessingException e) {
            throw new AvaliacaoInvalidaException("JSON inválido: " + e.getOriginalMessage());
        }
    }

    public String toJson(Object objeto) {
        try {
            return objectMapper.writeValueAsString(objeto);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar JSON.", e);
        }
    }

}
