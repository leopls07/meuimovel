package com.api.meuimovel.service;

import com.api.meuimovel.dto.SimulacaoRequestDTO;
import com.api.meuimovel.dto.SimulacaoResponseDTO;
import com.api.meuimovel.dto.SimulacaoResumoDTO;

import java.util.List;

public interface SimulacaoService {

    SimulacaoResponseDTO criarOuSubstituir(String imovelId, SimulacaoRequestDTO request);

    SimulacaoResponseDTO obter(String imovelId);

    SimulacaoResponseDTO patch(String imovelId, SimulacaoRequestDTO patch);

    void deletar(String imovelId);

    List<SimulacaoResumoDTO> listarPorUsuario();
}

