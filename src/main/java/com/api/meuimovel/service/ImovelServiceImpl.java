package com.api.meuimovel.service;

import com.api.meuimovel.dto.ImovelFilterDTO;
import com.api.meuimovel.dto.ImovelPatchDTO;
import com.api.meuimovel.dto.ImovelRequestDTO;
import com.api.meuimovel.dto.ImovelResponseDTO;
import com.api.meuimovel.dto.SimulacaoResponseDTO;
import com.api.meuimovel.exception.ResourceNotFoundException;
import com.api.meuimovel.model.Imovel;
import com.api.meuimovel.model.SimulacaoFinanciamento;
import com.api.meuimovel.repository.ImovelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ImovelServiceImpl implements ImovelService {

    private final ImovelRepository repository;
    private final MongoTemplate mongoTemplate;

    @Override
    public ImovelResponseDTO criar(ImovelRequestDTO request) {
        Imovel imovel = Imovel.builder()
                .localizacao(request.getLocalizacao())
                .notaLocalizacao(request.getNotaLocalizacao())
                .metragem(request.getMetragem())
                .quartos(request.getQuartos())
                .vagas(request.getVagas())
                .qtdBanheiros(request.getQtdBanheiros())
                .varanda(request.getVaranda())
                .andar(request.getAndar())
                .areaLazer(request.getAreaLazer())
                .vagaCoberta(request.getVagaCoberta())
                .distanciaMetroKm(request.getDistanciaMetroKm())
                .preco(request.getPreco())
                .iptuMensal(request.getIptuMensal())
                .condominioMensal(request.getCondominioMensal())
                .anoConstrucao(request.getAnoConstrucao())
                .estadoConservacao(request.getEstadoConservacao())
                .aliquotaIptu(request.getAliquotaIptu())
                .observacoes(request.getObservacoes())
                .build();

        calcularCamposImovel(imovel);
        Imovel salvo = repository.save(imovel);
        return toResponse(salvo);
    }

    @Override
    public List<ImovelResponseDTO> listarTodos() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public ImovelResponseDTO buscarPorId(String id) {
        return toResponse(buscarEntidadePorId(id));
    }

    @Override
    public ImovelResponseDTO patch(String id, ImovelPatchDTO patch) {
        Imovel imovel = buscarEntidadePorId(id);

        if (patch.getLocalizacao() != null) imovel.setLocalizacao(patch.getLocalizacao());
        if (patch.getNotaLocalizacao() != null) imovel.setNotaLocalizacao(patch.getNotaLocalizacao());
        if (patch.getMetragem() != null) imovel.setMetragem(patch.getMetragem());
        if (patch.getQuartos() != null) imovel.setQuartos(patch.getQuartos());
        if (patch.getVagas() != null) imovel.setVagas(patch.getVagas());
        if (patch.getQtdBanheiros() != null) imovel.setQtdBanheiros(patch.getQtdBanheiros());
        if (patch.getVaranda() != null) imovel.setVaranda(patch.getVaranda());
        if (patch.getAndar() != null) imovel.setAndar(patch.getAndar());
        if (patch.getAreaLazer() != null) imovel.setAreaLazer(patch.getAreaLazer());
        if (patch.getVagaCoberta() != null) imovel.setVagaCoberta(patch.getVagaCoberta());
        if (patch.getDistanciaMetroKm() != null) imovel.setDistanciaMetroKm(patch.getDistanciaMetroKm());
        if (patch.getPreco() != null) imovel.setPreco(patch.getPreco());
        if (patch.getIptuMensal() != null) imovel.setIptuMensal(patch.getIptuMensal());
        if (patch.getCondominioMensal() != null) imovel.setCondominioMensal(patch.getCondominioMensal());
        if (patch.getAnoConstrucao() != null) imovel.setAnoConstrucao(patch.getAnoConstrucao());
        if (patch.getEstadoConservacao() != null) imovel.setEstadoConservacao(patch.getEstadoConservacao());
        if (patch.getAliquotaIptu() != null) imovel.setAliquotaIptu(patch.getAliquotaIptu());
        if (patch.getObservacoes() != null) imovel.setObservacoes(patch.getObservacoes());

        calcularCamposImovel(imovel);
        Imovel salvo = repository.save(imovel);
        return toResponse(salvo);
    }

    @Override
    public void deletar(String id) {
        Imovel imovel = buscarEntidadePorId(id);
        repository.delete(imovel);
    }

    @Override
    public List<ImovelResponseDTO> buscar(ImovelFilterDTO filter) {
        Query q = new Query();
        List<Criteria> and = new ArrayList<>();

        if (StringUtils.hasText(filter.getLocalizacao())) {
            String escaped = Pattern.quote(filter.getLocalizacao().trim());
            and.add(Criteria.where("localizacao").regex(escaped, "i"));
        }
        if (filter.getPrecoMin() != null) and.add(Criteria.where("preco").gte(filter.getPrecoMin()));
        if (filter.getPrecoMax() != null) and.add(Criteria.where("preco").lte(filter.getPrecoMax()));
        if (filter.getMetMin() != null) and.add(Criteria.where("metragem").gte(filter.getMetMin()));
        if (filter.getQuartos() != null) and.add(Criteria.where("quartos").is(filter.getQuartos()));
        if (filter.getBanheiros() != null) and.add(Criteria.where("qtdBanheiros").is(filter.getBanheiros()));
        if (filter.getVagas() != null) and.add(Criteria.where("vagas").is(filter.getVagas()));
        if (filter.getAreaLazer() != null) and.add(Criteria.where("areaLazer").is(filter.getAreaLazer()));
        if (filter.getVagaCoberta() != null) and.add(Criteria.where("vagaCoberta").is(filter.getVagaCoberta()));
        if (filter.getDistMaxMetro() != null) and.add(Criteria.where("distanciaMetroKm").lte(filter.getDistMaxMetro()));
        if (filter.getNotaMinLoc() != null) and.add(Criteria.where("notaLocalizacao").gte(filter.getNotaMinLoc()));

        if (!and.isEmpty()) {
            q.addCriteria(new Criteria().andOperator(and));
        }

        return mongoTemplate.find(q, Imovel.class).stream().map(this::toResponse).toList();
    }

    private Imovel buscarEntidadePorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Imóvel não encontrado: " + id));
    }

    private void calcularCamposImovel(Imovel imovel) {
        if (imovel.getIptuMensal() == null
                && imovel.getPreco() != null
                && imovel.getAliquotaIptu() != null) {
            imovel.setIptuMensal((imovel.getPreco() * imovel.getAliquotaIptu()) / 12.0);
        }

        if (imovel.getPreco() != null && imovel.getMetragem() != null && imovel.getMetragem() > 0) {
            imovel.setPrecoM2(imovel.getPreco() / imovel.getMetragem());
        } else {
            imovel.setPrecoM2(null);
        }

        Double iptu = Objects.requireNonNullElse(imovel.getIptuMensal(), 0.0);
        Double cond = Objects.requireNonNullElse(imovel.getCondominioMensal(), 0.0);
        imovel.setCustoFixoMensal(iptu + cond);

        // Se houver simulação já calculada, atualiza custoTotalMensal (depende do custo fixo)
        SimulacaoFinanciamento sim = imovel.getSimulacao();
        if (sim != null && sim.getParcelaMensalPrice() != null) {
            sim.setCustoTotalMensal(sim.getParcelaMensalPrice() + imovel.getCustoFixoMensal());
        }
    }

    private Double round2(Double value) {
        if (value == null) return null;
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private Double round3(Double value) {
        if (value == null) return null;
        return BigDecimal.valueOf(value)
                .setScale(3, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private ImovelResponseDTO toResponse(Imovel imovel) {
        return ImovelResponseDTO.builder()
                .id(imovel.getId())
                .localizacao(imovel.getLocalizacao())
                .notaLocalizacao(imovel.getNotaLocalizacao())
                .metragem(round2(imovel.getMetragem()))
                .quartos(imovel.getQuartos())
                .vagas(imovel.getVagas())
                .qtdBanheiros(imovel.getQtdBanheiros())
                .varanda(imovel.getVaranda())
                .andar(imovel.getAndar())
                .areaLazer(imovel.getAreaLazer())
                .vagaCoberta(imovel.getVagaCoberta())
                .distanciaMetroKm(round2(imovel.getDistanciaMetroKm()))
                .preco(round2(imovel.getPreco()))
                .precoM2(round2(imovel.getPrecoM2()))
                .iptuMensal(round2(imovel.getIptuMensal()))
                .condominioMensal(round2(imovel.getCondominioMensal()))
                .custoFixoMensal(round2(imovel.getCustoFixoMensal()))
                .anoConstrucao(imovel.getAnoConstrucao())
                .estadoConservacao(imovel.getEstadoConservacao())
                .aliquotaIptu(round2(imovel.getAliquotaIptu()))
                .observacoes(imovel.getObservacoes())
                .simulacao(toResponse(imovel.getSimulacao()))
                .build();
    }

    private SimulacaoResponseDTO toResponse(SimulacaoFinanciamento simulacao) {
        if (simulacao == null) return null;
        return SimulacaoResponseDTO.builder()
                .entrada(round2(simulacao.getEntrada()))
                .percentualEntrada(round2(simulacao.getPercentualEntrada()))
                .valorFinanciado(round2(simulacao.getValorFinanciado()))
                .taxaJurosAnual(round2(simulacao.getTaxaJurosAnual()))
                .taxaJurosMensal(round3(simulacao.getTaxaJurosMensal()))
                .prazoMeses(simulacao.getPrazoMeses())
                .parcelaMensalPrice(round2(simulacao.getParcelaMensalPrice()))
                .amortizacaoExtraMes(round2(simulacao.getAmortizacaoExtraMes()))
                .pagamentoTotalMes(round2(simulacao.getPagamentoTotalMes()))
                .nParcelasEfetivas(simulacao.getNParcelasEfetivas())
                .tempoPagamentoAnos(round2(simulacao.getTempoPagamentoAnos()))
                .totalPago(round2(simulacao.getTotalPago()))
                .totalJuros(round2(simulacao.getTotalJuros()))
                .jurosPctFinanciado(round2(simulacao.getJurosPctFinanciado()))
                .custoTotalMensal(round2(simulacao.getCustoTotalMensal()))
                .build();
    }
}

