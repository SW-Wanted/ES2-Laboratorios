/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import enums.EstadoExemplar;
import java.time.LocalDate;

/**
 * Representa um exemplar fisico de um livro.
 *
 * Enunciado: "alguns titulos possuem varios exemplares fisicos, o que permite
 * que mais de um utilizador possa requisitar o mesmo livro"
 *
 * Relacao de COMPOSICAO com Livro: o exemplar nao existe sem o livro.
 * Se o livro for removido do sistema, os seus exemplares deixam de existir.
 * 
 * @author Emanuel
 */
public class Exemplar {

    private int           exemplarId;
    private String        isbn;
    private Livro         livro;
    private EstadoExemplar estado;
    private LocalDate     dataAquisicao;

    // ── Construtor ──────────────────────────────────────────────────────────

    public Exemplar(int exemplarId, Livro livro, LocalDate dataAquisicao) {
        this.exemplarId    = exemplarId;
        this.isbn          = livro.getIsbn();
        this.livro         = livro;
        this.estado        = EstadoExemplar.DISPONIVEL; // estado inicial
        this.dataAquisicao = dataAquisicao;
    }

    // ── Comportamentos principais ─────────────────────────────────────────────

    /**
     * Verifica se este exemplar pode ser emprestado.
     * Enunciado: "o sistema deve verificar se existem exemplares disponiveis"
     */
    public boolean estaDisponivel() {
        return estado == EstadoExemplar.DISPONIVEL;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int getExemplarId() {
        return exemplarId;
    }

    public String getIsbn() {
        return isbn;
    }

    public Livro getLivro() {
        return livro;
    }

    public EstadoExemplar getEstado() {
        return estado;
    }

    /**
     * Actualiza o estado do exemplar.
     * Chamado por Emprestimo.devolver() e BibliotecaService.registarEmprestimo()
     */
    public void setEstado(EstadoExemplar estado) {
        this.estado = estado;
    }

    public LocalDate getDataAquisicao() {
        return dataAquisicao;
    }

    // ── Utilitarios ──────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("Exemplar #%d | ISBN: %s | \"%s\" | Estado: %s",
                exemplarId, isbn, livro.getTitulo(), estado.getDescricao());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Exemplar)) return false;
        Exemplar outro = (Exemplar) obj;
        return this.exemplarId == outro.exemplarId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(exemplarId);
    }
}

