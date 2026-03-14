package com.api.meuimovel;

import com.api.meuimovel.security.GoogleTokenVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class MeuimovelApplicationTests {

    // GoogleTokenVerifier faz uma chamada HTTP ao JWKS do Google na inicialização.
    // MockitoBean substitui o bean real por um mock durante os testes de contexto.
    @MockitoBean
    GoogleTokenVerifier googleTokenVerifier;

    @Test
    void contextLoads() {
    }
}

