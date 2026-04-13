-- =============================================================================
-- db_biblioteca.sql
-- Sistema de Biblioteca Académica — ISPTEC
-- Engenharia de Software II | Lab #02
-- @author Emanuel dos Santos
-- =============================================================================
-- Execução:
--   mysql -u root -p < db_biblioteca.sql
--   ou cole directamente no MySQL Workbench / phpMyAdmin
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. BASE DE DADOS
-- -----------------------------------------------------------------------------

DROP DATABASE IF EXISTS db_biblioteca;
CREATE DATABASE db_biblioteca
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE db_biblioteca;

-- -----------------------------------------------------------------------------
-- 2. TABELA: autores
--    Entidade implícita — necessidade de manter autor como entidade própria
--    para reutilização (um autor pode ter vários livros).
-- -----------------------------------------------------------------------------

CREATE TABLE autores (
    id            INT          NOT NULL AUTO_INCREMENT,
    nome          VARCHAR(150) NOT NULL,
    nacionalidade VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);

-- -----------------------------------------------------------------------------
-- 3. TABELA: livros
--    Enunciado: "identificado por um código único (ISBN), título,
--    ano de publicação e autor"
--    Relação: N livros → 1 autor  (ASSOCIAÇÃO)
-- -----------------------------------------------------------------------------

