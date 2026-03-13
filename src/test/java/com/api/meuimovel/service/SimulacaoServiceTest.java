package com.api.meuimovel.service;

import com.api.meuimovel.dto.SimulacaoRequestDTO;
import com.api.meuimovel.dto.SimulacaoResponseDTO;
import com.api.meuimovel.model.Imovel;
import com.api.meuimovel.repository.ImovelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulacaoServiceTest {

    @Mock
    ImovelRepository repository;

    @InjectMocks
    SimulacaoServiceImpl service;

    @BeforeEach
    void setup() {
        when(repository.save(any(Imovel.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void calcularSimulacao_deveRetornarParcelaPriceCorreta() {
        Imovel imovel = Imovel.builder().id("1").preco(500000.0).custoFixoMensal(0.0).build();
        when(repository.findById("1")).thenReturn(Optional.of(imovel));

        SimulacaoRequestDTO req = SimulacaoRequestDTO.builder()
                .entrada(100000.0)
                .taxaJurosAnual(0.061)
                .prazoMeses(420)
                .amortizacaoExtraMes(0.0)
                .build();

        SimulacaoResponseDTO resp = service.criarOuSubstituir("1", req);

        double taxaMensal = Math.pow(1 + 0.061, 1.0 / 12.0) - 1;
        double valorFinanciado = 400000.0;
        double esperado = valorFinanciado * taxaMensal / (1 - Math.pow(1 + taxaMensal, -420));

        assertThat(resp.getParcelaMensalPrice()).isCloseTo(esperado, within(1e-6));
    }

    @Test
    void calcularSimulacao_deveCalcularNParcelasEfetivasComAmortizacaoExtra() {
        Imovel imovel = Imovel.builder().id("1").preco(300000.0).custoFixoMensal(0.0).build();
        when(repository.findById("1")).thenReturn(Optional.of(imovel));

        SimulacaoRequestDTO req = SimulacaoRequestDTO.builder()
                .entrada(0.0)
                .taxaJurosAnual(0.061)
                .prazoMeses(420)
                .amortizacaoExtraMes(500.0)
                .build();

        SimulacaoResponseDTO resp = service.criarOuSubstituir("1", req);

        assertThat(resp.getNParcelasEfetivas()).isNotNull();
        assertThat(resp.getNParcelasEfetivas()).isLessThan(420);
    }

    @Test
    void calcularSimulacao_deveRetornarTotalJurosCorreto() {
        Imovel imovel = Imovel.builder().id("1").preco(500000.0).custoFixoMensal(0.0).build();
        when(repository.findById("1")).thenReturn(Optional.of(imovel));

        SimulacaoRequestDTO req = SimulacaoRequestDTO.builder()
                .entrada(100000.0)
                .taxaJurosAnual(0.061)
                .prazoMeses(420)
                .amortizacaoExtraMes(200.0)
                .build();

        SimulacaoResponseDTO resp = service.criarOuSubstituir("1", req);

        double valorFinanciado = 400000.0;
        double totalPago = resp.getNParcelasEfetivas() * resp.getPagamentoTotalMes();
        double esperado = totalPago - valorFinanciado;

        assertThat(resp.getTotalJuros()).isCloseTo(esperado, within(1e-6));
    }
}

