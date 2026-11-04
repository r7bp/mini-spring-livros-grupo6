package br.com.minispring.exemplo.model;

import br.com.minispring.framework.validation.NotBlank;
import br.com.minispring.framework.validation.Positive;

/** DTO de entrada separado do modelo: o cliente não escolhe o ID. */
// @NotBlank rejeita titulo nulo, vazio ou só com espaços; @Positive exige numeroPaginas > 0.
// As anotações são metadados: quem as verifica é o Validator, chamado pelo dispatcher
// tanto no POST quanto no PUT (por isso as mesmas validações valem no cadastro e na atualização).
public record LivroInput(@NotBlank String titulo, @Positive int numeroPaginas) {}