CREATE TABLE livros (
    livro_id       INT          NOT NULL AUTO_INCREMENT,
    isbn           VARCHAR(20)  NOT NULL UNIQUE,
    titulo         VARCHAR(200) NOT NULL,
    ano_publicacao INT          NOT NULL,
    tipo           ENUM('TECNICO','CIENTIFICO') NOT NULL,
    autor_id       INT          NOT NULL,
    PRIMARY KEY (livro_id),
    CONSTRAINT fk_livro_autor FOREIGN KEY (autor_id)
        REFERENCES autores(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- -----------------------------------------------------------------------------
-- 4. TABELA: exemplares
--    Enunciado: "alguns títulos possuem vários exemplares físicos"
--    Relação: N exemplares → 1 livro  (COMPOSIÇÃO — exemplar não existe sem livro)
-- -----------------------------------------------------------------------------

CREATE TABLE exemplares (
    exemplar_id    INT  NOT NULL AUTO_INCREMENT,
    isbn           VARCHAR(20) NOT NULL,
    livro_id       INT         NOT NULL,
    estado         ENUM('DISPONIVEL','EMPRESTADO','RESERVADO') NOT NULL DEFAULT 'DISPONIVEL',
    data_aquisicao DATE        NOT NULL,
    PRIMARY KEY (exemplar_id),
    CONSTRAINT fk_exemplar_livro FOREIGN KEY (livro_id)
        REFERENCES livros(livro_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- -----------------------------------------------------------------------------
-- 5. TABELA: utilizadores
--    Enunciado: "número de identificação único, nome completo, contacto válido"
--    Enunciado: "maioritariamente estudantes e docentes"
-- -----------------------------------------------------------------------------

CREATE TABLE utilizadores (
    id             VARCHAR(20)  NOT NULL,
    nome_completo  VARCHAR(200) NOT NULL,
    contacto       VARCHAR(50)  NOT NULL,
    tipo           ENUM('ESTUDANTE','DOCENTE') NOT NULL,
    PRIMARY KEY (id)
);

-- -----------------------------------------------------------------------------
-- 6. TABELA: emprestimos
--    Enunciado: "data de início, data prevista de devolução, estado"
--    Enunciado: "cada empréstimo corresponde a um registo único,
--    envolvendo apenas um utilizador e um único exemplar"
--    Relação: N empréstimos → 1 utilizador  (ASSOCIAÇÃO)
--    Relação: N empréstimos → 1 exemplar    (ASSOCIAÇÃO)
-- -----------------------------------------------------------------------------

CREATE TABLE emprestimos (
    id                       INT  NOT NULL AUTO_INCREMENT,
    utilizador_id            VARCHAR(20) NOT NULL,
    exemplar_id              INT         NOT NULL,
    data_inicio              DATE        NOT NULL,
    data_prevista_devolucao  DATE        NOT NULL,
    data_efetiva_devolucao   DATE        NULL DEFAULT NULL,
    estado                   ENUM('ATIVO','DEVOLVIDO','ATRASADO') NOT NULL DEFAULT 'ATIVO',
    PRIMARY KEY (id),
    CONSTRAINT fk_emp_utilizador FOREIGN KEY (utilizador_id)
        REFERENCES utilizadores(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_emp_exemplar FOREIGN KEY (exemplar_id)
        REFERENCES exemplares(exemplar_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- -----------------------------------------------------------------------------
-- 7. DADOS INICIAIS — AUTORES
-- -----------------------------------------------------------------------------

INSERT INTO autores (nome, nacionalidade) VALUES
    ('Robert C. Martin',  'Americano'),
    ('Gang of Four',      'Internacional'),
    ('Thomas H. Cormen',  'Americano'),
    ('Edsger W. Dijkstra','Holandes');

-- -----------------------------------------------------------------------------
-- 8. DADOS INICIAIS — LIVROS
-- -----------------------------------------------------------------------------

INSERT INTO livros (isbn, titulo, ano_publicacao, tipo, autor_id) VALUES
    ('978-0-13-468599-1', 'Clean Code',                    2008, 'TECNICO',    1),
    ('978-0-20-163361-5', 'Design Patterns',               1994, 'TECNICO',    2),
    ('978-0-26-203293-3', 'Introduction to Algorithms',    2009, 'CIENTIFICO', 3),
    ('978-0-13-814900-0', 'A Discipline of Programming',   1976, 'CIENTIFICO', 4);

-- -----------------------------------------------------------------------------
-- 9. DADOS INICIAIS — EXEMPLARES
-- -----------------------------------------------------------------------------

INSERT INTO exemplares (isbn, livro_id, estado, data_aquisicao) VALUES
    ('978-0-13-468599-1', 1, 'DISPONIVEL', '2020-03-15'),  -- Clean Code #1
    ('978-0-13-468599-1', 1, 'DISPONIVEL', '2021-06-10'),  -- Clean Code #2
    ('978-0-20-163361-5', 2, 'DISPONIVEL', '2019-11-05'),  -- Design Patterns #1
    ('978-0-26-203293-3', 3, 'DISPONIVEL', '2022-01-20'),  -- Intro Algorithms #1
    ('978-0-26-203293-3', 3, 'DISPONIVEL', '2022-01-20'),  -- Intro Algorithms #2
    ('978-0-26-203293-3', 3, 'DISPONIVEL', '2023-08-01'),  -- Intro Algorithms #3
    ('978-0-13-814900-0', 4, 'DISPONIVEL', '2018-05-30');  -- Discipline of Prog #1

-- -----------------------------------------------------------------------------
-- 10. DADOS INICIAIS — UTILIZADORES
-- -----------------------------------------------------------------------------

INSERT INTO utilizadores (id, nome_completo, contacto, tipo) VALUES
    ('U001', 'Emanuel dos Santos', '923 000 001', 'ESTUDANTE'),
    ('U002', 'Joao Fernandes',     '923 000 002', 'ESTUDANTE'),
    ('U003', 'Prof. Ana Lopes',    '923 000 003', 'DOCENTE');

-- -----------------------------------------------------------------------------
-- 11. VERIFICAÇÃO — listar tudo após inserção
-- -----------------------------------------------------------------------------

SELECT 'autores'     AS tabela, COUNT(*) AS registos FROM autores
UNION ALL
SELECT 'livros',       COUNT(*) FROM livros
UNION ALL
SELECT 'exemplares',   COUNT(*) FROM exemplares
UNION ALL
SELECT 'utilizadores', COUNT(*) FROM utilizadores
UNION ALL
SELECT 'emprestimos',  COUNT(*) FROM emprestimos;
