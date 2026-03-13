package com.api.meuimovel.controller;

import com.api.meuimovel.dto.SimulacaoRequestDTO;
import com.api.meuimovel.dto.SimulacaoResponseDTO;
import com.api.meuimovel.service.SimulacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/imoveis/{id}/simulacao")
@RequiredArgsConstructor
public class SimulacaoController {

    private final SimulacaoService simulacaoService;

    @Operation(summary = "Criar/substituir simulação", description = "Cria ou substitui a simulação de financiamento embutida no imóvel")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Simulação criada/substituída"),
            @ApiResponse(responseCode = "404", description = "Imóvel não encontrado"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    @PostMapping
    public ResponseEntity<SimulacaoResponseDTO> criar(@PathVariable("id") String imovelId,
                                                     @Valid @RequestBody SimulacaoRequestDTO request) {
        SimulacaoResponseDTO resp = simulacaoService.criarOuSubstituir(imovelId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @Operation(summary = "Obter simulação atual")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Simulação retornada"),
            @ApiResponse(responseCode = "404", description = "Imóvel/simulação não encontrada")
    })
    @GetMapping
    public SimulacaoResponseDTO obter(@PathVariable("id") String imovelId) {
        return simulacaoService.obter(imovelId);
    }

    @Operation(summary = "Atualizar simulação parcialmente", description = "Atualiza inputs da simulação e recalcula todos os campos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Simulação atualizada"),
            @ApiResponse(responseCode = "404", description = "Imóvel não encontrado"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    @PatchMapping
    public SimulacaoResponseDTO patch(@PathVariable("id") String imovelId,
                                     @Valid @RequestBody SimulacaoRequestDTO patch) {
        return simulacaoService.patch(imovelId, patch);
    }

    @Operation(summary = "Remover simulação")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Simulação removida"),
            @ApiResponse(responseCode = "404", description = "Imóvel não encontrado")
    })
    @DeleteMapping
    public ResponseEntity<Void> deletar(@PathVariable("id") String imovelId) {
        simulacaoService.deletar(imovelId);
        return ResponseEntity.noContent().build();
    }
}

