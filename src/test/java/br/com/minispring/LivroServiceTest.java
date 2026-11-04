package br.com.minispring;

import br.com.minispring.exemplo.model.Livro;
import br.com.minispring.exemplo.model.LivroInput;
import br.com.minispring.exemplo.repository.InMemoryLivroRepository;
import br.com.minispring.exemplo.service.LivroService;
import br.com.minispring.framework.web.HttpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Teste unitário da camada Service, sem Jetty: o repositório é injetado pelo construtor. */
class LivroServiceTest {
    private InMemoryLivroRepository repository;
    private LivroService service;

    @BeforeEach void setUp() {
        repository = new InMemoryLivroRepository();
        service = new LivroService(repository);
    }

    @Test void createsTrimmedTitleAndGeneratedId() {
        Livro livro = service.create(new LivroInput("  O Cortiço  ", 200));
        assertEquals(1, livro.id());
        assertEquals("O Cortiço", livro.titulo());
    }

    @Test void acceptsExactLimitAndRejectsAboveWith422() {
        assertEquals(5000, service.create(new LivroInput("Enorme", 5000)).numeroPaginas());
        var e = assertThrows(HttpException.class, () -> service.create(new LivroInput("Maior", 5001)));
        assertEquals(422, e.status());
        assertEquals(1, repository.findAll().size());
    }

    @Test void rejectedUpdateDoesNotChangeStoredBook() {
        Livro original = service.create(new LivroInput("Original", 100));
        var e = assertThrows(HttpException.class, () -> service.update(original.id(), new LivroInput("Novo", 6000)));
        assertEquals(422, e.status());
        assertEquals(original, service.get(original.id()));
    }

    @Test void updatePreservesId() {
        Livro original = service.create(new LivroInput("Original", 100));
        Livro atualizado = service.update(original.id(), new LivroInput("Atualizado", 150));
        assertEquals(original.id(), atualizado.id());
        assertEquals("Atualizado", service.get(original.id()).titulo());
    }

    @Test void missingBookGives404ForGetUpdateAndDelete() {
        assertEquals(404, assertThrows(HttpException.class, () -> service.get(99)).status());
        assertEquals(404, assertThrows(HttpException.class, () -> service.update(99, new LivroInput("X", 1))).status());
        assertEquals(404, assertThrows(HttpException.class, () -> service.delete(99)).status());
    }

    @Test void filterIgnoresCaseAndMatchesPartOfTitle() {
        service.create(new LivroInput("Clean Code", 464));
        service.create(new LivroInput("Código Limpo", 425));
        assertEquals(1, service.list("CLEAN").size());
        assertEquals(1, service.list("limpo").size());
        assertEquals(2, service.list("").size());
    }
}
