/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import enums.EstadoEmprestimo;
import enums.EstadoExemplar;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Representa um registo unico de emprestimo na biblioteca.
 *
 * Enunciado: "cada emprestimo possui uma data de inicio e uma data prevista
 * de devolucao... cada emprestimo corresponde a um registo unico, envolvendo
 * apenas um utilizador e um unico livro."
 *
 * Associacao com Utilizador: 1 Emprestimo -> 1 Utilizador
 * Associacao com Exemplar:   1 Emprestimo -> 1 Exemplar
 * 
 * @author Emanuel
 */
public class Emprestimo {

    private static int contadorId = 1; // gerador automatico de IDs

    private int              id;
    private Utilizador       utilizador;
    private Exemplar         exemplar;
    private LocalDate        dataInicio;
    private LocalDate        dataPrevistaDevolucao;
    private LocalDate        dataEfetivaDevolucao;  // null enquanto nao devolvido
    private EstadoEmprestimo estado;

    // Prazo padrao de emprestimo: 14 dias
    private static final int PRAZO_DIAS = 14;

    // ── Construtor ──────────────────────────────────────────────────────────

    public Emprestimo(Utilizador utilizador, Exemplar exemplar) {
        this.id                    = contadorId++;
        this.utilizador            = utilizador;
        this.exemplar              = exemplar;
        this.dataInicio            = LocalDate.now();
        this.dataPrevistaDevolucao = LocalDate.now().plusDays(PRAZO_DIAS);
        this.dataEfetivaDevolucao  = null;
        this.estado                = EstadoEmprestimo.ATIVO;
    }

    // ── Comportamentos principais ─────────────────────────────────────────────

    /**
     * Regista a devolucao do exemplar.
     * Enunciado: "no momento da devolucao, o sistema deve atualizar o estado
     * do emprestimo e refletir automaticamente a reposicao do exemplar"
     */
    public void devolver() {
        if (estado == EstadoEmprestimo.DEVOLVIDO) {
            System.out.println("  ⚠  Emprestimo #" + id + " ja foi devolvido anteriormente.");
            return;
        }
        this.dataEfetivaDevolucao = LocalDate.now();
        this.estado               = EstadoEmprestimo.DEVOLVIDO;
        // Repoe o exemplar como disponivel — coerencia com o enunciado
        exemplar.setEstado(EstadoExemplar.DISPONIVEL);
        System.out.println("  ✔  Livro \"" + exemplar.getLivro().getTitulo()
                + "\" (Exemplar #" + exemplar.getExemplarId()
                + ") devolvido por " + utilizador.getNomeCompleto() + ".");
    }

    /**
     * Verifica e actualiza o estado para ATRASADO se necessario.
     * Enunciado: estado pode ser "ativo, devolvido ou atrasado"
     */
    public void verificarAtraso() {
        if (estado == EstadoEmprestimo.ATIVO
                && LocalDate.now().isAfter(dataPrevistaDevolucao)) {
            this.estado = EstadoEmprestimo.ATRASADO;
        }
    }

    /**
     * Indica se o emprestimo esta activo ou atrasado (ainda em posse do utilizador).
     */
    public boolean estaAtivo() {
        return estado == EstadoEmprestimo.ATIVO
            || estado == EstadoEmprestimo.ATRASADO;
    }

    /**
     * Calcula o numero de dias de atraso (0 se nao estiver atrasado).
     */
    public long getDiasAtraso() {
        if (estado != EstadoEmprestimo.ATRASADO) return 0;
        LocalDate referencia = (dataEfetivaDevolucao != null)
                ? dataEfetivaDevolucao
                : LocalDate.now();
        return ChronoUnit.DAYS.between(dataPrevistaDevolucao, referencia);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int getId() {
        return id;
    }

    public Utilizador getUtilizador() {
        return utilizador;
    }

    public Exemplar getExemplar() {
        return exemplar;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataPrevistaDevolucao() {
        return dataPrevistaDevolucao;
    }

    public LocalDate getDataEfetivaDevolucao() {
        return dataEfetivaDevolucao;
    }

    public EstadoEmprestimo getEstado() {
        return estado;
    }

    // ── Utilitarios ──────────────────────────────────────────────────────────

    @Override
    public String toString() {
        String devolucao = (dataEfetivaDevolucao != null)
                ? dataEfetivaDevolucao.toString()
                : "pendente";
        return String.format(
                "Emprestimo #%d | Utilizador: %s | Exemplar #%d (\"%s\") | "
                + "Inicio: %s | Prev. devolucao: %s | Dev. efectiva: %s | Estado: %s",
                id,
                utilizador.getNomeCompleto(),
                exemplar.getExemplarId(),
                exemplar.getLivro().getTitulo(),
                dataInicio,
                dataPrevistaDevolucao,
                devolucao,
                estado.getDescricao());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Emprestimo)) return false;
        Emprestimo outro = (Emprestimo) obj;
        return this.id == outro.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}

