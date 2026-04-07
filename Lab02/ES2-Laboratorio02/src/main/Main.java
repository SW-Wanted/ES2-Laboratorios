/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package main;

import enums.*;
import models.*;
import services.BibliotecaService;
import java.time.LocalDate;
import java.util.Scanner;

/**
 * @brief  Ponto de entrada do Sistema de Biblioteca Academica do ISPTEC.
 *
 * Apresenta um menu interactivo que permite gerir em tempo real
 * livros, exemplares, utilizadores e emprestimos.
 * Toda a logica de negocio e delegada ao {@link BibliotecaService}.
 *
 * @author Emanuel
 */
public class Main {

    /** Servico central partilhado por todos os metodos do menu. */
    private static final BibliotecaService biblioteca = new BibliotecaService();

    /** Leitor de entrada do utilizador, partilhado e reutilizado. */
    private static final Scanner scanner = new Scanner(System.in);

    // =========================================================================
    // PONTO DE ENTRADA
    // =========================================================================

    /**
     * @brief  Inicializa o sistema e lança o ciclo principal do menu.
     * @param  args  Argumentos da linha de comandos (nao utilizados).
     */
    public static void main(String[] args) {
        separador("SISTEMA DE BIBLIOTECA ACADEMICA - ISPTEC");
        carregarDadosIniciais();
        cicloMenu();
        scanner.close();
        separador("SESSAO ENCERRADA");
    }

    // =========================================================================
    // CICLO DO MENU
    // =========================================================================

    /**
     * @brief  Executa o ciclo principal do menu ate o utilizador escolher sair.
     */
    private static void cicloMenu() {
        boolean continuar = true;
        while (continuar) {
            imprimirMenu();
            int opcao = lerInteiro("Opcao: ");
            continuar = processarOpcao(opcao);
        }
    }

    /**
     * @brief  Imprime as opcoes disponiveis no menu principal.
     */
    private static void imprimirMenu() {
        System.out.println("\n┌─────────────────────────────────────┐");
        System.out.println("│           MENU PRINCIPAL            │");
        System.out.println("├─────────────────────────────────────┤");
        System.out.println("│  1. Registar utilizador             │");
        System.out.println("│  2. Registar livro                  │");
        System.out.println("│  3. Registar exemplar               │");
        System.out.println("│  4. Realizar emprestimo             │");
        System.out.println("│  5. Devolver livro                  │");
        System.out.println("│  6. Ver disponibilidade de livro    │");
        System.out.println("│  7. Ver historico de utilizador     │");
        System.out.println("│  8. Ver historico geral             │");
        System.out.println("│  9. Listar todos os exemplares      │");
        System.out.println("│  0. Sair                            │");
        System.out.println("└─────────────────────────────────────┘");
    }

    /**
     * @brief   Encaminha a opcao escolhida para o metodo correspondente.
     * @param   opcao  Numero da opcao seleccionada pelo utilizador.
     * @return  {@code false} se o utilizador escolheu sair, {@code true} caso contrario.
     */
    private static boolean processarOpcao(int opcao) {
        switch (opcao) {
            case 1 -> menuRegistarUtilizador();
            case 2 -> menuRegistarLivro();
            case 3 -> menuRegistarExemplar();
            case 4 -> menuRealizarEmprestimo();
            case 5 -> menuDevolverLivro();
            case 6 -> menuVerDisponibilidade();
            case 7 -> menuHistoricoUtilizador();
            case 8 -> biblioteca.imprimirHistoricoGeral();
            case 9 -> biblioteca.imprimirTodosExemplares();
            case 0 -> { return false; }
            default -> System.out.println("  ⚠  Opcao invalida. Escolha entre 0 e 9.");
        }
        return true;
    }

    // =========================================================================
    // OPCOES DO MENU
    // =========================================================================

