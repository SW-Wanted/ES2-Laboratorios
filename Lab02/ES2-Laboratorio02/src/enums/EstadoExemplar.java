/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package enums;

/**
 * Representa o estado fisico de um exemplar na biblioteca.
 * Inferencia implicita do enunciado: a disponibilidade do exemplar
 * deriva do estado do emprestimo activo associado a ele.
 * 
 * @author Emanuel
 */
public enum EstadoExemplar {
    DISPONIVEL("Disponivel"),
    EMPRESTADO("Emprestado"),
    RESERVADO("Reservado");

    private final String descricao;

    EstadoExemplar(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
