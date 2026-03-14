package com.api.meuimovel.service;

import com.api.meuimovel.dto.ImovelPatchDTO;
import com.api.meuimovel.dto.ImovelRequestDTO;
import com.api.meuimovel.exception.ResourceNotFoundException;
import com.api.meuimovel.model.Imovel;
import com.api.meuimovel.repository.ImovelRepository;
import com.api.meuimovel.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImovelServiceTest {

    static final String USER_ID = "user-test-123";

    @Mock
    ImovelRepository repository;

    @Mock
    MongoTemplate mongoTemplate;

    @InjectMocks
    ImovelServiceImpl service;

    @Captor
    ArgumentCaptor<Imovel> imovelCaptor;

    // SecurityUtils é uma classe estática — usamos mockStatic para controlar
    // o retorno de currentUserId() sem precisar de SecurityContext real
    MockedStatic<SecurityUtils> securityUtils;

    @BeforeEach
    void setup() {
        securityUtils = mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::currentUserId).thenReturn(USER_ID);
    }

    @AfterEach
    void teardown() {
        securityUtils.close();
    }

    @Test
    void salvar_deveCalcularPrecoM2EcustoFixo() {
        ImovelRequestDTO req = ImovelRequestDTO.builder()
                .localizacao("Rua X")
                .preco(500000.0)
                .metragem(50.0)
                .iptuMensal(200.0)
                .condominioMensal(800.0)
                .build();

        when(repository.save(any(Imovel.class))).thenAnswer(inv -> inv.getArgument(0));

        service.criar(req);

        verify(repository).save(imovelCaptor.capture());
        Imovel salvo = imovelCaptor.getValue();

        assertThat(salvo.getUserId()).isEqualTo(USER_ID);
        assertThat(salvo.getPrecoM2()).isCloseTo(10000.0, within(0.0001));
        assertThat(salvo.getCustoFixoMensal()).isCloseTo(1000.0, within(0.0001));
    }

    @Test
    void salvar_deveCalcularIptuQuandoAliquotaInformada() {
        when(repository.save(any(Imovel.class))).thenAnswer(inv -> inv.getArgument(0));
        ImovelRequestDTO req = ImovelRequestDTO.builder()
                .localizacao("Rua X")
                .preco(120000.0)
                .aliquotaIptu(0.01)
                .build();

        service.criar(req);

        verify(repository).save(imovelCaptor.capture());
        Imovel salvo = imovelCaptor.getValue();
        assertThat(salvo.getIptuMensal()).isCloseTo((120000.0 * 0.01) / 12.0, within(0.0000001));
    }

    @Test
    void buscarPorId_deveLancarExcecaoQuandoNaoEncontrado() {
        when(repository.findByIdAndUserId("x", USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId("x"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Imóvel não encontrado: x");
    }

    @Test
    void patch_deveAtualizarApenasOsCamposEnviados() {
        when(repository.save(any(Imovel.class))).thenAnswer(inv -> inv.getArgument(0));
        Imovel existente = Imovel.builder()
                .id("1")
                .userId(USER_ID)
                .localizacao("Antiga")
                .preco(300000.0)
                .metragem(30.0)
                .quartos(1)
                .condominioMensal(500.0)
                .build();

        when(repository.findByIdAndUserId("1", USER_ID)).thenReturn(Optional.of(existente));

        ImovelPatchDTO patch = ImovelPatchDTO.builder()
                .localizacao("Nova")
                .quartos(2)
                .qtdBanheiros(2)
                .varanda(true)
                .build();

        service.patch("1", patch);

        verify(repository).save(imovelCaptor.capture());
        Imovel salvo = imovelCaptor.getValue();

        assertThat(salvo.getLocalizacao()).isEqualTo("Nova");
        assertThat(salvo.getQuartos()).isEqualTo(2);
        assertThat(salvo.getQtdBanheiros()).isEqualTo(2);
        assertThat(salvo.getVaranda()).isTrue();
        assertThat(salvo.getPreco()).isEqualTo(300000.0);
        assertThat(salvo.getMetragem()).isEqualTo(30.0);
        // recalcula
        assertThat(salvo.getPrecoM2()).isCloseTo(10000.0, within(0.0001));
    }
}

