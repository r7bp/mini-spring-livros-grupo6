package br.com.minispring.exemplo.repository;

import br.com.minispring.exemplo.model.Livro;

import java.util.List;
import java.util.Optional;

/**
 * A aplicação depende de uma abstração; o container encontra a implementação.
 */
public interface LivroRepository {
    Livro create(String titulo, int numeroPaginas);

    List<Livro> findAll();

    // Optional torna a ausência explícita. O service decide o significado dessa ausência
    // para o caso de uso, em vez de o repository escolher uma resposta HTTP.
    Optional<Livro> findById(long id);

    Optional<Livro> update(long id, String titulo, int numeroPaginas);

    boolean delete(long id);
}
