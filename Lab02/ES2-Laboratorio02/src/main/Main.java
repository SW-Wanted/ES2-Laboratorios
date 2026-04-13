/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package main;

import enums.*;
import models.*;
import services.BibliotecaService;
import data.DatabaseConnection;
import java.time.LocalDate;
import java.util.Scanner;

/**
 * @brief  Ponto de entrada do Sistema de Biblioteca Académica do ISPTEC.
 *
 * Apresenta um menu interactivo que permite gerir em tempo real
 * livros, exemplares, utilizadores e empréstimos.
 * Os dados são persistidos em MySQL através do {@link BibliotecaService},
 * sobrevivendo ao encerramento da aplicação.
 *
 * @author Emanuel dos Santos
 */
public class Main {

    /** Serviço central partilhado por todos os métodos do menu. */
    private static final BibliotecaService biblioteca = new BibliotecaService();

    /** Leitor de entrada do utilizador, partilhado e reutilizado. */
    private static final Scanner scanner = new Scanner(System.in);

    // =========================================================================
    // PONTO DE ENTRADA
    // =========================================================================

    /**
     * @brief  Inicializa o sistema, lança o ciclo do menu e encerra a ligação.
     * @param  args  Argumentos da linha de comandos (não utilizados).
     */
    public static void main(String[] args) {
        separador("SISTEMA DE BIBLIOTECA ACADEMICA - ISPTEC");
        cicloMenu();
        scanner.close();
        DatabaseConnection.fechar();
        separador("SESSAO ENCERRADA");
    }

    // =========================================================================
    // CICLO DO MENU
    // =========================================================================

    /**
     * @brief  Executa o ciclo principal do menu até o utilizador escolher sair.
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
     * @brief  Imprime as opções disponíveis no menu principal.
     */
    private static void imprimirMenu() {
        System.out.println("\n+-------------------------------------+");
        System.out.println("|           MENU PRINCIPAL            |");
        System.out.println("+-------------------------------------+");
        System.out.println("|  1. Registar utilizador             |");
        System.out.println("|  2. Registar livro                  |");
        System.out.println("|  3. Registar exemplar               |");
        System.out.println("|  4. Realizar emprestimo             |");
        System.out.println("|  5. Devolver livro                  |");
        System.out.println("|  6. Ver disponibilidade de livro    |");
        System.out.println("|  7. Ver historico de utilizador     |");
        System.out.println("|  8. Ver historico geral             |");
        System.out.println("|  9. Listar todos os exemplares      |");
        System.out.println("|  0. Sair                            |");
        System.out.println("+-------------------------------------+");
    }

    /**
     * @brief   Encaminha a opção escolhida para o método correspondente.
     * @param   opcao  Número da opção seleccionada pelo utilizador.
     * @return  {@code false} se o utilizador escolheu sair, {@code true} caso contrário.
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
            default -> System.out.println("  Opcao invalida. Escolha entre 0 e 9.");
        }
        return true;
    }

    // =========================================================================
    // OPCOES DO MENU
    // =========================================================================

    /**
     * @brief  Recolhe os dados e regista um novo utilizador na base de dados.
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
     * @brief  Recolhe os dados e regista um novo livro e autor na base de dados.
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
        if (livro == null) { System.out.println("  Livro nao encontrado."); return; }
        int       id   = lerInteiro("ID do exemplar: ");
        LocalDate data = lerData("Data de aquisicao (AAAA-MM-DD): ");
        biblioteca.registarExemplar(new Exemplar(id, livro, data));
    }

    /**
     * @brief  Lista utilizadores e livros, recolhe as escolhas e regista um empréstimo.
     */
    private static void menuRealizarEmprestimo() {
        titulo("REALIZAR EMPRESTIMO");
        biblioteca.imprimirTodosUtilizadores();
        Utilizador utilizador = buscarUtilizadorComFeedback();
        if (utilizador == null) return;
        biblioteca.imprimirTodosLivros();
        Livro livro = buscarLivroComFeedback();
        if (livro == null) return;
        biblioteca.registarEmprestimo(utilizador, livro);
    }

    /**
     * @brief  Lista o histórico geral, recolhe o ID e processa a devolução.
     */
    private static void menuDevolverLivro() {
        titulo("DEVOLVER LIVRO");
        biblioteca.imprimirHistoricoGeral();
        int        id         = lerInteiro("ID do emprestimo a devolver: ");
        Emprestimo emprestimo = biblioteca.buscarEmprestimoPorId(id);
        if (emprestimo == null) { System.out.println("  Emprestimo nao encontrado."); return; }
        biblioteca.devolverLivro(emprestimo);
    }