    /**
     * @brief  Recolhe os dados e regista um novo utilizador no sistema.
     */
    private static void menuRegistarUtilizador() {
        titulo("REGISTAR UTILIZADOR");
        String id            = lerTexto("ID (ex: U004): ");
        String nome          = lerTexto("Nome completo: ");
        String contacto      = lerTexto("Contacto: ");
        TipoUtilizador tipo  = escolherTipoUtilizador();
        biblioteca.registarUtilizador(new Utilizador(id, nome, contacto, tipo));
    }

    /**
     * @brief  Recolhe os dados e regista um novo livro no sistema.
     */
    private static void menuRegistarLivro() {
        titulo("REGISTAR LIVRO");
        int       id     = lerInteiro("ID do livro: ");
        String    isbn   = lerTexto("ISBN: ");
        String    titulo = lerTexto("Titulo: ");
        int       ano    = lerInteiro("Ano de publicacao: ");
        TipoLivro tipo   = escolherTipoLivro();
        Autor     autor  = recolherAutor();
        biblioteca.registarLivro(new Livro(id, isbn, titulo, ano, tipo, autor));
    }

    /**
     * @brief  Lista os livros existentes, recolhe o ISBN e regista um novo exemplar.
     */
    private static void menuRegistarExemplar() {
        titulo("REGISTAR EXEMPLAR");
        biblioteca.imprimirTodosLivros();
        String isbn  = lerTexto("ISBN do livro: ");
        Livro  livro = biblioteca.buscarLivroPorIsbn(isbn);
        if (livro == null) { System.out.println("  ✖  Livro nao encontrado."); return; }
        int       id   = lerInteiro("ID do exemplar: ");
        LocalDate data = lerData("Data de aquisicao (AAAA-MM-DD): ");
        biblioteca.registarExemplar(new Exemplar(id, livro, data));
    }

    /**
     * @brief  Lista utilizadores e livros, recolhe as escolhas e regista um emprestimo.
     */
    private static void menuRealizarEmprestimo() {
        titulo("REALIZAR EMPRÉSTIMO");
        biblioteca.imprimirTodosUtilizadores();
        Utilizador utilizador = buscarUtilizadorComFeedback();
        if (utilizador == null) return;
        biblioteca.imprimirTodosLivros();
        Livro livro = buscarLivroComFeedback();
        if (livro == null) return;
        biblioteca.registarEmprestimo(utilizador, livro);
    }

    /**
     * @brief  Lista o historico geral, recolhe o ID e processa a devolucao.
     */
    private static void menuDevolverLivro() {
        titulo("DEVOLVER LIVRO");
        biblioteca.imprimirHistoricoGeral();
        int        id         = lerInteiro("ID do emprestimo a devolver: ");
        Emprestimo emprestimo = biblioteca.buscarEmprestimoPorId(id);
        if (emprestimo == null) { System.out.println("  ✖  Emprestimo nao encontrado."); return; }
        biblioteca.devolverLivro(emprestimo);
    }

    /**
     * @brief  Lista os livros, recolhe o ISBN e apresenta a disponibilidade de exemplares.
     */
    private static void menuVerDisponibilidade() {
        titulo("DISPONIBILIDADE");
        biblioteca.imprimirTodosLivros();
        Livro livro = buscarLivroComFeedback();
        if (livro == null) return;
        biblioteca.imprimirDisponibilidade(livro);
    }

    /**
     * @brief  Lista os utilizadores, recolhe o ID e apresenta o historico de emprestimos.
     */
    private static void menuHistoricoUtilizador() {
        titulo("HISTORICO DE UTILIZADOR");
        biblioteca.imprimirTodosUtilizadores();
        Utilizador utilizador = buscarUtilizadorComFeedback();
        if (utilizador == null) return;
        biblioteca.imprimirHistoricoUtilizador(utilizador);
    }

    // =========================================================================
    // AUXILIARES DE PESQUISA COM FEEDBACK
    // =========================================================================

    /**
     * @brief   Le um ID, procura o utilizador e imprime mensagem se nao existir.
     * @return  O {@link Utilizador} encontrado, ou {@code null} se nao existir.
     */
    private static Utilizador buscarUtilizadorComFeedback() {
        String     id         = lerTexto("ID do utilizador: ");
        Utilizador utilizador = biblioteca.buscarUtilizadorPorId(id);
        if (utilizador == null) System.out.println("  ✖  Utilizador nao encontrado.");
        return utilizador;
    }

