package com.api.meuimovel.controller;

import com.api.meuimovel.dto.ImovelFilterDTO;
import com.api.meuimovel.dto.ImovelPatchDTO;
import com.api.meuimovel.dto.ImovelRequestDTO;
import com.api.meuimovel.dto.ImovelResponseDTO;
import com.api.meuimovel.service.ImovelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/imoveis")
@RequiredArgsConstructor
public class ImovelController {

    private final ImovelService imovelService;

    @Operation(summary = "Cadastrar imóvel", description = "Cadastra um imóvel. Campos obrigatórios: localizacao e preco.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Imóvel cadastrado"),
            @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content(schema = @Schema(implementation = Object.class)))
    })
    @PostMapping
    public ResponseEntity<ImovelResponseDTO> criar(@Valid @RequestBody ImovelRequestDTO request) {
        ImovelResponseDTO resp = imovelService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @Operation(summary = "Listar imóveis", description = "Lista todos os imóveis cadastrados")
    @GetMapping
    public List<ImovelResponseDTO> listar() {
        return imovelService.listarTodos();
    }

    @Operation(summary = "Buscar imóvel por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imóvel encontrado"),
            @ApiResponse(responseCode = "404", description = "Imóvel não encontrado")
    })
    @GetMapping("/{id}")
    public ImovelResponseDTO buscarPorId(@PathVariable String id) {
        return imovelService.buscarPorId(id);
    }

    @Operation(summary = "Atualizar imóvel parcialmente", description = "Atualiza apenas os campos enviados no payload. Nenhum campo é obrigatório.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imóvel atualizado"),
            @ApiResponse(responseCode = "404", description = "Imóvel não encontrado")
    })
    @PatchMapping("/{id}")
    public ImovelResponseDTO patch(@PathVariable String id, @Valid @RequestBody ImovelPatchDTO patch) {
        return imovelService.patch(id, patch);
    }

    @Operation(summary = "Remover imóvel")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Imóvel removido"),
            @ApiResponse(responseCode = "404", description = "Imóvel não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        imovelService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar imóveis com filtros", description = "Busca/filtro dinâmico via query params (combinação AND)")
    @GetMapping("/buscar")
    public List<ImovelResponseDTO> buscar(@ParameterObject ImovelFilterDTO filter) {
        return imovelService.buscar(filter);
    }
}

