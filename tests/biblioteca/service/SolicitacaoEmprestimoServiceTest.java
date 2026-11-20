
package biblioteca.service;

import biblioteca.auth.Sessao;
import biblioteca.exception.*;
import biblioteca.model.SolicitacaoEmprestimo;
import biblioteca.model.StatusSolicitacao;
import biblioteca.model.material.Livro;
import biblioteca.model.usuario.Cargo;
import biblioteca.model.usuario.Usuario;
import biblioteca.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SolicitacaoEmprestimoServiceTest {

    private SolicitacaoEmprestimoService service;

    private SolicitacaoEmprestimoRepositorio solicitacaoRepositorio;
    private EmprestimoRepositorio emprestimoRepositorio;
    private UsuarioRepositorio usuarioRepositorio;

    private Sessao sessao;

    private Usuario leitor;
    private Usuario administrador;

    private Livro livro1;
    private Livro livro2;

    @BeforeEach
    void setUp() throws Exception {

        solicitacaoRepositorio = new SolicitacaoEmprestimoRepositorio();
        MaterialRepositorio materialRepositorio = new MaterialRepositorio();
        emprestimoRepositorio = new EmprestimoRepositorio();
        usuarioRepositorio = new UsuarioRepositorio();
        MultaRepositorio multaRepositorio = new MultaRepositorio();
        sessao = new Sessao();
        Sessao sessaoEmprestimo = new Sessao();
        AutorizacaoService autorizacaoService = new AutorizacaoService();
        MultaService multaService = new MultaService(autorizacaoService, sessaoEmprestimo, multaRepositorio);

        leitor = new Usuario(
                "João",
                "joao",
                "123",
                Cargo.LEITOR
        );

        administrador = new Usuario(
                "Admin",
                "admin",
                "123",
                Cargo.ADMINISTRADOR
        );

        livro1 = new Livro(
                "Clean Code",
                2008,
                "Robert C. Martin",
                "111",
                "Prentice Hall",
                Path.of(MaterialRepositorio.uploadsDirectory, "clean_code.jpg")
        );

        livro2 = new Livro(
                "Clean Architecture",
                2017,
                "Robert C. Martin",
                "222",
                "Prentice Hall",
                Path.of(MaterialRepositorio.uploadsDirectory, "clean_code.jpg")
        );

        usuarioRepositorio.adicionar(leitor);
        usuarioRepositorio.adicionar(administrador);

        materialRepositorio.adicionar(livro1);
        materialRepositorio.adicionar(livro2);

        // A sessão do serviço de empréstimos representa o administrador.
        sessaoEmprestimo.iniciarSessao(administrador);

        EmprestimoService emprestimoService = new EmprestimoService(
                emprestimoRepositorio,
                materialRepositorio,
                usuarioRepositorio,
                autorizacaoService,
                sessaoEmprestimo,
                multaService
        );

        service = new SolicitacaoEmprestimoService(
                autorizacaoService,
                sessao,
                solicitacaoRepositorio,
                materialRepositorio,
                emprestimoService
        );

        // Inicialmente, o usuário autenticado é o leitor.
        sessao.iniciarSessao(leitor);
    }

    private SolicitacaoEmprestimo criarSolicitacao(
            Usuario usuario,
            Livro livro,
            StatusSolicitacao status,
            int id
    ) {
        return new SolicitacaoEmprestimo(usuario, livro, status, id);
    }

    private void autenticarAdministrador() {
        sessao.iniciarSessao(administrador);
    }

    // ==========================
    // Solicitar empréstimo
    // ==========================

    @Test
    void deveSolicitarEmprestimo() throws Exception {

        service.solicitarEmprestimo(livro1.getId());

        assertEquals(1, solicitacaoRepositorio.listarTodos().size());

        SolicitacaoEmprestimo solicitacao = solicitacaoRepositorio.listarTodos().getFirst();

        assertSame(leitor, solicitacao.getSolicitante());
        assertSame(livro1, solicitacao.getMaterialSolicitado());

        assertEquals(
                StatusSolicitacao.PENDENTE,
                solicitacao.getStatus()
        );
    }

    @Test
    void naoDeveSolicitarEmprestimoSemAutenticacao() {

        sessao.encerrarSessao();

        assertThrowsExactly(
                UsuarioNaoAutenticadoException.class,
                () -> service.solicitarEmprestimo(livro1.getId())
        );

        assertTrue(solicitacaoRepositorio.listarTodos().isEmpty());
    }

    @Test
    void naoDeveSolicitarEmprestimoParaMaterialInexistente() {

        assertThrowsExactly(
                RegistroNaoEncontradoException.class,
                () -> service.solicitarEmprestimo(-1)
        );

        assertTrue(solicitacaoRepositorio.listarTodos().isEmpty());
    }

    @Test
    void naoDeveCriarSolicitacaoPendenteDuplicada() throws Exception {

        service.solicitarEmprestimo(livro1.getId());

        assertThrowsExactly(
                ItemDuplicadoException.class,
                () -> service.solicitarEmprestimo(livro1.getId())
        );

        assertEquals(1, solicitacaoRepositorio.listarTodos().size());
    }

    @Test
    void devePermitirSolicitarNovamenteAposRejeicao() throws Exception {

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(
                        leitor,
                        livro1,
                        StatusSolicitacao.REJEITADA,
                        1
                )
        );

        service.solicitarEmprestimo(livro1.getId());

        assertEquals(2, solicitacaoRepositorio.listarTodos().size());

        assertEquals(
                StatusSolicitacao.PENDENTE,
                solicitacaoRepositorio.listarTodos().get(1).getStatus()
        );
    }

    @Test
    void devePermitirSolicitacoesParaMateriaisDiferentes() throws Exception {

        service.solicitarEmprestimo(livro1.getId());
        service.solicitarEmprestimo(livro2.getId());

        assertEquals(2, solicitacaoRepositorio.listarTodos().size());
    }

    // ==========================
    // Listagens administrativas
    // ==========================

    @Test
    void deveListarTodasAsSolicitacoes() throws Exception {

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(leitor, livro1, StatusSolicitacao.PENDENTE, 1)
        );

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(leitor, livro2, StatusSolicitacao.APROVADA, 2)
        );

        autenticarAdministrador();

        assertEquals(2, service.listarSolicitacoes().size());
    }

    @Test
    void deveListarSolicitacoesPendentes() throws Exception {

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(leitor, livro1, StatusSolicitacao.PENDENTE, 1)
        );

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(leitor, livro2, StatusSolicitacao.APROVADA, 2)
        );

        autenticarAdministrador();

        assertEquals(1, service.listarSolicitacoesPorStatus(StatusSolicitacao.PENDENTE).size());
    }

    @Test
    void deveListarSolicitacoesAprovadas() throws Exception {

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(leitor, livro1, StatusSolicitacao.APROVADA, 1)
        );

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(leitor, livro2, StatusSolicitacao.PENDENTE, 2)
        );

        autenticarAdministrador();

        assertEquals(1, service.listarSolicitacoesPorStatus(StatusSolicitacao.APROVADA).size());
    }

    @Test
    void deveListarSolicitacoesRejeitadas() throws Exception {

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(leitor, livro1, StatusSolicitacao.REJEITADA, 1)
        );

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(leitor, livro2, StatusSolicitacao.PENDENTE, 2)
        );

        autenticarAdministrador();

        assertEquals(1, service.listarSolicitacoesPorStatus(StatusSolicitacao.REJEITADA).size());
    }

    @Test
    void deveListarSolicitacoesPorUsuario() throws Exception {

        Usuario outroLeitor = new Usuario(
                "Maria",
                "maria",
                "123",
                Cargo.LEITOR
        );

        usuarioRepositorio.adicionar(outroLeitor);

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(leitor, livro1, StatusSolicitacao.PENDENTE, 1)
        );

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(outroLeitor, livro2, StatusSolicitacao.PENDENTE, 2)
        );

        autenticarAdministrador();

        assertEquals(
                1,
                service.listarSolicitacoesPorUsuario(leitor.getID()).size()
        );
    }

    @Test
    void deveListarSolicitacoesPorUsuarioEStatus() throws Exception {

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(leitor, livro1, StatusSolicitacao.PENDENTE, 1)
        );

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(leitor, livro2, StatusSolicitacao.APROVADA, 2)
        );

        autenticarAdministrador();

        assertEquals(
                1,
                service.listarSolicitacoesPorUsuarioEStatus(
                        leitor.getID(),
                        StatusSolicitacao.PENDENTE
                ).size()
        );
    }

    // ==========================
    // Listagens do próprio usuário
    // ==========================

    @Test
    void deveListarMinhasSolicitacoes() throws Exception {

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(leitor, livro1, StatusSolicitacao.PENDENTE, 1)
        );

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(leitor, livro2, StatusSolicitacao.APROVADA, 2)
        );

        assertEquals(2, service.listarMinhasSolicitacoes().size());
    }

    @Test
    void deveListarMinhasSolicitacoesPorStatus() throws Exception {

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(leitor, livro1, StatusSolicitacao.PENDENTE, 1)
        );

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(leitor, livro2, StatusSolicitacao.APROVADA, 2)
        );

        assertEquals(
                1,
                service.listarMinhasSolicitacoesPorStatus(
                        StatusSolicitacao.PENDENTE
                ).size()
        );
    }

    // ==========================
    // Aprovação de solicitações
    // ==========================

    @Test
    void deveAprovarSolicitacaoPendente() throws Exception {

        SolicitacaoEmprestimo solicitacao =
                criarSolicitacao(
                        leitor,
                        livro1,
                        StatusSolicitacao.PENDENTE,
                        1
                );

        solicitacaoRepositorio.adicionar(solicitacao);

        autenticarAdministrador();

        service.aprovarSolicitacao(1);

        SolicitacaoEmprestimo resultado =
                solicitacaoRepositorio.buscarPorId(1);

        assertEquals(
                StatusSolicitacao.APROVADA,
                resultado.getStatus()
        );

        assertEquals(1, emprestimoRepositorio.listarTodos().size());
    }

    @Test
    void naoDeveAprovarSolicitacaoInexistente() {

        autenticarAdministrador();

        assertThrowsExactly(
                RegistroNaoEncontradoException.class,
                () -> service.aprovarSolicitacao(999)
        );
    }

    @Test
    void naoDeveAprovarSolicitacaoJaProcessada() throws Exception {

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(
                        leitor,
                        livro1,
                        StatusSolicitacao.REJEITADA,
                        1
                )
        );

        autenticarAdministrador();

        assertThrowsExactly(
                SolicitacaoJaProcessadaException.class,
                () -> service.aprovarSolicitacao(1)
        );

        assertTrue(emprestimoRepositorio.listarTodos().isEmpty());
    }

    // ==========================
    // Rejeição de solicitações
    // ==========================

    @Test
    void deveRejeitarSolicitacaoPendente() throws Exception {

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(
                        leitor,
                        livro1,
                        StatusSolicitacao.PENDENTE,
                        1
                )
        );

        autenticarAdministrador();

        service.rejeitarSolicitacao(1);

        assertEquals(
                StatusSolicitacao.REJEITADA,
                solicitacaoRepositorio.buscarPorId(1).getStatus()
        );

        assertTrue(emprestimoRepositorio.listarTodos().isEmpty());
    }

    @Test
    void naoDeveRejeitarSolicitacaoInexistente() {

        autenticarAdministrador();

        assertThrowsExactly(
                RegistroNaoEncontradoException.class,
                () -> service.rejeitarSolicitacao(999)
        );
    }

    @Test
    void naoDeveRejeitarSolicitacaoJaProcessada() throws Exception {

        solicitacaoRepositorio.adicionar(
                criarSolicitacao(
                        leitor,
                        livro1,
                        StatusSolicitacao.APROVADA,
                        1
                )
        );

        autenticarAdministrador();

        assertThrowsExactly(
                SolicitacaoJaProcessadaException.class,
                () -> service.rejeitarSolicitacao(1)
        );
    }

    // ==========================
    // Autenticação e autorização
    // ==========================

    @Test
    void naoDeveListarSolicitacoesSemAutenticacao() {

        sessao.encerrarSessao();

        assertThrowsExactly(
                UsuarioNaoAutenticadoException.class,
                () -> service.listarSolicitacoes()
        );
    }

    @Test
    void naoDevePermitirListagemAdministrativaAoLeitor() {

        assertThrowsExactly(
                OperacaoNaoPermitidaException.class,
                () -> service.listarSolicitacoes()
        );
    }

    @Test
    void naoDeveAprovarSolicitacaoSemAutenticacao() {

        sessao.encerrarSessao();

        assertThrowsExactly(
                UsuarioNaoAutenticadoException.class,
                () -> service.aprovarSolicitacao(1)
        );
    }
}
