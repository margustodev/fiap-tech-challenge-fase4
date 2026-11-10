package br.com.fiap.techchallenge.fase4.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataPrivacyHelperTest {

    private final DataPrivacyHelper helper = new DataPrivacyHelper();
    private final SecurityConfig securityConfig = new SecurityConfig(helper);

    @Test
    void deveMascararDescricaoLonga() {
        String mascarado = helper.mascararDescricao("Curso com material desatualizado");
        assertEquals("Curs***do", mascarado);
        assertTrue(securityConfig.isDadoSensivelMascarado(mascarado));
    }

    @Test
    void deveMascararDescricaoCurta() {
        assertEquals("***", helper.mascararDescricao("abc"));
        assertEquals("ab***", helper.mascararDescricao("abcdef"));
    }

    @Test
    void deveMascararQuandoDescricaoNulaOuVazia() {
        assertEquals("***", helper.mascararDescricao(null));
        assertEquals("***", helper.mascararDescricao("   "));
    }
}
