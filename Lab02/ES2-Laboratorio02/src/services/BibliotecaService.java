/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import enums.*;
import models.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

/**
 * @brief  Serviço central da biblioteca — orquestra operações.
 *
 * Delegates persistência a classes repository, evitando SQL directo neste serviço.
 * Mantém comentários sumariados e sem comentários dentro de funções.
 *
 * @author Emanuel dos Santos
 */
public class BibliotecaService {

    private final repository.UtilizadorRepository utilizadorRepo = new repository.UtilizadorRepository();
    private final repository.LivroRepository livroRepo = new repository.LivroRepository();
    private final repository.ExemplarRepository exemplarRepo = new repository.ExemplarRepository();
    private final repository.EmprestimoRepository emprestimoRepo = new repository.EmprestimoRepository();
    private final repository.AutorRepository autorRepo = new repository.AutorRepository();


    // =========================================================================
    // REGISTO DE ENTIDADES
    // =========================================================================

    /**
     * @brief  Insere um novo utilizador na base de dados.
     * @param  utilizador  O objecto Utilizador a persistir.
     */
    public void registarUtilizador(Utilizador utilizador) {
        try {
            utilizadorRepo.save(utilizador);
            System.out.println("  [OK] Utilizador registado: " + utilizador);
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("  [ERR] ID \"" + utilizador.getId() + "\" já existe.");
        } catch (SQLException e) {
            System.err.println("  [DB] Erro ao registar utilizador: " + e.getMessage());
        }
    }

    /**
     * @brief  Insere um novo livro e o seu autor na base de dados.
     *         Se o autor ainda não existir, é criado automaticamente.
     * @param  livro  O objecto Livro a persistir.
     */
    public void registarLivro(Livro livro) {
        try {
            // garante autor
            Autor existente = autorRepo.findById(livro.getAutor().getId());
            if (existente == null) autorRepo.save(livro.getAutor());
            livroRepo.save(livro);
            System.out.println("  [OK] Livro registado: " + livro);
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("  [ERR] ISBN \"" + livro.getIsbn() + "\" já existe.");
        } catch (SQLException e) {
            System.err.println("  [DB] Erro ao registar livro: " + e.getMessage());
        }
    }

    /**
     * @brief  Insere um novo exemplar físico na base de dados.
     * @param  exemplar  O objecto Exemplar a persistir.
     */
    public void registarExemplar(Exemplar exemplar) {
        try {
            exemplarRepo.save(exemplar);
            System.out.println("  [OK] Exemplar registado: " + exemplar);
        } catch (SQLException e) {
            System.err.println("  [DB] Erro ao registar exemplar: " + e.getMessage());
        }
    }

    // =========================================================================
    // OPERAÇÃO PRINCIPAL: REGISTAR EMPRÉSTIMO
    // =========================================================================

    /**
     * @brief   Regista um empréstimo após validar todas as regras de negócio.
     *
     * Regras verificadas:
     *  1. Utilizador registado no sistema
     *  2. Utilizador com menos de 3 empréstimos activos
     *  3. Existe exemplar disponível do livro pretendido
     *
     * @param   utilizador  O utilizador que requisita o livro.
     * @param   livro       O livro pretendido.
     * @return  O {@link Emprestimo} criado, ou {@code null} se alguma regra falhar.
     */
    public Emprestimo registarEmprestimo(Utilizador utilizador, Livro livro) {
        System.out.println("\n  -> Tentativa de empréstimo: " + utilizador.getNomeCompleto() + " | \"" + livro.getTitulo() + "\"");
        try {
            if (!utilizadorRepo.existsById(utilizador.getId())) {
                System.out.println("  [ERR] Utilizador não registado no sistema."); return null;
            }
            long activos = emprestimoRepo.countActiveByUtilizador(utilizador.getId());
            if (activos >= Utilizador.MAX_EMPRESTIMOS_ATIVOS) {
                System.out.println("  [ERR] Limite de " + Utilizador.MAX_EMPRESTIMOS_ATIVOS + " empréstimos activos atingido.");
                return null;
            }

            Exemplar exemplar = exemplarRepo.findAvailableByLivroId(livro.getLivroId());
            if (exemplar == null) {
                System.out.println("  [ERR] Sem exemplares disponíveis para \"" + livro.getTitulo() + "\".");
                return null;
            }

            exemplarRepo.updateEstado(exemplar.getExemplarId(), EstadoExemplar.EMPRESTADO);

            LocalDate inicio    = LocalDate.now();
            LocalDate devolucao = inicio.plusDays(Emprestimo.PRAZO_DIAS);
            int novoId = emprestimoRepo.save(utilizador.getId(), exemplar.getExemplarId(), inicio, devolucao);

            exemplar.setEstado(EstadoExemplar.EMPRESTADO);
            Emprestimo emp = new Emprestimo(novoId, utilizador, exemplar, inicio, devolucao);
            utilizador.adicionarEmprestimo(emp);

            System.out.println("  [OK] Empréstimo #" + novoId + " registado. Exemplar #" + exemplar.getExemplarId()
                    + " -> " + utilizador.getNomeCompleto() + " | Devolução prevista: " + devolucao);
            return emp;
        } catch (SQLException e) {
            System.err.println("  [DB] Erro ao registar empréstimo: " + e.getMessage());
            return null;
        }
    }

