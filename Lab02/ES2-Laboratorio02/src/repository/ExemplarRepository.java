/*
 * Repositório de Exemplares — encapsula operações SQL.
 */
package repository;

import data.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import models.Exemplar;
import models.Livro;
import models.Autor;
import enums.EstadoExemplar;

/**
 * Repositório para operações sobre exemplares.
 *
 * @author Emanuel
 */
public class ExemplarRepository {

    public void save(Exemplar ex) throws SQLException {
        String sql = "INSERT INTO exemplares (exemplar_id, isbn, livro_id, estado, data_aquisicao) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ex.getExemplarId());
            ps.setString(2, ex.getIsbn());
            ps.setInt(3, ex.getLivro().getLivroId());
            ps.setString(4, ex.getEstado().name());
            ps.setDate(5, java.sql.Date.valueOf(ex.getDataAquisicao()));
            ps.executeUpdate();
        }
    }

    public Exemplar findAvailableByLivroId(int livroId) throws SQLException {
        String sql = "SELECT ex.exemplar_id, ex.isbn, ex.estado, ex.data_aquisicao, "
                   + "l.livro_id, l.titulo, l.ano_publicacao, l.tipo, "
                   + "a.id AS autor_id, a.nome, a.nacionalidade "
                   + "FROM exemplares ex "
                   + "JOIN livros l  ON ex.livro_id = l.livro_id "
                   + "JOIN autores a ON l.autor_id  = a.id "
                   + "WHERE ex.livro_id = ? AND ex.estado = 'DISPONIVEL' LIMIT 1";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, livroId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Autor autor = new Autor(
                        rs.getInt("autor_id"),
                        rs.getString("nome"),
                        rs.getString("nacionalidade")
                );
                Livro livro = new Livro(
                        rs.getInt("livro_id"),
                        rs.getString("isbn"),
                        rs.getString("titulo"),
                        rs.getInt("ano_publicacao"),
                        enums.TipoLivro.valueOf(rs.getString("tipo")),
                        autor
                );
                Exemplar ex = new Exemplar(
                        rs.getInt("exemplar_id"),
                        livro,
                        rs.getDate("data_aquisicao").toLocalDate()
                );
                ex.setEstado(EstadoExemplar.valueOf(rs.getString("estado")));
                return ex;
            }
        }
        return null;
    }

    public void updateEstado(int exemplarId, EstadoExemplar estado) throws SQLException {
        String sql = "UPDATE exemplares SET estado = ? WHERE exemplar_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setInt(2, exemplarId);
            ps.executeUpdate();
        }
    }

    public Exemplar findById(int id) throws SQLException {
        String sql = "SELECT ex.exemplar_id, ex.isbn, ex.estado, ex.data_aquisicao, "
                   + "l.livro_id, l.titulo, l.ano_publicacao, l.tipo, "
                   + "a.id AS autor_id, a.nome, a.nacionalidade "
                   + "FROM exemplares ex "
                   + "JOIN livros l  ON ex.livro_id = l.livro_id "
                   + "JOIN autores a ON l.autor_id  = a.id "
                   + "WHERE ex.exemplar_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Autor autor = new Autor(
                        rs.getInt("autor_id"),
                        rs.getString("nome"),
                        rs.getString("nacionalidade")
                );
                Livro livro = new Livro(
                        rs.getInt("livro_id"),
                        rs.getString("isbn"),
                        rs.getString("titulo"),
                        rs.getInt("ano_publicacao"),
                        enums.TipoLivro.valueOf(rs.getString("tipo")),
                        autor
                );
                Exemplar ex = new Exemplar(
                        rs.getInt("exemplar_id"),
                        livro,
                        rs.getDate("data_aquisicao").toLocalDate()
                );
                ex.setEstado(EstadoExemplar.valueOf(rs.getString("estado")));
                return ex;
            }
        }
        return null;
    }

    public long countAvailableByLivroId(int livroId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM exemplares WHERE livro_id = ? AND estado = 'DISPONIVEL'";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, livroId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    public List<Exemplar> findAll() throws SQLException {
        String sql = "SELECT ex.exemplar_id, ex.isbn, ex.estado, ex.data_aquisicao, "
                   + "l.livro_id, l.titulo, l.ano_publicacao, l.tipo, "
                   + "a.id AS autor_id, a.nome, a.nacionalidade "
                   + "FROM exemplares ex "
                   + "JOIN livros l  ON ex.livro_id = l.livro_id "
                   + "JOIN autores a ON l.autor_id  = a.id "
                   + "ORDER BY ex.exemplar_id";
        List<Exemplar> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Autor autor = new Autor(
                        rs.getInt("autor_id"),
                        rs.getString("nome"),
                        rs.getString("nacionalidade")
                );
                Livro livro = new Livro(
                        rs.getInt("livro_id"),
                        rs.getString("isbn"),
                        rs.getString("titulo"),
                        rs.getInt("ano_publicacao"),
                        enums.TipoLivro.valueOf(rs.getString("tipo")),
                        autor
                );
                Exemplar ex = new Exemplar(
                        rs.getInt("exemplar_id"),
                        livro,
                        rs.getDate("data_aquisicao").toLocalDate()
                );
                ex.setEstado(EstadoExemplar.valueOf(rs.getString("estado")));
                list.add(ex);
            }
        }
        return list;
    }
}