    /**
     * @brief  Lista os livros e apresenta a disponibilidade de exemplares do escolhido.
     */
    private static void menuVerDisponibilidade() {
        titulo("DISPONIBILIDADE");
        biblioteca.imprimirTodosLivros();
        Livro livro = buscarLivroComFeedback();
        if (livro == null) return;
        biblioteca.imprimirDisponibilidade(livro);
    }

    /**
     * @brief  Lista os utilizadores e apresenta o histórico do escolhido.
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
     * @brief   Lê um ID, procura o utilizador e imprime mensagem se não existir.
     * @return  O {@link Utilizador} encontrado, ou {@code null} se não existir.
     */
    private static Utilizador buscarUtilizadorComFeedback() {
        String     id         = lerTexto("ID do utilizador: ");
        Utilizador utilizador = biblioteca.buscarUtilizadorPorId(id);
        if (utilizador == null) System.out.println("  Utilizador nao encontrado.");
        return utilizador;
    }

    /**
     * @brief   Lê um ISBN, procura o livro e imprime mensagem se não existir.
     * @return  O {@link Livro} encontrado, ou {@code null} se não existir.
     */
    private static Livro buscarLivroComFeedback() {
        String isbn  = lerTexto("ISBN do livro: ");
        Livro  livro = biblioteca.buscarLivroPorIsbn(isbn);
        if (livro == null) System.out.println("  Livro nao encontrado.");
        return livro;
    }

    // =========================================================================
    // SELECCAO DE ENUMS
    // =========================================================================

    /**
     * @brief   Apresenta as opções de {@link TipoUtilizador} e devolve a escolha.
     * @return  O {@link TipoUtilizador} seleccionado; {@code ESTUDANTE} por omissão.
     */
    private static TipoUtilizador escolherTipoUtilizador() {
        System.out.println("  Tipo:  1 - Estudante  |  2 - Docente");
        return (lerInteiro("Opcao: ") == 2) ? TipoUtilizador.DOCENTE : TipoUtilizador.ESTUDANTE;
    }

    /**
     * @brief   Apresenta as opções de {@link TipoLivro} e devolve a escolha.
     * @return  O {@link TipoLivro} seleccionado; {@code TECNICO} por omissão.
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
    // UTILITARIOS DE LEITURA
    // =========================================================================

    /**
     * @brief   Lê uma linha de texto da entrada do utilizador.
     * @param   prompt  Mensagem apresentada antes da leitura.
     * @return  A string introduzida, sem espaços no início e no fim.
     */
    private static String lerTexto(String prompt) {
        System.out.print("  " + prompt);
        return scanner.nextLine().trim();
    }

    /**
     * @brief   Lê um número inteiro da entrada, repetindo até ser válido.
     * @param   prompt  Mensagem apresentada antes da leitura.
     * @return  O inteiro introduzido pelo utilizador.
     */
    private static int lerInteiro(String prompt) {
        while (true) {
            try {
                System.out.print("  " + prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  Introduza um numero inteiro valido.");
            }
        }
    }

    /**
     * @brief   Lê uma data no formato AAAA-MM-DD, repetindo até ser válida.
     * @param   prompt  Mensagem apresentada antes da leitura.
     * @return  O objecto {@link LocalDate} correspondente à data introduzida.
     */
    private static LocalDate lerData(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(lerTexto(prompt));
            } catch (Exception e) {
                System.out.println("  Formato invalido. Use AAAA-MM-DD (ex: 2024-03-15).");
            }
        }
    }

    // =========================================================================
    // UTILITARIOS DE FORMATACAO
    // =========================================================================

    /**
     * @brief  Imprime uma linha separadora com o título centrado.
     * @param  texto  Texto a apresentar no separador.
     */
    private static void separador(String texto) {
        System.out.println("\n+======================================================+");
        System.out.printf( "|  %-52s|%n", texto);
        System.out.println("+======================================================+");
    }

    /**
     * @brief  Imprime um título de secção com linha decorativa.
     * @param  texto  Texto do título da secção.
     */
    private static void titulo(String texto) {
        System.out.println("\n-- " + texto + " " + "-".repeat(Math.max(0, 50 - texto.length())));
    }
}
