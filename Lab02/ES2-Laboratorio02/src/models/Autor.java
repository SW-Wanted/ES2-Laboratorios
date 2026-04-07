/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 * Representa um autor de livros na biblioteca.
 *
 * Necessidade implicita: o enunciado menciona "autor" como atributo do livro,
 * mas modelar Autor como classe propria permite reutilizacao (um autor pode
 * ter varios livros) e extensibilidade futura.
 * 
 * @author Emanuel
 */
public class Autor {

    private int id;
    private String nome;
    private String nacionalidade;

    // ── Construtor ──────────────────────────────────────────────────────────

    public Autor(int id, String nome, String nacionalidade) {
        this.id            = id;
        this.nome          = nome;
        this.nacionalidade = nacionalidade;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNacionalidade() {
        return nacionalidade;
    }

    public void setNacionalidade(String nacionalidade) {
        this.nacionalidade = nacionalidade;
    }

    // ── Utilitarios ──────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return nome + " (" + nacionalidade + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Autor)) return false;
        Autor outro = (Autor) obj;
        return this.id == outro.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