    // =========================================================================
    // OPERAÇÃO PRINCIPAL: DEVOLVER LIVRO
    // =========================================================================

    /**
     * @brief  Processa a devolução de um empréstimo, actualizando o estado
     *         do empréstimo e repondo o exemplar como disponível.
     * @param  emprestimo  O empréstimo a devolver.
     */
    public void devolverLivro(Emprestimo emprestimo) {
        System.out.println("\n  -> Devolução do empréstimo #" + emprestimo.getId() + ":");
        if (emprestimo.getEstado() == EstadoEmprestimo.DEVOLVIDO) {
            System.out.println("  [WARN] Empréstimo #" + emprestimo.getId() + " já devolvido.");
            return;
        }
        try {
            emprestimoRepo.marcarDevolvido(emprestimo.getId(), LocalDate.now());
            exemplarRepo.updateEstado(emprestimo.getExemplar().getExemplarId(), EstadoExemplar.DISPONIVEL);

            emprestimo.marcarDevolvido(LocalDate.now());
            emprestimo.getExemplar().setEstado(EstadoExemplar.DISPONIVEL);

            System.out.println("  [OK] Livro \"" + emprestimo.getExemplar().getLivro().getTitulo() + "\" devolvido por "
                    + emprestimo.getUtilizador().getNomeCompleto() + ".");
        } catch (SQLException e) {
            System.err.println("  [DB] Erro na devolução: " + e.getMessage());
        }
    }

    // =========================================================================
    // PESQUISA DE ENTIDADES
    // =========================================================================

    /**
     * @brief   Procura um utilizador pelo ID.
     * @param   id  ID único do utilizador.
     * @return  O {@link Utilizador} encontrado, ou {@code null}.
     */
    public Utilizador buscarUtilizadorPorId(String id) {
        try {
            return utilizadorRepo.findById(id);
        } catch (SQLException e) {
            System.err.println("  [DB] Erro ao buscar utilizador: " + e.getMessage());
            return null;
        }
    }

    /**
     * @brief   Procura um livro pelo ISBN.
     * @param   isbn  ISBN do livro.
     * @return  O {@link Livro} encontrado, ou {@code null}.
     */
    public Livro buscarLivroPorIsbn(String isbn) {
        try {
            return livroRepo.findByIsbn(isbn);
        } catch (SQLException e) {
            System.err.println("  [DB] Erro ao buscar livro: " + e.getMessage());
            return null;
        }
    }

    /**
     * @brief   Procura um empréstimo pelo ID numérico.
     * @param   id  ID do empréstimo.
     * @return  O {@link Emprestimo} encontrado, ou {@code null}.
     */
    public Emprestimo buscarEmprestimoPorId(int id) {
        try {
            return emprestimoRepo.findById(id);
        } catch (SQLException e) {
            System.err.println("  [DB] Erro ao buscar empréstimo: " + e.getMessage());
            return null;
        }
    }

    // =========================================================================
    // VERIFICAÇÃO DE DISPONIBILIDADE
    // =========================================================================

    /**
     * @brief   Conta os exemplares disponíveis de um livro.
     * @param   livro  O livro a verificar.
     * @return  Número de exemplares disponíveis.
     */
    public long verificarDisponibilidade(Livro livro) {
        try {
            return exemplarRepo.countAvailableByLivroId(livro.getLivroId());
        } catch (SQLException e) {
            System.err.println("  [DB] Erro ao verificar disponibilidade: " + e.getMessage());
            return 0;
        }
    }

