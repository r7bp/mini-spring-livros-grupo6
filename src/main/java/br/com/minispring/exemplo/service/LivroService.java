package br.com.minispring.exemplo.service;

import br.com.minispring.framework.annotation.Service;
import br.com.minispring.framework.web.HttpException;
import br.com.minispring.exemplo.model.*;
import br.com.minispring.exemplo.repository.LivroRepository;

import java.util.List;
import java.util.Locale;

@Service
public final class LivroService {
    /** Regra de negócio do tema: um livro não pode passar de 5.000 páginas. */
    public static final int MAX_PAGINAS = 5000;

    private final LivroRepository repository;

    // Dependemos da interface, permitindo trocar o armazenamento ou usar um fake nos testes.
    // A implementação concreta é escolhida pelo container durante a inicialização.
    public LivroService(LivroRepository repository) {
        this.repository = repository;
    }

    public List<Livro> list(String titulo) {
        // Normalizar os dois lados permite busca sem distinguir maiúsculas de minúsculas.
        // Locale.ROOT evita que a regra varie conforme o idioma configurado na máquina.
        String filter = titulo.trim().toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(l -> l.titulo().toLowerCase(Locale.ROOT).contains(filter)).toList();
    }

    public Livro get(long id) {
        return repository.findById(id).orElseThrow(() -> notFound(id));
    }

    public Livro create(LivroInput input) {
        // A regra é verificada ANTES de tocar no repositório: se for violada, nada é gravado.
        validateBusinessRule(input);
        return repository.create(input.titulo().trim(), input.numeroPaginas());
    }

    public Livro update(long id, LivroInput input) {
        // Mesma regra do cadastro. Se rejeitar, o livro existente permanece como estava.
        validateBusinessRule(input);
        return repository.update(id, input.titulo().trim(), input.numeroPaginas()).orElseThrow(() -> notFound(id));
    }

    public void delete(long id) {
        if (!repository.delete(id)) throw notFound(id);
    }

    // Esta regra pertence ao domínio; @NotBlank e @Positive validam apenas a forma da entrada.
    // Chamada direta ao service não passa pelo Validator HTTP, mas a regra de negócio sempre roda aqui.
    private void validateBusinessRule(LivroInput input) {
        if (input.numeroPaginas() > MAX_PAGINAS)
            throw new HttpException(422, "Um livro pode ter no máximo " + MAX_PAGINAS + " páginas");
    }

    // Simplificação didática herdada do projeto-base: o service conhece HTTP.
    private HttpException notFound(long id) {
        return new HttpException(404, "Livro não encontrado: " + id);
    }
}
