package com.api.meuimovel.service;

import com.api.meuimovel.dto.SimulacaoRequestDTO;
import com.api.meuimovel.dto.SimulacaoResponseDTO;
import com.api.meuimovel.exception.ResourceNotFoundException;
import com.api.meuimovel.model.Imovel;
import com.api.meuimovel.model.SimulacaoFinanciamento;
import com.api.meuimovel.repository.ImovelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SimulacaoServiceImpl implements SimulacaoService {

    private static final double TAXA_JUROS_ANUAL_PADRAO = 0.061;
    private static final int PRAZO_MESES_PADRAO = 420;
    private static final double AMORTIZACAO_EXTRA_PADRAO = 0.0;

    private final ImovelRepository imovelRepository;

    @Override
    public SimulacaoResponseDTO criarOuSubstituir(String imovelId, SimulacaoRequestDTO request) {
        Imovel imovel = buscarImovel(imovelId);
        SimulacaoFinanciamento simulacao = calcular(imovel, request);
        imovel.setSimulacao(simulacao);
        Imovel salvo = imovelRepository.save(imovel);
        return toResponse(salvo.getSimulacao());
    }

    @Override
    public SimulacaoResponseDTO obter(String imovelId) {
        Imovel imovel = buscarImovel(imovelId);
        if (imovel.getSimulacao() == null) {
            throw new ResourceNotFoundException("Simulação não encontrada para o imóvel: " + imovelId);
        }
        return toResponse(imovel.getSimulacao());
    }

    @Override
    public SimulacaoResponseDTO patch(String imovelId, SimulacaoRequestDTO patch) {
        Imovel imovel = buscarImovel(imovelId);

        SimulacaoFinanciamento atual = imovel.getSimulacao();
        // patch pode criar simulação do zero também
        double entrada = firstNonNull(patch.getEntrada(), atual != null ? atual.getEntrada() : null, 0.0);
        double taxaJurosAnual = firstNonNull(patch.getTaxaJurosAnual(), atual != null ? atual.getTaxaJurosAnual() : null, TAXA_JUROS_ANUAL_PADRAO);
        int prazoMeses = firstNonNull(patch.getPrazoMeses(), atual != null ? atual.getPrazoMeses() : null, PRAZO_MESES_PADRAO);
        double amortizacaoExtraMes = firstNonNull(patch.getAmortizacaoExtraMes(), atual != null ? atual.getAmortizacaoExtraMes() : null, AMORTIZACAO_EXTRA_PADRAO);

        SimulacaoRequestDTO merged = SimulacaoRequestDTO.builder()
                .entrada(entrada)
                .taxaJurosAnual(taxaJurosAnual)
                .prazoMeses(prazoMeses)
                .amortizacaoExtraMes(amortizacaoExtraMes)
                .build();

        SimulacaoFinanciamento simulacao = calcular(imovel, merged);
        imovel.setSimulacao(simulacao);
        Imovel salvo = imovelRepository.save(imovel);
        return toResponse(salvo.getSimulacao());
    }

    @Override
    public void deletar(String imovelId) {
        Imovel imovel = buscarImovel(imovelId);
        imovel.setSimulacao(null);
        imovelRepository.save(imovel);
    }

    private Imovel buscarImovel(String id) {
        return imovelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Imóvel não encontrado: " + id));
    }

    private SimulacaoFinanciamento calcular(Imovel imovel, SimulacaoRequestDTO request) {
        if (imovel.getPreco() == null || imovel.getPreco() <= 0) {
            throw new IllegalArgumentException("Imóvel precisa ter preço válido para simular");
        }

        double preco = imovel.getPreco();
        double entrada = Objects.requireNonNullElse(request.getEntrada(), 0.0);
        if (entrada < 0) throw new IllegalArgumentException("entrada deve ser >= 0");
        if (entrada > preco) throw new IllegalArgumentException("entrada não pode ser maior que o preço do imóvel");

        double taxaJurosAnual = Objects.requireNonNullElse(request.getTaxaJurosAnual(), TAXA_JUROS_ANUAL_PADRAO);
        if (taxaJurosAnual <= 0) throw new IllegalArgumentException("taxaJurosAnual deve ser > 0");

        int prazoMeses = Objects.requireNonNullElse(request.getPrazoMeses(), PRAZO_MESES_PADRAO);
        if (prazoMeses <= 0) throw new IllegalArgumentException("prazoMeses deve ser > 0");

        double amortizacaoExtraMes = Objects.requireNonNullElse(request.getAmortizacaoExtraMes(), AMORTIZACAO_EXTRA_PADRAO);
        if (amortizacaoExtraMes < 0) throw new IllegalArgumentException("amortizacaoExtraMes deve ser >= 0");

        double valorFinanciado = preco - entrada;
        if (valorFinanciado <= 0) {
            throw new IllegalArgumentException("valorFinanciado deve ser > 0 (entrada muito alta)");
        }

        double taxaJurosMensal = Math.pow(1 + taxaJurosAnual, 1.0 / 12.0) - 1;
        if (taxaJurosMensal <= 0) {
            throw new IllegalArgumentException("taxaJurosMensal inválida");
        }

        double parcelaMensalPrice = valorFinanciado * taxaJurosMensal /
                (1 - Math.pow(1 + taxaJurosMensal, -prazoMeses));

        double pagamentoTotalMes = parcelaMensalPrice + amortizacaoExtraMes;
        if (pagamentoTotalMes <= valorFinanciado * taxaJurosMensal) {
            throw new IllegalArgumentException("pagamentoTotalMes insuficiente para amortizar o financiamento");
        }

        int nParcelasEfetivas = (int) Math.ceil(
                Math.log(pagamentoTotalMes / (pagamentoTotalMes - valorFinanciado * taxaJurosMensal)) /
                        Math.log(1 + taxaJurosMensal)
        );

        double totalPago = nParcelasEfetivas * pagamentoTotalMes;
        double totalJuros = totalPago - valorFinanciado;
        double jurosPctFinanciado = totalJuros / valorFinanciado;

        double custoFixo = Objects.requireNonNullElse(imovel.getCustoFixoMensal(), 0.0);
        double custoTotalMensal = parcelaMensalPrice + custoFixo;

        return SimulacaoFinanciamento.builder()
                .entrada(entrada)
                .percentualEntrada(entrada / preco)
                .valorFinanciado(valorFinanciado)
                .taxaJurosAnual(taxaJurosAnual)
                .taxaJurosMensal(taxaJurosMensal)
                .prazoMeses(prazoMeses)
                .parcelaMensalPrice(parcelaMensalPrice)
                .amortizacaoExtraMes(amortizacaoExtraMes)
                .pagamentoTotalMes(pagamentoTotalMes)
                .nParcelasEfetivas(nParcelasEfetivas)
                .tempoPagamentoAnos(nParcelasEfetivas / 12.0)
                .totalPago(totalPago)
                .totalJuros(totalJuros)
                .jurosPctFinanciado(jurosPctFinanciado)
                .custoTotalMensal(custoTotalMensal)
                .build();
    }

    private SimulacaoResponseDTO toResponse(SimulacaoFinanciamento s) {
        if (s == null) return null;
        return SimulacaoResponseDTO.builder()
                .entrada(s.getEntrada())
                .percentualEntrada(s.getPercentualEntrada())
                .valorFinanciado(s.getValorFinanciado())
                .taxaJurosAnual(s.getTaxaJurosAnual())
                .taxaJurosMensal(s.getTaxaJurosMensal())
                .prazoMeses(s.getPrazoMeses())
                .parcelaMensalPrice(s.getParcelaMensalPrice())
                .amortizacaoExtraMes(s.getAmortizacaoExtraMes())
                .pagamentoTotalMes(s.getPagamentoTotalMes())
                .nParcelasEfetivas(s.getNParcelasEfetivas())
                .tempoPagamentoAnos(s.getTempoPagamentoAnos())
                .totalPago(s.getTotalPago())
                .totalJuros(s.getTotalJuros())
                .jurosPctFinanciado(s.getJurosPctFinanciado())
                .custoTotalMensal(s.getCustoTotalMensal())
                .build();
    }

    private static <T> T firstNonNull(T a, T b, T c) {
        return a != null ? a : (b != null ? b : c);
    }
}