    /**
     * @brief   Le um ISBN, procura o livro e imprime mensagem se nao existir.
     * @return  O {@link Livro} encontrado, ou {@code null} se nao existir.
     */
    private static Livro buscarLivroComFeedback() {
        String isbn  = lerTexto("ISBN do livro: ");
        Livro  livro = biblioteca.buscarLivroPorIsbn(isbn);
        if (livro == null) System.out.println("  ✖  Livro nao encontrado.");
        return livro;
    }

    // =========================================================================
    // SELECCAO DE ENUMS
    // =========================================================================

    /**
     * @brief   Apresenta as opcoes de {@link TipoUtilizador} e devolve a escolha.
     * @return  O {@link TipoUtilizador} seleccionado; {@code ESTUDANTE} por omissao.
     */
    private static TipoUtilizador escolherTipoUtilizador() {
        System.out.println("  Tipo:  1 - Estudante  |  2 - Docente");
        return (lerInteiro("Opcao: ") == 2) ? TipoUtilizador.DOCENTE : TipoUtilizador.ESTUDANTE;
    }

    /**
     * @brief   Apresenta as opcoes de {@link TipoLivro} e devolve a escolha.
     * @return  O {@link TipoLivro} seleccionado; {@code TECNICO} por omissao.
     */
    private static TipoLivro escolherTipoLivro() {
        System.out.println("  Tipo:  1 - Tecnico  |  2 - Cientifico");
        return (lerInteiro("Opcao: ") == 2) ? TipoLivro.CIENTIFICO : TipoLivro.TECNICO;
    }

    /**
     * @brief   Recolhe os dados de um autor directamente do utilizador.
     * @return  Um novo objecto {@link Autor} com os dados introduzidos.
     */
    private static Autor recolherAutor() {
        int    id            = lerInteiro("ID do autor: ");
        String nome          = lerTexto("Nome do autor: ");
        String nacionalidade = lerTexto("Nacionalidade: ");
        return new Autor(id, nome, nacionalidade);
    }

    // =========================================================================
    // DADOS INICIAIS DE DEMONSTRACAO
    // =========================================================================

    /**
     * @brief  Carrega um conjunto de dados de demonstracao no sistema.
     *
     * Permite explorar o menu imediatamente sem registar dados de raiz.
     * Os dados reflectem o cenario descrito no enunciado do laboratorio.
     */
    private static void carregarDadosIniciais() {
        titulo("A CARREGAR DADOS DE DEMONSTRACAO");
        carregarAutoresELivros();
        carregarExemplares();
        carregarUtilizadores();
        System.out.println("\n  ✔  Dados de demonstracao carregados com sucesso.");
    }

    /**
     * @brief  Cria e regista os autores e livros de demonstracao.
     */
    private static void carregarAutoresELivros() {
        Autor a1 = new Autor(1, "Robert C. Martin",  "Americano");
        Autor a2 = new Autor(2, "Gang of Four",       "Internacional");
        Autor a3 = new Autor(3, "Thomas H. Cormen",   "Americano");
        Autor a4 = new Autor(4, "Edsger W. Dijkstra", "Holandês");

        biblioteca.registarLivro(new Livro(1, "978-0-13-468599-1",
                "Clean Code", 2008, TipoLivro.TECNICO, a1));
        biblioteca.registarLivro(new Livro(2, "978-0-20-163361-5",
                "Design Patterns", 1994, TipoLivro.TECNICO, a2));
        biblioteca.registarLivro(new Livro(3, "978-0-26-203293-3",
                "Introduction to Algorithms", 2009, TipoLivro.CIENTIFICO, a3));
        biblioteca.registarLivro(new Livro(4, "978-0-13-814900-0",
                "A Discipline of Programming", 1976, TipoLivro.CIENTIFICO, a4));
    }

