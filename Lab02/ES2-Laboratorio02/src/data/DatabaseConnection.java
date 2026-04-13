/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @brief  Gere a ligação única ao banco de dados MySQL.
 *
 * Implementa o padrão Singleton para garantir que existe
 * apenas uma ligação activa durante todo o ciclo de vida
 * da aplicação, evitando o custo de abrir/fechar ligações
 * repetidamente.
 *
 * Configuração necessária: ajuste as constantes URL, USER e
 * PASSWORD antes de executar o projecto.
 * 
 * @author Emanuel dos Santos
 */
public class DatabaseConnection {

    // ── Configuração da ligação ───────────────────────────────────────────────

    /** URL JDBC do servidor MySQL. Altere o host/porta se necessário. */
    private static final String URL  = "jdbc:mysql://localhost:3306/db_biblioteca"
                                     + "?useSSL=false"
                                     + "&allowPublicKeyRetrieval=true"
                                     + "&serverTimezone=UTC"
                                     + "&characterEncoding=UTF-8";

    /** Utilizador do MySQL. */
    private static final String USER = "root";

    /** Password do MySQL. Substitua pela sua password real. */
    private static final String PASSWORD = "admin";

    // ── Instância Singleton ───────────────────────────────────────────────────

    /** Instância única mantida em memória. */
    private static Connection instancia = null;

    /** Construtor privado — impede instanciação directa. */
    private DatabaseConnection() {}

    // ── Métodos públicos ──────────────────────────────────────────────────────

    /**
     * @brief   Devolve a ligação activa, criando-a se ainda não existir
     *          ou se tiver sido encerrada.
     * @return  A {@link Connection} activa ao banco de dados.
     * @throws  SQLException se não for possível estabelecer a ligação.
     */
    public static Connection getConnection() throws SQLException {
        if (instancia == null || instancia.isClosed()) {
            instancia = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("  [DB] Ligação ao MySQL estabelecida.");
        }
        return instancia;
    }

    /**
     * @brief  Encerra a ligação ao banco de dados, se estiver aberta.
     *         Deve ser chamado quando a aplicação termina.
     */
    public static void fechar() {
        try {
            if (instancia != null && !instancia.isClosed()) {
                instancia.close();
                System.out.println("  [DB] Ligação ao MySQL encerrada.");
            }
        } catch (SQLException e) {
            System.err.println("  [DB] Erro ao encerrar ligação: " + e.getMessage());
        }
    }
}

