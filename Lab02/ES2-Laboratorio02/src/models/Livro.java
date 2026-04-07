/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import enums.TipoLivro;

/**
 * Representa um titulo de livro na biblioteca.
 *
 * Enunciado: "cada livro e identificado por um codigo unico (ISBN) e contem
 * informacoes como titulo, ano de publicacao e autor."
 *
 * Nota: Livro representa o TITULO (entidade logica).
 * Os exemplares fisicos sao representados pela classe Exemplar.
 * Relacao de COMPOSICAO: Livro -> Exemplar (1 para 1..*)
 * 
 * @author Emanuel
 */
public class Livro {

    private int      livroId;
    private String   isbn;
    private String   titulo;
    private int      anoPublicacao;
    private TipoLivro tipo;
    private Autor    autor;

    // ── Construtor ──────────────────────────────────────────────────────────

    public Livro(int livroId, String isbn, String titulo, int anoPublicacao,
                 TipoLivro tipo, Autor autor) {
        this.livroId       = livroId;
        this.isbn          = isbn;
        this.titulo        = titulo;
        this.anoPublicacao = anoPublicacao;
        this.tipo          = tipo;
        this.autor         = autor;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int getLivroId() {
        return livroId;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getAnoPublicacao() {
        return anoPublicacao;
    }

    public TipoLivro getTipo() {
        return tipo;
    }

    public Autor getAutor() {
        return autor;
    }

    // ── Utilitarios ──────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("[%s] %s - %s (%d) | %s",
                isbn, titulo, autor.getNome(), anoPublicacao, tipo.getDescricao());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Livro)) return false;
        Livro outro = (Livro) obj;
        return this.isbn.equals(outro.isbn);
    }

    @Override
    public int hashCode() {
        return isbn.hashCode();
    }
}
