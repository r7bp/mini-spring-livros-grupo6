package br.com.minispring.exemplo.repository;

import br.com.minispring.framework.annotation.Repository;
import br.com.minispring.exemplo.model.Livro;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public final class InMemoryLivroRepository implements LivroRepository {
    // Beans singleton são compartilhados entre requisições concorrentes.
    private final ConcurrentHashMap<Long, Livro> livros = new ConcurrentHashMap<>();
    // incrementAndGet gera IDs de forma atômica. A sequência reinicia junto com a aplicação.
    private final AtomicLong sequence = new AtomicLong();

    public Livro create(String titulo, int numeroPaginas) {
        long id = sequence.incrementAndGet();
        var livro = new Livro(id, titulo, numeroPaginas);
        livros.put(id, livro);
        return livro;
    }

    // Ordenamos porque ConcurrentHashMap não garante ordem de iteração.
    public List<Livro> findAll() {
        return livros.values().stream().sorted(Comparator.comparingLong(Livro::id)).toList();
    }

    public Optional<Livro> findById(long id) {
        return Optional.ofNullable(livros.get(id));
    }

    // computeIfPresent consulta e substitui atomicamente. O id é preservado: o novo Livro
    // reutiliza o id recebido, e um id inexistente nunca é criado por acidente.
    public Optional<Livro> update(long id, String titulo, int numeroPaginas) {
        return Optional.ofNullable(livros.computeIfPresent(id, (key, old) -> new Livro(id, titulo, numeroPaginas)));
    }

    public boolean delete(long id) {
        return livros.remove(id) != null;
    }
}
