/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import enums.EstadoEmprestimo;
import enums.TipoUtilizador;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa um utilizador registado na biblioteca.
 *
 * Enunciado: "cada utilizador possui um numero de identificacao unico,
 * nome completo e um contacto valido."
 *
 * Regra de negocio encapsulada:
 * "nao pode ter mais de tres emprestimos activos em simultaneo"
 * 
 * @author Emanuel
 */
public class Utilizador {

    // Constante da regra de negocio — extraida do enunciado
    public static final int MAX_EMPRESTIMOS_ATIVOS = 3;

    private String          id;
    private String          nomeCompleto;
    private String          contacto;
    private TipoUtilizador  tipo;
    private List<Emprestimo> emprestimos;

    // ── Construtor ──────────────────────────────────────────────────────────

    public Utilizador(String id, String nomeCompleto, String contacto,
                      TipoUtilizador tipo) {
        this.id           = id;
        this.nomeCompleto = nomeCompleto;
        this.contacto     = contacto;
        this.tipo         = tipo;
        this.emprestimos  = new ArrayList<>();
    }

    // ── Comportamentos principais ─────────────────────────────────────────────

    /**
     * Verifica se o utilizador pode fazer mais um emprestimo.
     * Enunciado: "nao pode ter mais de tres emprestimos activos em simultaneo"
     */
    public boolean podeRequisitar() {
        long ativos = emprestimos.stream()
                .filter(e -> e.getEstado() == EstadoEmprestimo.ATIVO
                          || e.getEstado() == EstadoEmprestimo.ATRASADO)
                .count();
        return ativos < MAX_EMPRESTIMOS_ATIVOS;
    }

    /**
     * Adiciona um emprestimo ao historico deste utilizador.
     * Enunciado: "o sistema deve manter o historico completo de todos os emprestimos"
     */
    public void adicionarEmprestimo(Emprestimo emprestimo) {
        emprestimos.add(emprestimo);
    }

    /**
     * Devolve uma vista nao-modificavel do historico completo de emprestimos.
     */
    public List<Emprestimo> getHistorico() {
        return Collections.unmodifiableList(emprestimos);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getId() {
        return id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public String getContacto() {
        return contacto;
    }

    public TipoUtilizador getTipo() {
        return tipo;
    }

    // ── Utilitarios ──────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | Contacto: %s",
                id, nomeCompleto, tipo.getDescricao(), contacto);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Utilizador)) return false;
        Utilizador outro = (Utilizador) obj;
        return this.id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

