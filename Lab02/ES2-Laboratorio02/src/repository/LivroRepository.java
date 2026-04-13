/*
 * Repositório de Livros — encapsula operações SQL.
 */
package repository;

import data.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import models.Autor;
import models.Livro;
import enums.TipoLivro;

/**
 * Repositório para operações sobre Livro e leitura do Autor associado.
 *
 * @author Emanuel
 */
public class LivroRepository {

    public void save(Livro livro) throws SQLException {
        String sql = "INSERT INTO livros (livro_id, isbn, titulo, ano_publicacao, tipo, autor_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, livro.getLivroId());
            ps.setString(2, livro.getIsbn());
            ps.setString(3, livro.getTitulo());
            ps.setInt(4, livro.getAnoPublicacao());
            ps.setString(5, livro.getTipo().name());
            ps.setInt(6, livro.getAutor().getId());
            ps.executeUpdate();
        }
    }

    public Livro findByIsbn(String isbn) throws SQLException {
        String sql = "SELECT l.livro_id, l.isbn, l.titulo, l.ano_publicacao, l.tipo, "
                   + "a.id AS autor_id, a.nome, a.nacionalidade "
                   + "FROM livros l JOIN autores a ON l.autor_id = a.id "
                   + "WHERE l.isbn = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Autor autor = new Autor(
                        rs.getInt("autor_id"),
                        rs.getString("nome"),
                        rs.getString("nacionalidade")
                );
                return new Livro(
                        rs.getInt("livro_id"),
                        rs.getString("isbn"),
                        rs.getString("titulo"),
                        rs.getInt("ano_publicacao"),
                        TipoLivro.valueOf(rs.getString("tipo")),
                        autor
                );
            }
        }
        return null;
    }

    public List<Livro> findAll() throws SQLException {
        String sql = "SELECT l.livro_id, l.isbn, l.titulo, l.ano_publicacao, l.tipo, "
                   + "a.id AS autor_id, a.nome, a.nacionalidade "
                   + "FROM livros l JOIN autores a ON l.autor_id = a.id ORDER BY l.livro_id";
        List<Livro> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Autor autor = new Autor(
                        rs.getInt("autor_id"),
                        rs.getString("nome"),
                        rs.getString("nacionalidade")
                );
                list.add(new Livro(
                        rs.getInt("livro_id"),
                        rs.getString("isbn"),
                        rs.getString("titulo"),
                        rs.getInt("ano_publicacao"),
                        TipoLivro.valueOf(rs.getString("tipo")),
                        autor
                ));
            }
        }
        return list;
    }
}
