package br.com.minispring.exemplo.model;

// Entidade principal do domínio. Record = imutável: atualizar significa criar outro Livro
// com o mesmo id. O id é gerado pelo repositório, nunca informado pelo cliente.
public record Livro(long id, String titulo, int numeroPaginas) {}
