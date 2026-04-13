/*
 * Repositório de Empréstimos — encapsula operações SQL.
 */
package repository;

import data.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import models.Emprestimo;
import models.Utilizador;
import models.Exemplar;
import enums.EstadoEmprestimo;

/**
 * Repositório para operações sobre emprestimos.
 *
 * @author Emanuel
 */
public class EmprestimoRepository {

    public int save(String utilizadorId, int exemplarId, LocalDate inicio, LocalDate devolucao) throws SQLException {
        String sql = "INSERT INTO emprestimos (utilizador_id, exemplar_id, data_inicio, data_prevista_devolucao, estado) VALUES (?, ?, ?, ?, 'ATIVO')";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, utilizadorId);
            ps.setInt(2, exemplarId);
            ps.setDate(3, java.sql.Date.valueOf(inicio));
            ps.setDate(4, java.sql.Date.valueOf(devolucao));
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            return keys.getInt(1);
        }
    }

    public Emprestimo findById(int id) throws SQLException {
        String sql = "SELECT e.id, e.utilizador_id, e.exemplar_id, e.data_inicio, "
                   + "e.data_prevista_devolucao, e.data_efetiva_devolucao, e.estado "
                   + "FROM emprestimos e WHERE e.id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Utilizador u = new UtilizadorRepository().findById(rs.getString("utilizador_id"));
                Exemplar ex = new ExemplarRepository().findById(rs.getInt("exemplar_id"));
                java.sql.Date dataEfetiva = rs.getDate("data_efetiva_devolucao");
                LocalDate devEf = (dataEfetiva != null) ? dataEfetiva.toLocalDate() : null;
                return new Emprestimo(
                        rs.getInt("id"),
                        u,
                        ex,
                        rs.getDate("data_inicio").toLocalDate(),
                        rs.getDate("data_prevista_devolucao").toLocalDate(),
                        devEf,
                        EstadoEmprestimo.valueOf(rs.getString("estado"))
                );
            }
        }
        return null;
    }

    public long countActiveByUtilizador(String utilizadorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM emprestimos WHERE utilizador_id = ? AND estado IN ('ATIVO','ATRASADO')";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, utilizadorId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getLong(1);
        }
    }

    public void marcarDevolvido(int emprestimoId, LocalDate dataEfetiva) throws SQLException {
        String sql = "UPDATE emprestimos SET estado = 'DEVOLVIDO', data_efetiva_devolucao = ? WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(dataEfetiva));
            ps.setInt(2, emprestimoId);
            ps.executeUpdate();
        }
    }

    public List<Emprestimo> findAll() throws SQLException {
        String sql = "SELECT id, utilizador_id, exemplar_id, data_inicio, data_prevista_devolucao, data_efetiva_devolucao, estado FROM emprestimos ORDER BY id";
        List<Emprestimo> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(findById(rs.getInt("id")));
            }
        }
        return list;
    }

    public List<Emprestimo> findByUtilizadorId(String utilizadorId) throws SQLException {
        String sql = "SELECT id FROM emprestimos WHERE utilizador_id = ? ORDER BY id";
        List<Emprestimo> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, utilizadorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(findById(rs.getInt("id")));
        }
        return list;
    }
}
