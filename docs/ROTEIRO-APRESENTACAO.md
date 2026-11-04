# Roteiro de apresentação (10 minutos) — Grupo 6, Catálogo de Livros

Antes de começar: `mvn clean verify` já executado, API iniciada (`java -jar target/mini-spring-1.0.0.jar`), coleção Postman importada.

| Tempo | O que mostrar | Quem |
| --- | --- | --- |
| 0:00–1:00 | Tema (livros), campos e regra de negócio (máx. 5.000 páginas). Estrutura de pastas e o pacote `framework` intacto. | Integrante 1 |
| 1:00–3:30 | **Postman** → *Run collection* ou clicando requisição a requisição: POST (201 + aba *Headers* mostrando `Location`), GET lista, GET filtro `?titulo=JAVA`, GET por ID, PUT, DELETE (204). | Integrante 2 |
| 3:30–5:30 | **Erros no Postman**: 400 (título em branco), 422 (5001 páginas) e depois o GET que prova que nada mudou, 404, 405. | Integrante 3 |
| 5:30–7:30 | **Código**: Controller → Service → Repository. Mostrar o `if` da regra no Service e o `@NotBlank`/`@Positive` no `LivroInput`. | Integrante 4 |
| 7:30–9:00 | **Injeção de dependências**: construtores de `LivroController` e `LivroService`; service depende da interface `LivroRepository`; `@Repository` em `InMemoryLivroRepository`. | Integrante 1 |
| 9:00–10:00 | **Testes**: `mvn test` com `BUILD SUCCESS`; citar `ApiIntegrationTest` e `LivroServiceTest`. Fechar com o `requests.http`. | Integrante 2 |

## Perguntas que o professor pode fazer (todos devem saber responder)

- **Qual a responsabilidade de cada camada?** Controller = HTTP; Service = regra de negócio; Repository = armazenamento.
- **Por que a regra de 5.000 páginas fica no Service e não no DTO?** `@Positive`/`@NotBlank` validam a *forma* da entrada; o limite é uma regra do domínio.
- **Como garantimos que uma operação rejeitada não altera dados?** O Service valida antes de chamar o Repository.
- **Como o ID é preservado no PUT?** O ID vem da URL; o repositório recria o `Livro` com o mesmo ID via `computeIfPresent`.
- **O que é injeção por construtor?** O container cria `InMemoryLivroRepository`, depois `LivroService(repository)`, depois `LivroController(service)`.
- **Por que `LivroRepository` é uma interface?** Para trocar o armazenamento (ou usar um fake nos testes) sem mexer no Service.
- **Diferença entre 400, 404 e 422?** 400 = entrada malformada; 404 = recurso não existe; 422 = entrada bem formada que viola a regra de negócio.
- **Por que 201 devolve `Location`?** Informa a URL do recurso recém-criado.
