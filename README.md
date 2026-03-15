# meuimovel-api

API REST para cadastro, busca e análise de imóveis, com simulação de financiamento embutida e autenticação via Google OAuth2.

---

## Sumário

- [Stack](#stack)
- [Arquitetura](#arquitetura)
- [Como rodar](#como-rodar)
- [Docker](#docker)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Autenticação](#autenticação)
- [Swagger / OpenAPI](#swagger--openapi)
- [Endpoints](#endpoints)
  - [Auth](#auth)
  - [Imóveis](#imóveis)
  - [Simulação de Financiamento](#simulação-de-financiamento)
- [Modelos e DTOs](#modelos-e-dtos)
- [Filtros de busca](#filtros-de-busca)
- [Campos calculados](#campos-calculados)
- [Formato de erros](#formato-de-erros)
- [Testes](#testes)

---

## Stack

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 3.4.x |
| Spring Data MongoDB | — |
| Spring Security + JWT (JJWT) | 0.12.6 |
| OAuth2 Resource Server | — |
| springdoc-openapi (Swagger UI) | 2.8.5 |
| Lombok | — |
| Bean Validation (Jakarta) | — |

**Banco de dados:** MongoDB (Atlas ou local)

---

## Arquitetura

```
controller/
  AuthController          → POST /api/auth/google
  ImovelController        → CRUD + busca filtrada de imóveis
  SimulacaoController     → CRUD de simulação embutida no imóvel
  SimulacaoListController → listagem de simulações por usuário

service/
  ImovelServiceImpl       → regras de negócio de imóveis
  SimulacaoServiceImpl    → cálculo de financiamento (Price/SAC + amortização extra)
  UserServiceImpl         → gerenciamento de usuários

security/
  JwtService              → geração e validação de JWT próprio
  JwtAuthFilter           → filtro de autenticação por token
  GoogleTokenVerifier     → validação do ID Token do Google via JWKS

config/
  SecurityConfig          → proteção de rotas (produção)
  LocalSecurityConfig     → sem autenticação (perfil local)
  CorsConfig              → CORS liberado para qualquer origem
```

Cada imóvel pertence a um usuário (`userId`). Operações de escrita e leitura são sempre escopadas ao usuário autenticado.

---

## Como rodar

**Pré-requisitos:**
- Java 21
- MongoDB acessível

**Com Maven Wrapper (Linux/macOS):**

```bash
export MONGO_URI="mongodb://localhost:27017"
export JWT_SECRET="sua-chave-secreta-aqui"
export GOOGLE_CLIENT_ID="seu-client-id.apps.googleusercontent.com"

./mvnw spring-boot:run
```

**Com Maven Wrapper (Windows PowerShell):**

```powershell
$env:MONGO_URI="mongodb://localhost:27017"
$env:JWT_SECRET="sua-chave-secreta-aqui"
$env:GOOGLE_CLIENT_ID="seu-client-id.apps.googleusercontent.com"

.\mvnw.cmd spring-boot:run
```

**Perfil local (sem autenticação):**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

> No perfil `local`, o `SecurityConfig` é desativado e todas as rotas ficam abertas — útil para desenvolvimento sem Google OAuth.

A API sobe em: `http://localhost:8080`

---

## Docker

O projeto inclui um `Dockerfile` multi-stage:

```bash
# Build
docker build -t meuimovel-api .

# Run
docker run -p 8080:8080 \
  -e MONGO_URI="mongodb+srv://..." \
  -e JWT_SECRET="..." \
  -e GOOGLE_CLIENT_ID="..." \
  meuimovel-api
```

**Detalhes do Dockerfile:**
- Stage 1 (`build`): `maven:3.9-eclipse-temurin-21` — compila o JAR sem rodar testes.
- Stage 2 (`runtime`): `eclipse-temurin:21-jre-jammy` — imagem Debian slim (evita incompatibilidades de TLS do Alpine com MongoDB Atlas).
- Roda com usuário não-root (`spring`).

---

## Variáveis de ambiente

| Variável | Obrigatória | Padrão | Descrição |
|---|---|---|---|
| `MONGO_URI` | ✅ | — | URI do MongoDB (ex.: `mongodb://localhost:27017`) |
| `JWT_SECRET` | ✅ | — | Chave secreta para assinar o JWT da aplicação |
| `GOOGLE_CLIENT_ID` | ✅ | — | Client ID do projeto no Google Cloud Console |
| `JWT_EXPIRATION_MS` | ❌ | `86400000` | Tempo de expiração do JWT em ms (padrão: 24h) |

---

## Autenticação

A API usa autenticação **stateless com JWT próprio**, obtido após login via Google OAuth2.

### Fluxo

```
Frontend                          Backend
   │                                 │
   │  1. Google Sign-In              │
   │  → obtém idToken do Google      │
   │                                 │
   │  2. POST /api/auth/google       │
   │     { "idToken": "..." }  ────► │
   │                                 │  Valida idToken no Google (JWKS)
   │                                 │  Cria/atualiza usuário no MongoDB
   │                                 │  Gera JWT próprio
   │  ◄──── { token, userInfo }      │
   │                                 │
   │  3. Todas as próximas requests  │
   │     Authorization: Bearer <JWT> │
```

### Usando o token

Inclua o header em todas as requisições protegidas:

```http
Authorization: Bearer <seu_jwt>
```

Rotas públicas (sem autenticação necessária):
- `POST /api/auth/google`
- `GET /swagger-ui.html`, `GET /v3/api-docs/**`
- `OPTIONS /**`

---

## Swagger / OpenAPI

Disponível em:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## Endpoints

### Auth

#### `POST /api/auth/google`

Autentica com o ID Token do Google e retorna um JWT da aplicação.

**Request body:**

```json
{
  "idToken": "<Google ID Token obtido no frontend>"
}
```

**Response `200`:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": "66a1b2...",
    "email": "usuario@gmail.com",
    "name": "Nome do Usuário",
    "pictureUrl": "https://lh3.googleusercontent.com/..."
  }
}
```

| Código | Descrição |
|---|---|
| `200` | Autenticado com sucesso |
| `400` | Requisição inválida |
| `401` | Token do Google inválido ou expirado |

---

### Imóveis

Base path: `/api/imoveis`  
Todas as rotas exigem `Authorization: Bearer <token>`.

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/imoveis` | Cadastrar imóvel |
| `GET` | `/api/imoveis` | Listar imóveis do usuário |
| `GET` | `/api/imoveis/{id}` | Buscar imóvel por ID |
| `PATCH` | `/api/imoveis/{id}` | Atualizar imóvel parcialmente |
| `DELETE` | `/api/imoveis/{id}` | Remover imóvel |
| `GET` | `/api/imoveis/buscar` | Buscar imóveis com filtros |

---

#### `POST /api/imoveis`

Campos obrigatórios: `localizacao` e `preco`.

**Request body:**

```json
{
  "localizacao": "Rua X, 123 - Bairro Y",
  "preco": 500000,
  "metragem": 50,
  "quartos": 2,
  "vagas": 1,
  "qtdBanheiros": 1,
  "varanda": true,
  "iptuMensal": 200,
  "condominioMensal": 800,
  "observacoes": "Sol da manhã"
}
```

**Response `201`:**

```json
{
  "id": "65f0c2...",
  "localizacao": "Rua X, 123 - Bairro Y",
  "preco": 500000.0,
  "metragem": 50.0,
  "precoM2": 10000.0,
  "quartos": 2,
  "vagas": 1,
  "qtdBanheiros": 1,
  "varanda": true,
  "iptuMensal": 200.0,
  "condominioMensal": 800.0,
  "custoFixoMensal": 1000.0,
  "simulacao": null
}
```

---

#### `GET /api/imoveis`

Retorna todos os imóveis do usuário autenticado.

**Response `200`:** array de `ImovelResponseDTO`.

---

#### `GET /api/imoveis/{id}`

**Response `200`:** `ImovelResponseDTO` | **`404`:** imóvel não encontrado.

---

#### `PATCH /api/imoveis/{id}`

Atualiza apenas os campos enviados. O backend recalcula campos derivados (`precoM2`, `custoFixoMensal`).

```json
{
  "localizacao": "Nova localização",
  "quartos": 3
}
```

**Response `200`:** `ImovelResponseDTO` atualizado.

---

#### `DELETE /api/imoveis/{id}`

**Response `204`:** removido | **`404`:** não encontrado.

---

#### `GET /api/imoveis/buscar`

Filtro dinâmico via query params (combinação AND). Todos os parâmetros são opcionais.

```
GET /api/imoveis/buscar?precoMax=600000&metMin=45&quartos=2&distMaxMetro=1.5
```

Veja todos os parâmetros disponíveis em [Filtros de busca](#filtros-de-busca).

---

### Simulação de Financiamento

Base path: `/api/imoveis/{id}/simulacao`  
A simulação é armazenada como subdocumento do imóvel.

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/imoveis/{id}/simulacao` | Criar ou substituir simulação |
| `GET` | `/api/imoveis/{id}/simulacao` | Obter simulação atual |
| `PATCH` | `/api/imoveis/{id}/simulacao` | Atualizar simulação parcialmente (recalcula tudo) |
| `DELETE` | `/api/imoveis/{id}/simulacao` | Remover simulação |

**Request body (`SimulacaoRequestDTO`):**

```json
{
  "entrada": 100000,
  "taxaJurosAnual": 0.12,
  "prazoMeses": 360,
  "amortizacaoExtraMes": 500
}
```

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `entrada` | number (>= 0) | ❌ | Valor de entrada |
| `taxaJurosAnual` | number (> 0) | ❌ | Ex.: `0.12` = 12% a.a. (padrão: 6.1%) |
| `prazoMeses` | integer (> 0) | ❌ | Prazo total em meses (padrão: 420) |
| `amortizacaoExtraMes` | number (>= 0) | ❌ | Valor de amortização extra mensal (padrão: 0) |

**Response (`SimulacaoResponseDTO`):**

```json
{
  "entrada": 100000.0,
  "taxaJurosAnual": 0.12,
  "prazoMeses": 360,
  "amortizacaoExtraMes": 500.0,
  "percentualEntrada": 20.0,
  "valorFinanciado": 400000.0,
  "taxaJurosMensal": 0.009489,
  "parcelaMensalPrice": 4114.28,
  "pagamentoTotalMes": 4614.28,
  "nParcelasEfetivas": 290,
  "tempoPagamentoAnos": 24.2,
  "totalPago": 1337141.2,
  "totalJuros": 937141.2,
  "jurosPctFinanciado": 234.3,
  "custoTotalMensal": 5614.28
}
```

---

## Modelos e DTOs

### Campos do imóvel (`ImovelRequestDTO`)

| Campo | Tipo | Regra |
|---|---|---|
| `localizacao` | string | **Obrigatório**, máx 500 chars |
| `preco` | number | **Obrigatório**, > 0 |
| `notaLocalizacao` | integer | Opcional |
| `metragem` | number | Opcional, > 0 |
| `quartos` | integer | Opcional, >= 0 |
| `vagas` | integer | Opcional, >= 0 |
| `qtdBanheiros` | integer | Opcional, >= 0 |
| `varanda` | boolean | Opcional |
| `andar` | integer | Opcional |
| `areaLazer` | boolean | Opcional |
| `vagaCoberta` | boolean | Opcional |
| `distanciaMetroKm` | number | Opcional, >= 0 |
| `iptuMensal` | number | Opcional, >= 0 |
| `condominioMensal` | number | Opcional, >= 0 |
| `anoConstrucao` | integer | Opcional |
| `estadoConservacao` | integer | Opcional |
| `aliquotaIptu` | number | Opcional, >= 0 — ex.: `0.01` = 1% a.a. |
| `observacoes` | string | Opcional |
| `url` | string | Opcional, máx 2048 chars |
| `aceitaPets` | boolean | Opcional |

> No `PATCH`, todos os campos são opcionais.

---

## Filtros de busca

`GET /api/imoveis/buscar?{params}`

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `localizacao` | string | Busca por texto na localização |
| `precoMin` | number | Preço mínimo |
| `precoMax` | number | Preço máximo |
| `metMin` | number | Metragem mínima |
| `quartos` | integer | Número exato de quartos |
| `banheiros` | integer | Número exato de banheiros |
| `vagas` | integer | Número exato de vagas |
| `areaLazer` | boolean | Tem área de lazer |
| `vagaCoberta` | boolean | Tem vaga coberta |
| `distMaxMetro` | number | Distância máxima do metrô (km) |
| `notaMinLoc` | integer | Nota mínima de localização |

Todos os filtros são combinados com AND. Exemplo:

```
GET /api/imoveis/buscar?precoMax=800000&quartos=2&vagaCoberta=true&distMaxMetro=1.0
```

---

## Campos calculados

O backend calcula automaticamente os seguintes campos ao salvar/atualizar um imóvel:

| Campo | Fórmula |
|---|---|
| `precoM2` | `preco / metragem` |
| `custoFixoMensal` | `iptuMensal + condominioMensal` |
| `iptuMensal` | Derivado de `aliquotaIptu * preco / 12` (quando aplicável) |

Na simulação, os principais campos calculados são:

| Campo | Descrição |
|---|---|
| `parcelaMensalPrice` | Parcela pelo sistema Price |
| `nParcelasEfetivas` | Prazo real considerando amortização extra |
| `tempoPagamentoAnos` | Tempo total em anos |
| `totalPago` | Total pago ao longo do financiamento |
| `totalJuros` | Total de juros pagos |
| `custoTotalMensal` | Parcela + custos fixos do imóvel |

---

## Formato de erros

Todos os erros seguem o mesmo contrato retornado pelo `GlobalExceptionHandler`.

**404 — Não encontrado:**

```json
{
  "timestamp": "2026-03-15T10:30:00.000-03:00",
  "status": 404,
  "error": "Not Found",
  "message": "Imóvel não encontrado: 65f0c2...",
  "path": "/api/imoveis/65f0c2..."
}
```

**400 — Erro de validação:**

```json
{
  "timestamp": "2026-03-15T10:30:00.000-03:00",
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

**401 — Não autorizado:**

```json
{
  "timestamp": "2026-03-15T10:30:00.000-03:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token inválido ou expirado"
}
```

---

## Testes

```bash
./mvnw test
```

Os testes cobrem:
- `ImovelServiceTest` — criação, busca, patch, remoção e filtros de imóveis.
- `SimulacaoServiceTest` — criação, cálculo de financiamento, patch e remoção de simulação.
