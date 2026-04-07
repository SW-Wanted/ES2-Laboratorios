/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package enums;

/**
 * Representa os tipos de utilizadores da biblioteca.
 * Enunciado: "maioritariamente estudantes e docentes do ISPTEC"
 * 
 * @author Emanuel
 */
public enum TipoUtilizador {
    ESTUDANTE("Estudante"),
    DOCENTE("Docente");

    private final String descricao;

    TipoUtilizador(String descricao) {
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

