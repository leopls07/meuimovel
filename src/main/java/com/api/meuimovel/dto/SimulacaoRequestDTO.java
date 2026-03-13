package com.api.meuimovel.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimulacaoRequestDTO {

    @PositiveOrZero(message = "entrada deve ser >= 0")
    private Double entrada;

    @Positive(message = "taxaJurosAnual deve ser positiva")
    private Double taxaJurosAnual;

    @Positive(message = "prazoMeses deve ser positivo")
    private Integer prazoMeses;

    @PositiveOrZero(message = "amortizacaoExtraMes deve ser >= 0")
    private Double amortizacaoExtraMes;
}

