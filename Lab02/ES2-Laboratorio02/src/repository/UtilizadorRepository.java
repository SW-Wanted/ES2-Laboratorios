/*
 * Repositório de Utilizadores — encapsula operações SQL.
 */
package repository;

import data.DatabaseConnection;
import enums.TipoUtilizador;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import models.Utilizador;

/**
 * Repositório para operações CRUD sobre Utilizador.
 *
 * Lance SQLException em caso de erro — o serviço trata mensagens.
 *
 * @author Emanuel
 */
public class UtilizadorRepository {

    public void save(Utilizador u) throws SQLException {
        String sql = "INSERT INTO utilizadores (id, nome_completo, contacto, tipo) VALUES (?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getId());
            ps.setString(2, u.getNomeCompleto());
            ps.setString(3, u.getContacto());
            ps.setString(4, u.getTipo().name());
            ps.executeUpdate();
        }
    }

    public Utilizador findById(String id) throws SQLException {
        String sql = "SELECT id, nome_completo, contacto, tipo FROM utilizadores WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Utilizador(
                        rs.getString("id"),
                        rs.getString("nome_completo"),
                        rs.getString("contacto"),
                        TipoUtilizador.valueOf(rs.getString("tipo"))
                );
            }
        }
        return null;
    }

    public boolean existsById(String id) throws SQLException {
        String sql = "SELECT 1 FROM utilizadores WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeQuery().next();
        }
    }

    public List<Utilizador> findAll() throws SQLException {
        String sql = "SELECT id, nome_completo, contacto, tipo FROM utilizadores ORDER BY id";
        List<Utilizador> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Utilizador(
                        rs.getString("id"),
                        rs.getString("nome_completo"),
                        rs.getString("contacto"),
                        TipoUtilizador.valueOf(rs.getString("tipo"))
                ));
            }
        }
        return list;
    }
}