    // =========================================================================
    // RELATÓRIOS — IMPRESSÃO
    // =========================================================================

    /**
     * @brief  Lista todos os utilizadores registados na base de dados.
     */
    public void imprimirTodosUtilizadores() {
        System.out.println("\n  -- Utilizadores registados --");
        try {
            List<Utilizador> list = utilizadorRepo.findAll();
            if (list.isEmpty()) System.out.println("  (sem utilizadores)");
            else for (Utilizador u : list) System.out.println("  " + u);
        } catch (SQLException e) {
            System.err.println("  [DB] Erro: " + e.getMessage());
        }
    }

    /**
     * @brief  Lista todos os livros registados na base de dados.
     */
    public void imprimirTodosLivros() {
        System.out.println("\n  -- Livros registados --");
        try {
            List<Livro> list = livroRepo.findAll();
            if (list.isEmpty()) System.out.println("  (sem livros)");
            else for (Livro l : list) System.out.println("  " + l);
        } catch (SQLException e) {
            System.err.println("  [DB] Erro: " + e.getMessage());
        }
    }

    /**
     * @brief  Lista todos os exemplares e o seu estado actual.
     */
    public void imprimirTodosExemplares() {
        System.out.println("\n  -- Exemplares --");
        try {
            List<Exemplar> list = exemplarRepo.findAll();
            if (list.isEmpty()) System.out.println("  (sem exemplares)");
            else for (Exemplar e : list) System.out.println("  " + e);
        } catch (SQLException e) {
            System.err.println("  [DB] Erro: " + e.getMessage());
        }
    }

    /**
     * @brief  Lista o histórico completo de todos os empréstimos.
     */
    public void imprimirHistoricoGeral() {
        System.out.println("\n  -- Histórico completo de empréstimos --");
        try {
            List<Emprestimo> list = emprestimoRepo.findAll();
            if (list.isEmpty()) System.out.println("  (sem registos)");
            else for (Emprestimo e : list) System.out.println("  " + e);
        } catch (SQLException e) {
            System.err.println("  [DB] Erro: " + e.getMessage());
        }
    }

    /**
     * @brief  Lista o histórico de empréstimos de um utilizador específico.
     * @param  utilizador  O utilizador cujo histórico se pretende visualizar.
     */
    public void imprimirHistoricoUtilizador(Utilizador utilizador) {
        System.out.println("\n  -- Histórico de " + utilizador.getNomeCompleto() + " --");
        try {
            List<Emprestimo> list = emprestimoRepo.findByUtilizadorId(utilizador.getId());
            if (list.isEmpty()) System.out.println("  (sem empréstimos)");
            else for (Emprestimo e : list) System.out.println("  " + e);
        } catch (SQLException e) {
            System.err.println("  [DB] Erro: " + e.getMessage());
        }
    }

    /**
     * @brief  Imprime a disponibilidade actual de exemplares de um livro.
     * @param  livro  O livro a consultar.
     */
    public void imprimirDisponibilidade(Livro livro) {
        long disp = verificarDisponibilidade(livro);
        System.out.println("  Disponibilidade de \"" + livro.getTitulo() + "\": " + disp + " exemplar(es) disponível(is).");
    }

    // =========================================================================
    // AUXILIARES PRIVADOS — DELEGAÇÕES AOS REPOSITORIES
    // =========================================================================

    // Note: Os métodos que antes executavam SQL directo foram movidos para os
    // repositories. Esta secção permanece leve para manter o serviço focado
    // apenas na orquestração das operações de alto nível.

    // =========================================================================
    // AUXILIARES PRIVADOS — LÓGICA DE NEGÓCIO
    // =========================================================================