    /**
     * @brief  Cria e regista os exemplares físicos de demonstracao.
     *
     * Busca cada livro pelo ISBN para não depender de variáveis locais
     * externas, mantendo o método coeso e independente.
     */
    private static void carregarExemplares() {
        Livro l1 = biblioteca.buscarLivroPorIsbn("978-0-13-468599-1");
        Livro l2 = biblioteca.buscarLivroPorIsbn("978-0-20-163361-5");
        Livro l3 = biblioteca.buscarLivroPorIsbn("978-0-26-203293-3");
        Livro l4 = biblioteca.buscarLivroPorIsbn("978-0-13-814900-0");

        biblioteca.registarExemplar(new Exemplar(1, l1, LocalDate.of(2020, 3,  15)));
        biblioteca.registarExemplar(new Exemplar(2, l1, LocalDate.of(2021, 6,  10)));
        biblioteca.registarExemplar(new Exemplar(3, l2, LocalDate.of(2019, 11,  5)));
        biblioteca.registarExemplar(new Exemplar(4, l3, LocalDate.of(2022, 1,  20)));
        biblioteca.registarExemplar(new Exemplar(5, l3, LocalDate.of(2022, 1,  20)));
        biblioteca.registarExemplar(new Exemplar(6, l3, LocalDate.of(2023, 8,   1)));
        biblioteca.registarExemplar(new Exemplar(7, l4, LocalDate.of(2018, 5,  30)));
    }

    /**
     * @brief  Cria e regista os utilizadores de demonstracao.
     */
    private static void carregarUtilizadores() {
        biblioteca.registarUtilizador(new Utilizador(
                "U001", "Emanuel dos Santos", "923 000 001", TipoUtilizador.ESTUDANTE));
        biblioteca.registarUtilizador(new Utilizador(
                "U002", "Joao Fernandes",     "923 000 002", TipoUtilizador.ESTUDANTE));
        biblioteca.registarUtilizador(new Utilizador(
                "U003", "Prof. Ana Lopes",    "923 000 003", TipoUtilizador.DOCENTE));
    }

    // =========================================================================
    // UTILITARIOS DE LEITURA
    // =========================================================================

    /**
     * @brief   Le uma linha de texto da entrada do utilizador.
     * @param   prompt  Mensagem apresentada antes da leitura.
     * @return  A string introduzida, sem espacos no inicio e no fim.
     */
    private static String lerTexto(String prompt) {
        System.out.print("  " + prompt);
        return scanner.nextLine().trim();
    }

    /**
     * @brief   Le um numero inteiro da entrada, repetindo ate ser valido.
     * @param   prompt  Mensagem apresentada antes da leitura.
     * @return  O inteiro introduzido pelo utilizador.
     */
    private static int lerInteiro(String prompt) {
        while (true) {
            try {
                System.out.print("  " + prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  ⚠  Introduza um numero inteiro valido.");
            }
        }
    }

    /**
     * @brief   Le uma data no formato AAAA-MM-DD, repetindo ate ser valida.
     * @param   prompt  Mensagem apresentada antes da leitura.
     * @return  O objecto {@link LocalDate} correspondente a data introduzida.
     */
    private static LocalDate lerData(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(lerTexto(prompt));
            } catch (Exception e) {
                System.out.println("  ⚠  Formato invalido. Use AAAA-MM-DD (ex: 2024-03-15).");
            }
        }
    }

    // =========================================================================
    // UTILITARIOS DE FORMATACAO
    // =========================================================================

    /**
     * @brief  Imprime uma linha separadora com o titulo centrado.
     * @param  texto  Texto a apresentar no separador.
     */
    private static void separador(String texto) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.printf( "║  %-52s║%n", texto);
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    /**
     * @brief  Imprime um titulo de secao com linha decorativa.
     * @param  texto  Texto do titulo da secao.
     */
    private static void titulo(String texto) {
        System.out.println("\n── " + texto + " " + "─".repeat(Math.max(0, 50 - texto.length())));
    }
}
