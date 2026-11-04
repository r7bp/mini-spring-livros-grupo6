# API de Catálogo de Livros — Grupo 6

API REST em **Java 21** para cadastro dos livros de uma biblioteca, construída sobre o framework didático **Mini Spring** (projeto-base: <https://github.com/ferreiralisson/mini-spring>). Os dados ficam em memória e o pacote `framework` foi mantido sem alterações: toda a adaptação está em `br.com.minispring.exemplo`.

## Integrantes

| Nome | Matrícula |
|------|-----------|
| Caique Lucas Ribeiro Lupercinio dos Santos | 202517640070 |
| Davyd de Oliveira Melo | 202517640072 |
| Gleysson Robert de Oliveira Silva | 202517640014 |
| Pablo Henrique Braz dos Santos | 202517640046 |
| Ruan Henry Nascimento Cunha | 202517640037 |
| Victor Kauã Dias Ferreira | 202517640039 |

## Domínio

Uma biblioteca precisa manter o catálogo de seus livros. A entidade principal é o **Livro**, com:

| Campo | Tipo | Observação |
| --- | --- | --- |
| `id` | número | Gerado automaticamente; nunca informado pelo cliente; preservado nas atualizações |
| `titulo` | texto | Obrigatório; não aceita vazio nem apenas espaços; espaços nas pontas são removidos |
| `numeroPaginas` | inteiro | Maior que zero e no máximo **5.000** |

Escopo: apenas o catálogo (sem empréstimos, autores ou usuários).

## Regra de negócio

> Um livro pode ter, no máximo, **5.000 páginas**.

A regra é aplicada na camada **Service**, no cadastro e na atualização. Se violada, a API responde **422** e **nada é gravado**: a verificação acontece antes de qualquer chamada ao repositório, então um `PUT` rejeitado deixa o livro existente exatamente como estava.

As validações estruturais (`@NotBlank` em `titulo`, `@Positive` em `numeroPaginas`) ficam no DTO `LivroInput` e são aplicadas pelo framework antes do controller, tanto no `POST` quanto no `PUT`. Elas geram **400**.

## Como executar

Pré-requisitos: **JDK 21** e **Maven 3.9+** (`java -version` e `mvn -version`). Na primeira compilação é preciso internet para baixar as dependências.

```bash
git clone <URL-DO-REPOSITORIO>
cd <pasta-do-repositorio>

mvn clean verify                          # compila, roda os testes e gera o JAR
java -jar target/mini-spring-1.0.0.jar    # sobe a API em http://127.0.0.1:8080
```

Confirme que está no ar: `curl -i http://127.0.0.1:8080/health` → `200` e `{"status":"UP"}`.

Também é possível rodar pela IDE executando `br.com.minispring.exemplo.Application`. Para trocar a porta: `java -Dserver.port=9090 -jar target/mini-spring-1.0.0.jar`.

Executar só os testes: `mvn test`.

## Rotas

Base: `http://127.0.0.1:8080` · corpo e respostas em JSON.

| Método | Rota | Descrição | Sucesso |
| --- | --- | --- | --- |
| `POST` | `/livros` | Cadastra um livro | `201` + cabeçalho `Location: /livros/{id}` |
| `GET` | `/livros` | Lista todos | `200` |
| `GET` | `/livros?titulo=java` | Filtra por parte do título, sem diferenciar maiúsculas/minúsculas | `200` |
| `GET` | `/livros/{id}` | Consulta por ID | `200` |
| `PUT` | `/livros/{id}` | Atualiza (o ID é preservado) | `200` |
| `DELETE` | `/livros/{id}` | Exclui | `204` (sem corpo) |
| `GET` | `/` e `/health` | Identificação e saúde da aplicação | `200` |

Corpo de `POST` e `PUT`:

```json
{ "titulo": "Dom Casmurro", "numeroPaginas": 256 }
```

### Respostas de erro

| Status | Quando |
| --- | --- |
| `400` | Título vazio/em branco, páginas ≤ 0, JSON inválido, campo desconhecido (ex.: `id`), tipo errado, ID não numérico |
| `404` | Livro inexistente (GET, PUT ou DELETE) ou rota inexistente |
| `422` | Regra de negócio violada: mais de 5.000 páginas |
| `405` / `415` | Método não permitido / `Content-Type` diferente de `application/json` |

Erros têm formato uniforme: `timestamp`, `status`, `message`, `path`, `details` e `requestId`.

## Testando

- **`requests.http`**: exemplos de sucesso e de erro para o cliente HTTP do IntelliJ ou a extensão REST Client do VS Code. Rode em ordem, com a aplicação recém-iniciada.
- **Postman**: importe `postman/Livros-API.postman_collection.json` e `postman/Livros-API.local.postman_environment.json` (*Import* → arquivos) e selecione o ambiente **Livros API - Local**. A coleção tem 28 requisições em três pastas (Sucesso, Erros, Limpeza), com testes automáticos de status e salva o ID criado na variável `livroId`. Para rodar tudo de uma vez, use *Run collection*. A variável `baseUrl` está em `http://127.0.0.1:8080`.
- **Testes automatizados** (`mvn test`):
  - `ApiIntegrationTest`: sobe um Jetty real e exercita CRUD, filtro, IDs, 400/404/405/415/422, limites 5000/5001 e garante que operações rejeitadas não alteram os dados.
  - `LivroServiceTest`: testa a regra de negócio na camada Service, sem HTTP.
  - `ApplicationContextTest` e `RouterTest`: testes do framework (herdados do projeto-base).

## Arquitetura

```
src/main/java/br/com/minispring/
├── framework/            Mini framework (não alterado): IoC, roteamento, validação, Jetty
└── exemplo/
    ├── Application.java  Ponto de entrada
    ├── controller/       LivroController — contrato HTTP, sem regra de negócio
    ├── service/          LivroService — regra de negócio (limite de 5.000 páginas)
    ├── repository/       LivroRepository (interface) + InMemoryLivroRepository
    └── model/            Livro (entidade) e LivroInput (DTO de entrada com validação)
```

| Camada | Responsabilidade |
| --- | --- |
| **Controller** | Receber a requisição já convertida/validada e devolver `HttpResult` (201, 204…). Não decide regras. |
| **Service** | Aplicar a regra de negócio, normalizar o título, traduzir ausência em 404. |
| **Repository** | Guardar e buscar livros em memória e gerar IDs. Não conhece HTTP nem regras. |

**Injeção de dependências por construtor:** `LivroController(LivroService)` → `LivroService(LivroRepository)` → `InMemoryLivroRepository`. O service depende da *interface* `LivroRepository`; o container (`ApplicationContext`) descobre a implementação anotada com `@Repository` e a entrega na inicialização.

### Caminho de uma requisição (`POST /livros`)

1. O **Jetty** recebe o HTTP e entrega ao `DispatcherServlet` (Front Controller).
2. O **Router** encontra o método `LivroController.create` pelo verbo + caminho.
3. O **Jackson** converte o JSON em `LivroInput` e o **Validator** aplica `@NotBlank`/`@Positive` (falha → 400).
4. O **Controller** chama `LivroService.create(input)`.
5. O **Service** verifica o limite de 5.000 páginas (falha → 422) e só então chama o **Repository**.
6. O **Repository** gera o ID, guarda o `Livro` em memória e o devolve.
7. O Controller responde `HttpResult.created("/livros/{id}", livro)` → o dispatcher serializa em JSON e envia **201** com `Location`.

Mais detalhes do framework em [`docs/ARQUITETURA.md`](docs/ARQUITETURA.md).

## Limitações

Dados em memória (perdidos ao reiniciar), sem banco de dados, frontend ou autenticação, conforme o escopo da atividade.
