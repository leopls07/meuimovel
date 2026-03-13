package com.api.meuimovel.service;

import com.api.meuimovel.dto.ImovelPatchDTO;
import com.api.meuimovel.dto.ImovelRequestDTO;
import com.api.meuimovel.exception.ResourceNotFoundException;
import com.api.meuimovel.model.Imovel;
import com.api.meuimovel.repository.ImovelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImovelServiceTest {

    @Mock
    ImovelRepository repository;

    @Mock
    MongoTemplate mongoTemplate;

    @InjectMocks
    ImovelServiceImpl service;

    @Captor
    ArgumentCaptor<Imovel> imovelCaptor;

    @BeforeEach
    void setup() {

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
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId("x"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Imóvel não encontrado: x");
    }

    @Test
    void patch_deveAtualizarApenasOsCamposEnviados() {
        when(repository.save(any(Imovel.class))).thenAnswer(inv -> inv.getArgument(0));
        Imovel existente = Imovel.builder()
                .id("1")
                .localizacao("Antiga")
                .preco(300000.0)
                .metragem(30.0)
                .quartos(1)
                .condominioMensal(500.0)
                .build();

        when(repository.findById("1")).thenReturn(Optional.of(existente));

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

