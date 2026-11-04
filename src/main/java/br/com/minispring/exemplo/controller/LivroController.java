package br.com.minispring.exemplo.controller;

import br.com.minispring.framework.annotation.*;
import br.com.minispring.framework.web.HttpResult;
import br.com.minispring.exemplo.model.*;
import br.com.minispring.exemplo.service.LivroService;
import java.util.List;

@RestController("/livros")
public final class LivroController {
    private final LivroService service;
    // DI por construtor: o container fornece o service. O controller não escolhe
    // como construí-lo, e a dependência fica explícita para quem lê ou testa a classe.
    public LivroController(LivroService service) { this.service = service; }

    // GET /livros?titulo=java. Sem query param, o default vazio lista todos os livros.
    @Route(method = "GET")
    public List<Livro> list(@RequestParam("titulo") String titulo) { return service.list(titulo); }

    // O "id" da anotação corresponde ao {id} da rota. O dispatcher converte texto em long.
    @Route(method = "GET", path = "/{id}")
    public Livro get(@PathVariable("id") long id) { return service.get(id); }

    // O input vem do JSON e já foi validado (@NotBlank/@Positive) antes deste método começar.
    // 201 + cabeçalho Location apontando para o recurso criado.
    @Route(method = "POST")
    public HttpResult create(@RequestBody LivroInput input) {
        Livro livro = service.create(input);
        return HttpResult.created("/livros/" + livro.id(), livro);
    }

    // Dados da URL (identidade) + dados do corpo (novos valores). O id da URL é preservado.
    @Route(method = "PUT", path = "/{id}")
    public Livro update(@PathVariable("id") long id, @RequestBody LivroInput input) {
        return service.update(id, input);
    }

    // Não há representação para devolver após excluir: 204 sem corpo.
    @Route(method = "DELETE", path = "/{id}")
    public HttpResult delete(@PathVariable("id") long id) {
        service.delete(id);
        return HttpResult.noContent();
    }
}