    /**
     * @brief   Verifica se um utilizador existe na base de dados.
     * @param   con  Ligação activa.
     * @param   id   ID do utilizador.
     * @return  {@code true} se existir.
     * @throws  SQLException em caso de erro SQL.
     */
    private boolean utilizadorExiste(Connection con, String id) throws SQLException {
        String sql = "SELECT 1 FROM utilizadores WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeQuery().next();
        }
    }

    /**
     * @brief   Verifica se o utilizador pode fazer mais um empréstimo.
     *          Enunciado: "não pode ter mais de 3 empréstimos activos"
     * @param   con  Ligação activa.
     * @param   id   ID do utilizador.
     * @return  {@code true} se ainda tiver capacidade.
     * @throws  SQLException em caso de erro SQL.
     */
    private boolean podeRequisitar(Connection con, String id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM emprestimos "
                   + "WHERE utilizador_id = ? AND estado IN ('ATIVO','ATRASADO')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) < Utilizador.MAX_EMPRESTIMOS_ATIVOS;
        }
    }

    /**
     * @brief   Procura o primeiro exemplar disponível de um livro.
     * @param   con    Ligação activa.
     * @param   livro  O livro pretendido.
     * @return  O {@link Exemplar} disponível, ou {@code null} se não existir.
     * @throws  SQLException em caso de erro SQL.
     */
    private Exemplar buscarExemplarDisponivel(Connection con, Livro livro) throws SQLException {
        String sql = "SELECT ex.exemplar_id, ex.isbn, ex.estado, ex.data_aquisicao, "
                   + "l.livro_id, l.titulo, l.ano_publicacao, l.tipo, "
                   + "a.id AS autor_id, a.nome, a.nacionalidade "
                   + "FROM exemplares ex "
                   + "JOIN livros l  ON ex.livro_id = l.livro_id "
                   + "JOIN autores a ON l.autor_id  = a.id "
                   + "WHERE ex.livro_id = ? AND ex.estado = 'DISPONIVEL' LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, livro.getLivroId());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapearExemplar(rs) : null;
        }
    }

    /**
     * @brief   Actualiza o estado de um exemplar na base de dados.
     * @param   con         Ligação activa.
     * @param   exemplarId  ID do exemplar.
     * @param   estado      Novo estado a aplicar.
     * @throws  SQLException em caso de erro SQL.
     */
    private void atualizarEstadoExemplar(Connection con, int exemplarId,
                                          EstadoExemplar estado) throws SQLException {
        String sql = "UPDATE exemplares SET estado = ? WHERE exemplar_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setInt(2, exemplarId);
            ps.executeUpdate();
        }
    }

    /**
     * @brief   Insere um novo registo de empréstimo e devolve o ID gerado.
     * @param   con         Ligação activa.
     * @param   utilizador  O utilizador do empréstimo.
     * @param   exemplar    O exemplar emprestado.
     * @param   inicio      Data de início.
     * @param   devolucao   Data prevista de devolução.
     * @return  O ID auto-gerado pelo MySQL.
     * @throws  SQLException em caso de erro SQL.
     */
    private int inserirEmprestimo(Connection con, Utilizador utilizador,
                                   Exemplar exemplar, LocalDate inicio,
                                   LocalDate devolucao) throws SQLException {
        String sql = "INSERT INTO emprestimos "
                   + "(utilizador_id, exemplar_id, data_inicio, data_prevista_devolucao, estado) "
                   + "VALUES (?, ?, ?, ?, 'ATIVO')";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, utilizador.getId());
            ps.setInt(2, exemplar.getExemplarId());
            ps.setDate(3, Date.valueOf(inicio));
            ps.setDate(4, Date.valueOf(devolucao));
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            return keys.getInt(1);
        }
    }

    /**
     * @brief   Garante que o autor existe na base de dados, inserindo-o se necessário.
     * @param   con    Ligação activa.
     * @param   autor  O objecto Autor a verificar/inserir.
     * @return  O ID do autor na base de dados.
     * @throws  SQLException em caso de erro SQL.
     */
    private int garantirAutor(Connection con, Autor autor) throws SQLException {
        String sqlSel = "SELECT id FROM autores WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlSel)) {
            ps.setInt(1, autor.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        String sqlIns = "INSERT INTO autores (id, nome, nacionalidade) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sqlIns)) {
            ps.setInt(1, autor.getId());
            ps.setString(2, autor.getNome());
            ps.setString(3, autor.getNacionalidade());
            ps.executeUpdate();
        }
        return autor.getId();
    }

    // =========================================================================
    // AUXILIARES PRIVADOS — MAPEAMENTO ResultSet → Objecto
    // =========================================================================

    /**
     * @brief   Converte a linha actual do ResultSet num objecto {@link Utilizador}.
     * @param   rs  ResultSet posicionado numa linha de utilizadores.
     * @return  O {@link Utilizador} construído.
     * @throws  SQLException em caso de erro SQL.
     */
    private Utilizador mapearUtilizador(ResultSet rs) throws SQLException {
        return new Utilizador(
                rs.getString("id"),
                rs.getString("nome_completo"),
                rs.getString("contacto"),
                TipoUtilizador.valueOf(rs.getString("tipo"))
        );
    }

    /**
     * @brief   Converte a linha actual do ResultSet num objecto {@link Livro}.
     *          O ResultSet deve incluir colunas do JOIN com autores.
     * @param   rs  ResultSet posicionado numa linha do JOIN livros+autores.
     * @return  O {@link Livro} construído.
     * @throws  SQLException em caso de erro SQL.
     */
    private Livro mapearLivro(ResultSet rs) throws SQLException {
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

    /**
     * @brief   Converte a linha actual do ResultSet num objecto {@link Exemplar}.
     *          O ResultSet deve incluir colunas do JOIN com livros e autores.
     * @param   rs  ResultSet posicionado numa linha do JOIN exemplares+livros+autores.
     * @return  O {@link Exemplar} construído.
     * @throws  SQLException em caso de erro SQL.
     */
    private Exemplar mapearExemplar(ResultSet rs) throws SQLException {
        Livro livro = mapearLivro(rs);
        Exemplar ex = new Exemplar(
                rs.getInt("exemplar_id"),
                livro,
                rs.getDate("data_aquisicao").toLocalDate()
        );
        ex.setEstado(EstadoExemplar.valueOf(rs.getString("estado")));
        return ex;
    }

    /**
     * @brief   Converte a linha actual do ResultSet num objecto {@link Emprestimo},
     *          carregando o utilizador e o exemplar associados por sub-queries.
     * @param   rs   ResultSet posicionado numa linha de empréstimos.
     * @param   con  Ligação activa para carregar utilizador e exemplar.
     * @return  O {@link Emprestimo} construído.
     * @throws  SQLException em caso de erro SQL.
     */
    private Emprestimo mapearEmprestimo(ResultSet rs, Connection con) throws SQLException {
        Utilizador utilizador = buscarUtilizadorPorIdCon(con, rs.getString("utilizador_id"));
        Exemplar   exemplar   = buscarExemplarPorIdCon(con, rs.getInt("exemplar_id"));

        Date dataEfetiva = rs.getDate("data_efetiva_devolucao");
        LocalDate devEfetiva = (dataEfetiva != null) ? dataEfetiva.toLocalDate() : null;

        return new Emprestimo(
                rs.getInt("id"),
                utilizador,
                exemplar,
                rs.getDate("data_inicio").toLocalDate(),
                rs.getDate("data_prevista_devolucao").toLocalDate(),
                devEfetiva,
                EstadoEmprestimo.valueOf(rs.getString("estado"))
        );
    }

    /**
     * @brief   Busca utilizador por ID numa ligação já aberta (evita abrir nova).
     * @param   con  Ligação activa.
     * @param   id   ID do utilizador.
     * @return  O {@link Utilizador}, ou {@code null}.
     * @throws  SQLException em caso de erro SQL.
     */
    private Utilizador buscarUtilizadorPorIdCon(Connection con, String id) throws SQLException {
        String sql = "SELECT id, nome_completo, contacto, tipo FROM utilizadores WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapearUtilizador(rs) : null;
        }
    }

    /**
     * @brief   Busca exemplar por ID numa ligação já aberta (evita abrir nova).
     * @param   con  Ligação activa.
     * @param   id   ID do exemplar.
     * @return  O {@link Exemplar}, ou {@code null}.
     * @throws  SQLException em caso de erro SQL.
     */
    private Exemplar buscarExemplarPorIdCon(Connection con, int id) throws SQLException {
        String sql = "SELECT ex.exemplar_id, ex.isbn, ex.estado, ex.data_aquisicao, "
                   + "l.livro_id, l.titulo, l.ano_publicacao, l.tipo, "
                   + "a.id AS autor_id, a.nome, a.nacionalidade "
                   + "FROM exemplares ex "
                   + "JOIN livros l  ON ex.livro_id = l.livro_id "
                   + "JOIN autores a ON l.autor_id  = a.id "
                   + "WHERE ex.exemplar_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapearExemplar(rs) : null;
        }
    }
}
