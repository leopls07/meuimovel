package com.api.meuimovel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "imovel")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Imovel {

    @Id
    private String id;

    /** ID do usuário dono deste imóvel */
    @Indexed
    private String userId;

    private String localizacao;
    private Integer notaLocalizacao;
    private Double metragem;
    private Integer quartos;
    private Integer vagas;
    private Integer qtdBanheiros;
    private Boolean varanda;
    private Integer andar;
    private Boolean areaLazer;
    private Boolean vagaCoberta;
    private Double distanciaMetroKm;
    private Double preco;
    private Double precoM2;
    private Double iptuMensal;
    private Double condominioMensal;
    private Double custoFixoMensal;
    private Integer anoConstrucao;
    private Integer estadoConservacao;
    private Double aliquotaIptu;
    private String observacoes;

    private SimulacaoFinanciamento simulacao;
}

