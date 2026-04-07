/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import enums.EstadoExemplar;
import models.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servico central da biblioteca.
 *
 * Responsavel por orquestrar as operacoes principais do sistema:
 *  - Registo de utilizadores, livros e exemplares
 *  - Registo e devolucao de emprestimos
 *  - Pesquisa de entidades
 *  - Aplicacao de todas as regras de negocio do enunciado
 * 
 * @author Emanuel
 */
public class BibliotecaService {

    private final List<Utilizador> utilizadores = new ArrayList<>();
    private final List<Livro>      livros        = new ArrayList<>();
    private final List<Exemplar>   exemplares    = new ArrayList<>();
    private final List<Emprestimo> emprestimos   = new ArrayList<>();

    // ══════════════════════════════════════════════════════════════════════════
    // REGISTO DE ENTIDADES
    // ══════════════════════════════════════════════════════════════════════════

    public void registarUtilizador(Utilizador utilizador) {
        utilizadores.add(utilizador);
        System.out.println("  ✔  Utilizador registado: " + utilizador);
    }

    public void registarLivro(Livro livro) {
        livros.add(livro);
        System.out.println("  ✔  Livro registado: " + livro);
    }

    public void registarExemplar(Exemplar exemplar) {
        exemplares.add(exemplar);
        System.out.println("  ✔  Exemplar registado: " + exemplar);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // OPERACAO PRINCIPAL: REGISTAR EMPRESTIMO
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Tenta registar um emprestimo aplicando todas as regras do enunciado:
     *  1. Utilizador deve estar registado no sistema
     *  2. Utilizador nao pode ter mais de 3 emprestimos activos
     *  3. Deve existir pelo menos um exemplar disponivel do livro pretendido
     *
     * @return o Emprestimo criado, ou null se alguma regra for violada.
     */
    public Emprestimo registarEmprestimo(Utilizador utilizador, Livro livro) {
        System.out.println("\n  → Tentativa de emprestimo: "
                + utilizador.getNomeCompleto() + " | \"" + livro.getTitulo() + "\"");

        if (!utilizadores.contains(utilizador)) {
            System.out.println("  ✖  Utilizador nao registado no sistema.");
            return null;
        }
        if (!utilizador.podeRequisitar()) {
            System.out.println("  ✖  " + utilizador.getNomeCompleto()
                    + " ja possui " + Utilizador.MAX_EMPRESTIMOS_ATIVOS
                    + " emprestimos activos. Limite atingido.");
            return null;
        }

        Optional<Exemplar> exemplaDisponivel = exemplares.stream()
                .filter(e -> e.getLivro().equals(livro) && e.estaDisponivel())
                .findFirst();

        if (exemplaDisponivel.isEmpty()) {
            System.out.println("  ✖  Sem exemplares disponiveis para \""
                    + livro.getTitulo() + "\".");
            return null;
        }

        Exemplar exemplar = exemplaDisponivel.get();
        exemplar.setEstado(EstadoExemplar.EMPRESTADO);

        Emprestimo emprestimo = new Emprestimo(utilizador, exemplar);
        utilizador.adicionarEmprestimo(emprestimo);
        emprestimos.add(emprestimo);

        System.out.println("  ✔  Emprestimo #" + emprestimo.getId()
                + " registado. Exemplar #" + exemplar.getExemplarId()
                + " → " + utilizador.getNomeCompleto()
                + " | Devolucao prevista: " + emprestimo.getDataPrevistaDevolucao());

        return emprestimo;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // OPERACAO PRINCIPAL: DEVOLVER LIVRO
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Processa a devolucao de um emprestimo.
     * Enunciado: "atualizar o estado do emprestimo e refletir automaticamente
     * a reposicao do exemplar na disponibilidade do livro"
     */
    public void devolverLivro(Emprestimo emprestimo) {
        System.out.println("\n  → Devolucao do emprestimo #" + emprestimo.getId() + ":");
        emprestimo.devolver();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PESQUISA DE ENTIDADES
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Procura um utilizador pelo seu ID unico.
     * @return O Utilizador encontrado, ou null se nao existir.
     */
    public Utilizador buscarUtilizadorPorId(String id) {
        return utilizadores.stream()
                .filter(u -> u.getId().equalsIgnoreCase(id))
                .findFirst().orElse(null);
    }

    /**
     * Procura um livro pelo seu ISBN.
     * @return O Livro encontrado, ou null se nao existir.
     */
    public Livro buscarLivroPorIsbn(String isbn) {
        return livros.stream()
                .filter(l -> l.getIsbn().equals(isbn))
                .findFirst().orElse(null);
    }

    /**
     * Procura um emprestimo pelo seu ID numerico.
     * @return O Emprestimo encontrado, ou null se nao existir.
     */
    public Emprestimo buscarEmprestimoPorId(int id) {
        return emprestimos.stream()
                .filter(e -> e.getId() == id)
                .findFirst().orElse(null);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // VERIFICACAO DE DISPONIBILIDADE
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Conta os exemplares disponiveis de um livro.
     * Enunciado: "verificar se existem exemplares disponiveis"
     */
    public long verificarDisponibilidade(Livro livro) {
        return exemplares.stream()
                .filter(e -> e.getLivro().equals(livro) && e.estaDisponivel())
                .count();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // RELATORIOS
    // ══════════════════════════════════════════════════════════════════════════

    public void actualizarEstadosAtraso() {
        emprestimos.forEach(Emprestimo::verificarAtraso);
    }

    public void imprimirTodosUtilizadores() {
        System.out.println("\n  ── Utilizadores registados ──");
        utilizadores.forEach(u -> System.out.println("  " + u));
    }

    public void imprimirTodosLivros() {
        System.out.println("\n  ── Livros registados ──");
        livros.forEach(l -> System.out.println("  " + l));
    }

    public void imprimirTodosExemplares() {
        System.out.println("\n  ── Exemplares ──");
        exemplares.forEach(e -> System.out.println("  " + e));
    }

    public void imprimirHistoricoGeral() {
        System.out.println("\n  ── Historico completo de emprestimos ──");
        if (emprestimos.isEmpty()) { System.out.println("  (sem registos)"); return; }
        emprestimos.forEach(e -> System.out.println("  " + e));
    }

    public void imprimirHistoricoUtilizador(Utilizador utilizador) {
        System.out.println("\n  ── Historico de " + utilizador.getNomeCompleto() + " ──");
        List<Emprestimo> hist = utilizador.getHistorico();
        if (hist.isEmpty()) { System.out.println("  (sem emprestimos)"); return; }
        hist.forEach(e -> System.out.println("  " + e));
    }

    public void imprimirDisponibilidade(Livro livro) {
        long disp = verificarDisponibilidade(livro);
        System.out.println("  Disponibilidade de \"" + livro.getTitulo()
                + "\": " + disp + " exemplar(es) disponivel(is).");
    }

    public List<Utilizador> getUtilizadores() { return utilizadores; }
    public List<Livro>      getLivros()        { return livros; }
    public List<Exemplar>   getExemplares()    { return exemplares; }
    public List<Emprestimo> getEmprestimos()   { return emprestimos; }
}
