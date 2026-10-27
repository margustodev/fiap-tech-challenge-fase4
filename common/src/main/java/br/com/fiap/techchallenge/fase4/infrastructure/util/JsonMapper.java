package br.com.fiap.techchallenge.fase4.infrastructure.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utilitário compartilhado de serialização/deserialização JSON.
 */
public class JsonMapper {
    private final ObjectMapper mapper;

    public JsonMapper() {
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        false
                )
                .configure(
                        DeserializationFeature.ACCEPT_FLOAT_AS_INT,
                        false
                )
                .configure(
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                        false
                );
    }
    public <T> T fromJson(String json, Class<T> tipo) {
        // TODO: deserializar JSON
        try {
            return mapper.readValue(json, tipo);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON inválido.", e);
        }
    }

    public String toJson(Object objeto) {
        // TODO: serializar objeto para JSON
        try {
            return mapper.writeValueAsString(objeto);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Não foi possível gerar o JSON.",
                    e
            );
        }
    }

}
