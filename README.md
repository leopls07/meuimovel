# meuimovel

API REST para cadastro e análise de imóveis, com MongoDB e simulação de financiamento embutida no recurso de imóvel.

> Objetivo deste README: servir como documentação de integração (principalmente para um futuro frontend), descrevendo **todos os endpoints**, contratos (payloads), exemplos de requests/responses e formato de erros.

---

## Sumário

- [Stack](#stack)
- [Como rodar](#como-rodar)
- [Configuração](#configuração)
- [Swagger / OpenAPI](#swagger--openapi)
- [Convenções de API](#convenções-de-api)
- [Formato de erros](#formato-de-erros)
- [Resumo de endpoints](#resumo-de-endpoints)
- [Recurso: Imóveis](#recurso-imóveis)
  - [POST /api/imoveis](#post-apiimoveis)
  - [GET /api/imoveis](#get-apiimoveis)
  - [GET /api/imoveis/{id}](#get-apiimoveisid)
  - [PATCH /api/imoveis/{id}](#patch-apiimoveisid)
  - [DELETE /api/imoveis/{id}](#delete-apiimoveisid)
  - [GET /api/imoveis/buscar](#get-apiimoveisbuscar)
- [Recurso: Simulação (aninhada no Imóvel)](#recurso-simulação-aninhada-no-imóvel)
  - [POST /api/imoveis/{id}/simulacao](#post-apiimoveisidsimulacao)
  - [GET /api/imoveis/{id}/simulacao](#get-apiimoveisidsimulacao)
  - [PATCH /api/imoveis/{id}/simulacao](#patch-apiimoveisidsimulacao)
  - [DELETE /api/imoveis/{id}/simulacao](#delete-apiimoveisidsimulacao)
- [Guia rápido para Frontend](#guia-rápido-para-frontend)

---

## Stack

- Java 21
- Spring Boot 3.4.x
- Spring Web (REST)
- Spring Data MongoDB
- Bean Validation (Jakarta Validation)
- springdoc-openapi (Swagger UI)

---

## Como rodar

Pré-requisitos:

- Java 21
- MongoDB acessível pela URI configurada

Rodando com Maven Wrapper:

```powershell
cd C:\dev\pessoal\meuimovel
$env:MONGO_URI="mongodb://localhost:27017"  # ajuste conforme seu ambiente
.\mvnw.cmd spring-boot:run
```

A API normalmente sobe em:

- `http://localhost:8080`

---

## Configuração

### Variáveis de ambiente

- `MONGO_URI` (obrigatória)
  - Exemplo: `mongodb://localhost:27017`

Config em `src/main/resources/application.yaml`:

- `spring.data.mongodb.uri: ${MONGO_URI}`
- `spring.data.mongodb.database: meuimovel`

---

## Swagger / OpenAPI

Este projeto expõe Swagger UI via springdoc:

- Swagger UI: `http://localhost:8080/swagger-ui.html`

Dica: para gerar um client (ou base de prompt) a partir do contrato, também é possível consumir o OpenAPI JSON (path padrão do springdoc), normalmente:

- `http://localhost:8080/v3/api-docs`

---

## Convenções de API

- **Base path**: todos os endpoints estão sob `/api`.
- **Content-Type**: `application/json`.
- **IDs**: `id` é string (MongoDB ObjectId serializado como string).
- **Números**: valores monetários e métricas numéricas são `Double`.
- **Simulação**:
  - A simulação é um subrecurso de imóvel (`/api/imoveis/{id}/simulacao`).
  - O `ImovelResponseDTO` retorna a simulação no campo `simulacao`.

Campos calculados no imóvel (servidor calcula):

- `precoM2 = preco / metragem` (quando `metragem` informada)
- `custoFixoMensal = iptuMensal + condominioMensal` (quando aplicável)
- `iptuMensal` pode ser calculado por `aliquotaIptu` se informada (regra de negócio no service)

---

## Formato de erros

Erros são retornados como JSON pelo `GlobalExceptionHandler`.

### Exemplo (404 - não encontrado)

```json
{
  "timestamp": "2026-03-13T12:34:56.789-03:00",
  "status": 404,
  "error": "Not Found",
  "message": "Imóvel não encontrado: x",
  "path": "/api/imoveis/x"
}
```

### Exemplo (400 - validação)

```json
{
  "timestamp": "2026-03-13T12:34:56.789-03:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Erro de validação",
  "path": "/api/imoveis",
  "validationErrors": [
    { "field": "localizacao", "message": "localizacao é obrigatória" },
    { "field": "preco", "message": "preco é obrigatório" }
  ]
}
```

### Status codes comuns

- `400` - payload inválido (Bean Validation) ou `IllegalArgumentException`
- `404` - recurso não encontrado (`ResourceNotFoundException`)

---

## Resumo de endpoints

| Recurso | Método | Rota | Descrição |
|---|---:|---|---|
| Imóveis | POST | `/api/imoveis` | Cadastrar imóvel |
| Imóveis | GET | `/api/imoveis` | Listar imóveis |
| Imóveis | GET | `/api/imoveis/{id}` | Buscar imóvel por ID |
| Imóveis | PATCH | `/api/imoveis/{id}` | Atualizar imóvel parcialmente |
| Imóveis | DELETE | `/api/imoveis/{id}` | Remover imóvel |
| Imóveis | GET | `/api/imoveis/buscar` | Buscar imóveis com filtros via query params |
| Simulação | POST | `/api/imoveis/{id}/simulacao` | Criar/substituir simulação |
| Simulação | GET | `/api/imoveis/{id}/simulacao` | Obter simulação atual |
| Simulação | PATCH | `/api/imoveis/{id}/simulacao` | Atualizar simulação parcialmente |
| Simulação | DELETE | `/api/imoveis/{id}/simulacao` | Remover simulação |

---

## Recurso: Imóveis

### Modelos (DTOs)

#### `ImovelRequestDTO` (criação) — body do `POST /api/imoveis`

Campos:

- `localizacao` (string, **obrigatório**, máx 500)
- `preco` (number, **obrigatório**, > 0)
- `notaLocalizacao` (integer, opcional)
- `metragem` (number, opcional, > 0)
- `quartos` (integer, opcional, >= 0)
- `vagas` (integer, opcional, >= 0)
- `qtdBanheiros` (integer, opcional, >= 0)
- `varanda` (boolean, opcional) — indica se o imóvel tem varanda
- `andar` (integer, opcional)
- `areaLazer` (boolean, opcional)
- `vagaCoberta` (boolean, opcional)
- `distanciaMetroKm` (number, opcional, >= 0)
- `iptuMensal` (number, opcional, >= 0)
- `condominioMensal` (number, opcional, >= 0)
- `anoConstrucao` (integer, opcional)
- `estadoConservacao` (integer, opcional)
- `aliquotaIptu` (number, opcional, >= 0) — alíquota anual (ex.: `0.01` = 1% a.a.)
- `observacoes` (string, opcional)

#### `ImovelPatchDTO` (patch) — body do `PATCH /api/imoveis/{id}`

Mesmos campos do `ImovelRequestDTO`, porém **todos opcionais** (nenhum é obrigatório). Restrições:

- `preco` e `metragem`, se enviados, devem ser positivos
- `quartos`, `vagas`, `qtdBanheiros`, `distanciaMetroKm`, `iptuMensal`, `condominioMensal`, `aliquotaIptu` devem ser >= 0
- `localizacao` máx 500

#### `ImovelResponseDTO` (retorno)

Campos relevantes:

- Campos de cadastro (localização, metragem, quartos etc.)
- Calculados:
  - `precoM2`
  - `custoFixoMensal`
- `simulacao` (objeto do tipo [`SimulacaoResponseDTO`](#simulacaoresponsedto-retorno))

---

### POST /api/imoveis

Cadastrar imóvel.

- **Request body**: `ImovelRequestDTO`
- **Responses**:
  - `201` — retorna `ImovelResponseDTO`
  - `400` — erro de validação

Exemplo de request:

```http
POST /api/imoveis HTTP/1.1
Content-Type: application/json

{
  "localizacao": "Rua X, 123 - Bairro Y",
  "preco": 500000,
  "metragem": 50,
  "qtdBanheiros": 1,
  "varanda": true,
  "iptuMensal": 200,
  "condominioMensal": 800,
  "quartos": 2,
  "vagas": 1,
  "observacoes": "Sol da manhã"
}
```

Exemplo de response (201):

```json
{
  "id": "65f0c2...",
  "localizacao": "Rua X, 123 - Bairro Y",
  "notaLocalizacao": null,
  "metragem": 50.0,
  "quartos": 2,
  "vagas": 1,
  "qtdBanheiros": 1,
  "varanda": true,
  "andar": null,
  "areaLazer": null,
  "vagaCoberta": null,
  "distanciaMetroKm": null,
  "preco": 500000.0,
  "precoM2": 10000.0,
  "iptuMensal": 200.0,
  "condominioMensal": 800.0,
  "custoFixoMensal": 1000.0,
  "anoConstrucao": null,
  "estadoConservacao": null,
  "aliquotaIptu": null,
  "observacoes": "Sol da manhã",
  "simulacao": null
}
```

---

### GET /api/imoveis

Lista todos os imóveis.

- **Responses**:
  - `200` — array de `ImovelResponseDTO`

Exemplo de response:

```json
[
  {
    "id": "65f0c2...",
    "localizacao": "Rua X",
    "preco": 500000.0,
    "precoM2": 10000.0,
    "custoFixoMensal": 1000.0,
    "simulacao": null
  }
]
```

---

### GET /api/imoveis/{id}

Busca imóvel por ID.

- **Path params**:
  - `id` (string)

- **Responses**:
  - `200` — `ImovelResponseDTO`
  - `404` — imóvel não encontrado

---

### PATCH /api/imoveis/{id}

Atualiza parcialmente um imóvel (somente os campos enviados). O backend recalcula campos derivados (ex.: `precoM2`).

- **Path params**:
  - `id` (string)
- **Request body**: `ImovelPatchDTO`
- **Responses**:
  - `200` — `ImovelResponseDTO`
  - `400` — erro de validação
  - `404` — imóvel não encontrado

Exemplo (atualizar somente localização e quartos):

```http
PATCH /api/imoveis/65f0c2... HTTP/1.1
Content-Type: application/json

{
  "localizacao": "Nova localização",
  "quartos": 3,
  "qtdBanheiros": 2,
  "varanda": false
}
```

---

### DELETE /api/imoveis/{id}

Remove um imóvel.

- **Responses**:
  - `204` — removido
  - `404` — imóvel não encontrado

---

### GET /api/imoveis/buscar

Busca/filtro dinâmico via query params (combinação AND). Os parâmetros são opcionais.

**Query params** (a partir do `ImovelFilterDTO`):

- `localizacao` (string) — texto para busca por localização
- `precoMin` (number)
- `precoMax` (number)
- `metMin` (number) — metragem mínima
- `quartos` (integer)
- `vagas` (integer)
- `areaLazer` (boolean)
- `vagaCoberta` (boolean)
- `distMaxMetro` (number) — distância máxima do metrô (km)
- `notaMinLoc` (integer) — nota mínima de localização

Exemplo:

`GET /api/imoveis/buscar?precoMax=600000&metMin=45&quartos=2&distMaxMetro=1.5`

- **Responses**:
  - `200` — lista de `ImovelResponseDTO`

---

## Recurso: Simulação (aninhada no Imóvel)

A simulação é armazenada/gerenciada por imóvel. O frontend pode tratar como:

- Aba/Seção “Simulação” dentro da tela de detalhe do imóvel, ou
- Wizard/modal que salva no subrecurso.

### Modelos (DTOs)

#### `SimulacaoRequestDTO` (entrada)

Campos (todos opcionais no contexto de PATCH; para criação depende da regra do service):

- `entrada` (number, >= 0)
- `taxaJurosAnual` (number, > 0) — ex.: `0.12` = 12% a.a.
- `prazoMeses` (integer, > 0)
- `amortizacaoExtraMes` (number, >= 0)

#### `SimulacaoResponseDTO` (retorno)

Campos:

- Inputs ecoados:
  - `entrada`, `taxaJurosAnual`, `prazoMeses`, `amortizacaoExtraMes`
- Calculados:
  - `percentualEntrada`
  - `valorFinanciado`
  - `taxaJurosMensal`
  - `parcelaMensalPrice`
  - `pagamentoTotalMes`
  - `nParcelasEfetivas`
  - `tempoPagamentoAnos`
  - `totalPago`
  - `totalJuros`
  - `jurosPctFinanciado`
  - `custoTotalMensal`

---

### POST /api/imoveis/{id}/simulacao

Cria ou substitui a simulação de financiamento do imóvel.

- **Path params**:
  - `id` (string) — id do imóvel
- **Request body**: `SimulacaoRequestDTO`
- **Responses**:
  - `201` — `SimulacaoResponseDTO`
  - `400` — parâmetros inválidos
  - `404` — imóvel não encontrado

Exemplo de request:

```http
POST /api/imoveis/65f0c2.../simulacao HTTP/1.1
Content-Type: application/json

{
  "entrada": 100000,
  "taxaJurosAnual": 0.12,
  "prazoMeses": 360,
  "amortizacaoExtraMes": 0
}
```

---

### GET /api/imoveis/{id}/simulacao

Retorna a simulação atual do imóvel.

- **Responses**:
  - `200` — `SimulacaoResponseDTO`
  - `404` — imóvel/simulação não encontrada

---

### PATCH /api/imoveis/{id}/simulacao

Atualiza parcialmente os inputs da simulação e recalcula todos os campos derivados.

- **Responses**:
  - `200` — `SimulacaoResponseDTO`
  - `400` — parâmetros inválidos
  - `404` — imóvel não encontrado

---

### DELETE /api/imoveis/{id}/simulacao

Remove a simulação do imóvel.

- **Responses**:
  - `204` — removida
  - `404` — imóvel não encontrado

---

## Guia rápido para Frontend

### Telas/fluxos sugeridos

1. **Lista de imóveis**
   - Carregar: `GET /api/imoveis`
   - Ações: abrir detalhe, remover (`DELETE /api/imoveis/{id}`)

2. **Busca com filtros**
   - Usar: `GET /api/imoveis/buscar` com query params
   - UI: filtros por preço, metragem, quartos, vagas, distância até metrô, etc.

3. **Cadastro de imóvel**
   - Enviar: `POST /api/imoveis`
   - Validar no client: `localizacao` obrigatório, `preco` > 0 (para reduzir roundtrips)

4. **Detalhe do imóvel**
   - Carregar: `GET /api/imoveis/{id}`
   - Editar rápido: `PATCH /api/imoveis/{id}`
   - Mostrar campos calculados: `precoM2`, `custoFixoMensal`

5. **Simulação de financiamento (no detalhe do imóvel)**
   - Criar/substituir: `POST /api/imoveis/{id}/simulacao`
   - Atualização incremental: `PATCH /api/imoveis/{id}/simulacao`
   - Exibir resultados: campos calculados de `SimulacaoResponseDTO`

### Tratamento de erros

- `400` com `validationErrors`: mapear `field` → erro por campo (ex.: formulário)
- `404`: exibir “não encontrado” e redirecionar para lista

---

## Prompt seed (para gerar o frontend)

Se você for usar este README como prompt para gerar um frontend, copie também estas regras:

- Base URL: `http://localhost:8080`
- Rotas do frontend:
  - `/imoveis` (lista + filtros)
  - `/imoveis/novo` (cadastro)
  - `/imoveis/:id` (detalhe + edição + simulação)
- Formatar moeda em BRL (R$) e percentuais (taxas) como `%`.
- Exibir `precoM2`, `custoFixoMensal` e `custoTotalMensal` com destaque.
- Ao salvar (POST/PATCH), atualizar cache/estado local com o response do backend.

