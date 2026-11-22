
package biblioteca.repository;

import biblioteca.exception.ItemDuplicadoException;
import biblioteca.exception.RegistroNaoEncontradoException;
import biblioteca.model.SolicitacaoEmprestimo;
import biblioteca.model.StatusEnums.StatusSolicitacao;
import biblioteca.model.material.Livro;
import biblioteca.model.usuario.Cargo;
import biblioteca.model.usuario.Usuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SolicitacaoEmprestimoRepositorioTest {

    private SolicitacaoEmprestimoRepositorio repositorio;

    private Usuario usuario1;
    private Usuario usuario2;

    private Livro livro1;
    private Livro livro2;
    private Livro livro3;

    @BeforeEach
    void setUp() {
        repositorio = new SolicitacaoEmprestimoRepositorio();

        usuario1 = new Usuario("João", "joao", "123", Cargo.LEITOR);
        usuario2 = new Usuario("Maria", "maria", "123", Cargo.LEITOR);

        livro1 = new Livro("Clean Code", 2008, "Robert Martin", "111", "Prentice Hall", Path.of(MaterialRepositorio.uploadsDirectory, "clean_code.jpg"));
        livro2 = new Livro("Clean Architecture", 2017, "Robert Martin", "222", "Prentice Hall", Path.of(MaterialRepositorio.uploadsDirectory, "clean_code.jpg"));
        livro3 = new Livro("Effective Java", 2018, "Joshua Bloch", "333", "Addison-Wesley", Path.of(MaterialRepositorio.uploadsDirectory, "clean_code.jpg"));
    }

    private SolicitacaoEmprestimo criarSolicitacao(
            Usuario usuario,
            Livro livro,
            StatusSolicitacao status,
            int id
    ) {
        return new SolicitacaoEmprestimo(usuario.getID(), livro.getId(), status, id);
    }

    @Test
    void deveAdicionarSolicitacao() throws Exception {
        SolicitacaoEmprestimo solicitacao =
                criarSolicitacao(usuario1, livro1, StatusSolicitacao.PENDENTE, 1);

        repositorio.adicionar(solicitacao);

        assertEquals(1, repositorio.listarTodos().size());
        assertSame(solicitacao, repositorio.buscarPorId(1));
    }

    @Test
    void naoDeveAdicionarSolicitacaoComIdDuplicado() throws Exception {
        SolicitacaoEmprestimo solicitacao1 =
                criarSolicitacao(usuario1, livro1, StatusSolicitacao.PENDENTE, 1);

        SolicitacaoEmprestimo solicitacao2 =
                criarSolicitacao(usuario2, livro2, StatusSolicitacao.PENDENTE, 1);

        repositorio.adicionar(solicitacao1);

        assertThrowsExactly(
                ItemDuplicadoException.class,
                () -> repositorio.adicionar(solicitacao2)
        );

        assertEquals(1, repositorio.listarTodos().size());
    }

    @Test
    void deveBuscarSolicitacaoPorId() throws Exception {
        SolicitacaoEmprestimo solicitacao =
                criarSolicitacao(usuario1, livro1, StatusSolicitacao.PENDENTE, 1);

        repositorio.adicionar(solicitacao);

        assertSame(solicitacao, repositorio.buscarPorId(1));
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        assertThrowsExactly(
                RegistroNaoEncontradoException.class,
                () -> repositorio.buscarPorId(999)
        );
    }

    @Test
    void deveListarTodasAsSolicitacoes() throws Exception {
        repositorio.adicionar(
                criarSolicitacao(usuario1, livro1, StatusSolicitacao.PENDENTE, 1)
        );

        repositorio.adicionar(
                criarSolicitacao(usuario2, livro2, StatusSolicitacao.APROVADA, 2)
        );

        repositorio.adicionar(
                criarSolicitacao(usuario1, livro3, StatusSolicitacao.REJEITADA, 3)
        );

        List<SolicitacaoEmprestimo> solicitacoes = repositorio.listarTodos();

        assertEquals(3, solicitacoes.size());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremSolicitacoes() {
        assertTrue(repositorio.listarTodos().isEmpty());
    }

    @Test
    void deveRetornarUmaNovaListaAoListarTodos() throws Exception {
        repositorio.adicionar(
                criarSolicitacao(usuario1, livro1, StatusSolicitacao.PENDENTE, 1)
        );

        List<SolicitacaoEmprestimo> solicitacoes = repositorio.listarTodos();

        solicitacoes.clear();

        assertEquals(1, repositorio.listarTodos().size());
    }

    @Test
    void deveListarSolicitacoesPorStatus() throws Exception {
        repositorio.adicionar(
                criarSolicitacao(usuario1, livro1, StatusSolicitacao.PENDENTE, 1)
        );

        repositorio.adicionar(
                criarSolicitacao(usuario2, livro2, StatusSolicitacao.APROVADA, 2)
        );

        repositorio.adicionar(
                criarSolicitacao(usuario1, livro3, StatusSolicitacao.PENDENTE, 3)
        );

        List<SolicitacaoEmprestimo> pendentes =
                repositorio.listarPorStatus(StatusSolicitacao.PENDENTE);

        assertEquals(2, pendentes.size());

        assertTrue(pendentes.stream().allMatch(
                s -> s.getStatus() == StatusSolicitacao.PENDENTE
        ));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistirSolicitacaoComStatus() throws Exception {
        repositorio.adicionar(
                criarSolicitacao(usuario1, livro1, StatusSolicitacao.PENDENTE, 1)
        );

        assertTrue(
                repositorio.listarPorStatus(StatusSolicitacao.REJEITADA).isEmpty()
        );
    }

    @Test
    void deveListarSolicitacoesPorUsuario() throws Exception {
        repositorio.adicionar(
                criarSolicitacao(usuario1, livro1, StatusSolicitacao.PENDENTE, 1)
        );

        repositorio.adicionar(
                criarSolicitacao(usuario2, livro2, StatusSolicitacao.PENDENTE, 2)
        );

        repositorio.adicionar(
                criarSolicitacao(usuario1, livro3, StatusSolicitacao.APROVADA, 3)
        );

        List<SolicitacaoEmprestimo> solicitacoes =
                repositorio.listarPorUsuario(usuario1.getID());

        assertEquals(2, solicitacoes.size());

        assertTrue(solicitacoes.stream().allMatch(
                s -> s.getSolicitanteId() == usuario1.getID()
        ));
    }

    @Test
    void deveRetornarListaVaziaParaUsuarioSemSolicitacoes() {
        assertTrue(repositorio.listarPorUsuario(usuario1.getID()).isEmpty());
    }

    @Test
    void deveListarSolicitacoesPorUsuarioEStatus() throws Exception {
        repositorio.adicionar(
                criarSolicitacao(usuario1, livro1, StatusSolicitacao.PENDENTE, 1)
        );

        repositorio.adicionar(
                criarSolicitacao(usuario1, livro2, StatusSolicitacao.APROVADA, 2)
        );

        repositorio.adicionar(
                criarSolicitacao(usuario2, livro3, StatusSolicitacao.PENDENTE, 3)
        );

        List<SolicitacaoEmprestimo> resultado =
                repositorio.listarPorUsuarioEStatus(
                        usuario1.getID(),
                        StatusSolicitacao.PENDENTE
                );

        assertEquals(1, resultado.size());
        assertEquals(1, resultado.getFirst().getId());
    }

    @Test
    void deveAtualizarSolicitacaoExistente() throws Exception {
        SolicitacaoEmprestimo original =
                criarSolicitacao(usuario1, livro1, StatusSolicitacao.PENDENTE, 1);

        repositorio.adicionar(original);

        SolicitacaoEmprestimo atualizada =
                criarSolicitacao(usuario1, livro1, StatusSolicitacao.APROVADA, 1);

        repositorio.atualizar(atualizada);

        SolicitacaoEmprestimo resultado = repositorio.buscarPorId(1);

        assertEquals(StatusSolicitacao.APROVADA, resultado.getStatus());
        assertSame(atualizada, resultado);
    }

    @Test
    void naoDeveAtualizarSolicitacaoInexistente() {
        SolicitacaoEmprestimo solicitacao =
                criarSolicitacao(usuario1, livro1, StatusSolicitacao.APROVADA, 999);

        assertThrowsExactly(
                RegistroNaoEncontradoException.class,
                () -> repositorio.atualizar(solicitacao)
        );
    }
}
