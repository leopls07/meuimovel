package com.api.meuimovel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulacaoResponseDTO {

    private Double entrada;
    private Double percentualEntrada;
    private Double valorFinanciado;
    private Double taxaJurosAnual;
    private Double taxaJurosMensal;
    private Integer prazoMeses;
    private Double parcelaMensalPrice;
    private Double amortizacaoExtraMes;
    private Double pagamentoTotalMes;
    private Integer nParcelasEfetivas;
    private Double tempoPagamentoAnos;
    private Double totalPago;
    private Double totalJuros;
    private Double jurosPctFinanciado;
    private Double custoTotalMensal;
}

