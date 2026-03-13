package com.api.meuimovel.service;

import com.api.meuimovel.dto.ImovelFilterDTO;
import com.api.meuimovel.dto.ImovelPatchDTO;
import com.api.meuimovel.dto.ImovelRequestDTO;
import com.api.meuimovel.dto.ImovelResponseDTO;

import java.util.List;

public interface ImovelService {

    ImovelResponseDTO criar(ImovelRequestDTO request);

    List<ImovelResponseDTO> listarTodos();

    ImovelResponseDTO buscarPorId(String id);

    ImovelResponseDTO patch(String id, ImovelPatchDTO patch);

    void deletar(String id);

    List<ImovelResponseDTO> buscar(ImovelFilterDTO filter);
}

