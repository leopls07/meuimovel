package com.api.meuimovel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulacaoResumoDTO {

    // Dados do imóvel
    private String imovelId;
    private String localizacao;
    private Double preco;
    private Double precoM2;
    private Double custoFixoMensal;

    // Dados principais da simulação
    private Double entrada;
    private Double percentualEntrada;
    private Double valorFinanciado;
    private Double parcelaMensalPrice;
    private Double pagamentoTotalMes;
    private Double totalJuros;
    private Double totalPago;
    private Double taxaJurosAnual;
    private Integer prazoMeses;
    private Integer nParcelasEfetivas;
    private Double tempoPagamentoAnos;
    private Double custoTotalMensal;
}