package com.api.meuimovel.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImovelPatchDTO {

    @Size(max = 500, message = "localizacao deve ter no máximo 500 caracteres")
    private String localizacao;

    @Positive(message = "preco deve ser positivo")
    private Double preco;

    private Integer notaLocalizacao;

    @Positive(message = "metragem deve ser positiva")
    private Double metragem;

    @PositiveOrZero(message = "quartos deve ser >= 0")
    private Integer quartos;

    @PositiveOrZero(message = "vagas deve ser >= 0")
    private Integer vagas;

    @PositiveOrZero(message = "qtdBanheiros deve ser >= 0")
    private Integer qtdBanheiros;

    private Boolean varanda;

    private Integer andar;
    private Boolean areaLazer;
    private Boolean vagaCoberta;

    @PositiveOrZero(message = "distanciaMetroKm deve ser >= 0")
    private Double distanciaMetroKm;

    @PositiveOrZero(message = "iptuMensal deve ser >= 0")
    private Double iptuMensal;

    @PositiveOrZero(message = "condominioMensal deve ser >= 0")
    private Double condominioMensal;

    private Integer anoConstrucao;
    private Integer estadoConservacao;

    @PositiveOrZero(message = "aliquotaIptu deve ser >= 0")
    private Double aliquotaIptu;

    private String observacoes;
}

