package com.api.meuimovel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImovelFilterDTO {

    private String localizacao;
    private Double precoMin;
    private Double precoMax;
    private Double metMin;
    private Integer quartos;
    private Integer vagas;
    private Boolean areaLazer;
    private Boolean vagaCoberta;
    private Double distMaxMetro;
    private Integer notaMinLoc;
}

