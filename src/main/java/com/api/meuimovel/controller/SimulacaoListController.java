package com.api.meuimovel.controller;

import com.api.meuimovel.dto.SimulacaoResumoDTO;
import com.api.meuimovel.service.SimulacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/simulacoes")
@RequiredArgsConstructor
public class SimulacaoListController {

    private final SimulacaoService simulacaoService;

    @Operation(
            summary = "Listar simulações do usuário",
            description = "Retorna todas as simulações do usuário autenticado com os principais dados do imóvel embutidos"
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public List<SimulacaoResumoDTO> listar() {
        return simulacaoService.listarPorUsuario();
    }
}