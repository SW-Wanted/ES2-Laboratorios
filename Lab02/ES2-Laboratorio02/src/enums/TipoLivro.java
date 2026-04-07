/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package enums;

/**
 * Representa os tipos de livros disponiveis na biblioteca.
 * Enunciado: "acervo diversificado de livros tecnicos e cientificos"
 * 
 * @author Emanuel
 */
public enum TipoLivro {
    TECNICO("Tecnico"),
    CIENTIFICO("Cientifico");

    private final String descricao;

    TipoLivro(String descricao) {
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

