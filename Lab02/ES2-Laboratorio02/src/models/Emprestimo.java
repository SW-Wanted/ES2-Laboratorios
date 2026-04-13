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
 * @brief  Representa um registo único de empréstimo na biblioteca.
 *
 * Enunciado: "cada empréstimo possui uma data de início e uma data prevista
 * de devolução... cada empréstimo corresponde a um registo único, envolvendo
 * apenas um utilizador e um único exemplar."
 *
 * Associação com Utilizador: 1 Empréstimo → 1 Utilizador
 * Associação com Exemplar:   1 Empréstimo → 1 Exemplar
 */
public class Emprestimo {

    private int              id;
    private Utilizador       utilizador;
    private Exemplar         exemplar;
    private LocalDate        dataInicio;
    private LocalDate        dataPrevistaDevolucao;
    private LocalDate        dataEfetivaDevolucao;
    private EstadoEmprestimo estado;

    /** Prazo padrão de empréstimo em dias. */
    public static final int PRAZO_DIAS = 14;

    // ── Construtores ─────────────────────────────────────────────────────────

    /**
     * @brief  Construtor usado ao criar um novo empréstimo em runtime.
     *         O ID é atribuído pelo MySQL (AUTO_INCREMENT).
     * @param  id         ID gerado pelo banco de dados.
     * @param  utilizador Utilizador que requisitou.
     * @param  exemplar   Exemplar emprestado.
     * @param  inicio     Data de início do empréstimo.
     * @param  devolucao  Data prevista de devolução.
     */
    public Emprestimo(int id, Utilizador utilizador, Exemplar exemplar,
                      LocalDate inicio, LocalDate devolucao) {
        this.id                    = id;
        this.utilizador            = utilizador;
        this.exemplar              = exemplar;
        this.dataInicio            = inicio;
        this.dataPrevistaDevolucao = devolucao;
        this.dataEfetivaDevolucao  = null;
        this.estado                = EstadoEmprestimo.ATIVO;
    }

    /**
     * @brief  Construtor completo usado ao reconstruir um empréstimo da base de dados.
     * @param  id              ID do empréstimo.
     * @param  utilizador      Utilizador associado.
     * @param  exemplar        Exemplar associado.
     * @param  inicio          Data de início.
     * @param  devolucaoPrev   Data prevista de devolução.
     * @param  devolucaoEfet   Data efectiva de devolução (pode ser null).
     * @param  estado          Estado actual do empréstimo.
     */
    public Emprestimo(int id, Utilizador utilizador, Exemplar exemplar,
                      LocalDate inicio, LocalDate devolucaoPrev,
                      LocalDate devolucaoEfet, EstadoEmprestimo estado) {
        this.id                    = id;
        this.utilizador            = utilizador;
        this.exemplar              = exemplar;
        this.dataInicio            = inicio;
        this.dataPrevistaDevolucao = devolucaoPrev;
        this.dataEfetivaDevolucao  = devolucaoEfet;
        this.estado                = estado;
    }

    // ── Comportamentos principais ─────────────────────────────────────────────

    /**
     * @brief  Marca o empréstimo como devolvido em memória.
     *         A persistência no banco é feita pelo BibliotecaService.
     * @param  dataEfetiva  Data real em que o livro foi devolvido.
     */
    public void marcarDevolvido(LocalDate dataEfetiva) {
        this.dataEfetivaDevolucao = dataEfetiva;
        this.estado               = EstadoEmprestimo.DEVOLVIDO;
        exemplar.setEstado(EstadoExemplar.DISPONIVEL);
    }

    /**
     * @brief  Verifica e actualiza o estado para ATRASADO se necessário.
     */
    public void verificarAtraso() {
        if (estado == EstadoEmprestimo.ATIVO
                && LocalDate.now().isAfter(dataPrevistaDevolucao)) {
            this.estado = EstadoEmprestimo.ATRASADO;
        }
    }

    /**
     * @brief   Indica se o empréstimo ainda está em posse do utilizador.
     * @return  {@code true} se ATIVO ou ATRASADO.
     */
    public boolean estaAtivo() {
        return estado == EstadoEmprestimo.ATIVO
            || estado == EstadoEmprestimo.ATRASADO;
    }

    /**
     * @brief   Calcula os dias de atraso.
     * @return  Número de dias de atraso, ou 0 se não estiver atrasado.
     */
    public long getDiasAtraso() {
        if (estado != EstadoEmprestimo.ATRASADO) return 0;
        LocalDate ref = (dataEfetivaDevolucao != null) ? dataEfetivaDevolucao : LocalDate.now();
        return ChronoUnit.DAYS.between(dataPrevistaDevolucao, ref);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int              getId()                    { return id; }
    public Utilizador       getUtilizador()            { return utilizador; }
    public Exemplar         getExemplar()              { return exemplar; }
    public LocalDate        getDataInicio()            { return dataInicio; }
    public LocalDate        getDataPrevistaDevolucao() { return dataPrevistaDevolucao; }
    public LocalDate        getDataEfetivaDevolucao()  { return dataEfetivaDevolucao; }
    public EstadoEmprestimo getEstado()                { return estado; }

    // ── Utilitários ──────────────────────────────────────────────────────────

    @Override
    public String toString() {
        String dev = (dataEfetivaDevolucao != null) ? dataEfetivaDevolucao.toString() : "pendente";
        return String.format(
                "Empréstimo #%d | %s | Exemplar #%d (\"%s\") | "
                + "Início: %s | Prev: %s | Dev: %s | %s",
                id,
                utilizador.getNomeCompleto(),
                exemplar.getExemplarId(),
                exemplar.getLivro().getTitulo(),
                dataInicio, dataPrevistaDevolucao, dev,
                estado.getDescricao());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Emprestimo)) return false;
        return this.id == ((Emprestimo) obj).id;
    }

    @Override
    public int hashCode() { return Integer.hashCode(id); }
}
