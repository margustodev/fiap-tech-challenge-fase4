package br.com.fiap.techchallenge.fase4.infrastructure.security;

/**
 * Helper para tratamento de privacidade em logs e payloads.
 */
public class DataPrivacyHelper {

    private static final String MASCARA = "***";

    public String mascararDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return MASCARA;
        }

        String texto = descricao.trim();
        if (texto.length() <= 4) {
            return MASCARA;
        }
        if (texto.length() <= 8) {
            return texto.substring(0, 2) + MASCARA;
        }

        return texto.substring(0, 4)
                + MASCARA
                + texto.substring(texto.length() - 2);
    }
}
