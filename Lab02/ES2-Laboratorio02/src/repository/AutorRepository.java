/*
 * Repositório de Autores — encapsula operações SQL.
 */
package repository;

import data.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import models.Autor;

/**
 * Repositório para operações sobre Autor.
 *
 * @author Emanuel
 */
public class AutorRepository {

    public Autor findById(int id) throws SQLException {
        String sql = "SELECT id, nome, nacionalidade FROM autores WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Autor(rs.getInt("id"), rs.getString("nome"), rs.getString("nacionalidade"));
        }
        return null;
    }

    public void save(Autor autor) throws SQLException {
        String sql = "INSERT INTO autores (id, nome, nacionalidade) VALUES (?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, autor.getId());
            ps.setString(2, autor.getNome());
            ps.setString(3, autor.getNacionalidade());
            ps.executeUpdate();
        }
    }
}
