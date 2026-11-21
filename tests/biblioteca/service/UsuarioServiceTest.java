package biblioteca.service;

import biblioteca.auth.SenhaHashUtil;
import biblioteca.auth.Sessao;
import biblioteca.exception.*;
import biblioteca.model.Emprestimo;
import biblioteca.model.usuario.Cargo;
import biblioteca.model.usuario.Usuario;
import biblioteca.repository.EmprestimoRepositorio;
import biblioteca.repository.UsuarioRepositorio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioServiceTest {

    private UsuarioService service;
    private UsuarioRepositorio usuarioRepositorio;
    private EmprestimoRepositorio emprestimoRepositorio;
    private AutorizacaoService autorizacaoService;
    private Sessao sessao;

    @BeforeEach
    void setUp() {
        usuarioRepositorio = new UsuarioRepositorio();
        emprestimoRepositorio = new EmprestimoRepositorio();
        autorizacaoService = new AutorizacaoService();
        sessao = new Sessao();

        service = new UsuarioService(
                usuarioRepositorio,
                emprestimoRepositorio,
                autorizacaoService,
                sessao
        );
    }

    // Método auxiliar para autenticar usuários com determinado cargo.

    private Usuario autenticar(Cargo cargo) throws ItemDuplicadoException {
        Usuario usuario = new Usuario(
                "Usuario Teste",
                "teste",
                "123456",
                cargo
        );

        usuarioRepositorio.adicionar(usuario);
        sessao.iniciarSessao(usuario);

        return usuario;
    }

    // CADASTRO

    @Test
    void deveCadastrarUsuario() throws Exception {
        this.autenticar(Cargo.ADMINISTRADOR);

        Usuario usuarioCadastrado = service.cadastrarUsuario(
                "João Silva", "joao", "123456",
                Cargo.LEITOR
        );

        Usuario encontrado = usuarioRepositorio.buscarPorId(
                usuarioCadastrado.getID()
        );

        assertNotNull(encontrado);
        assertEquals(usuarioCadastrado.getID(), encontrado.getID());
    }

    @Test
    void deveCadastrarUsuarioComCargoEspecifico() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        Usuario usuarioTeste = service.cadastrarUsuario(
                "João Silva",
                "joao",
                "123456",
                Cargo.BIBLIOTECARIO
        );

        Usuario encontrado = usuarioRepositorio.buscarPorId(
                usuarioTeste.getID()
        );

        assertNotNull(encontrado);
        assertEquals(usuarioTeste.getID(), encontrado.getID());
        assertEquals(Cargo.BIBLIOTECARIO, encontrado.getCargo());
    }

    @Test
    void deveImpedirCadastroComNomeInvalido() throws Exception {
        this.autenticar(Cargo.ADMINISTRADOR);

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.cadastrarUsuario(null, "joao", "123456", Cargo.LEITOR)
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.cadastrarUsuario("", "joao", "123456", Cargo.LEITOR)
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.cadastrarUsuario("   ", "joao", "123456", Cargo.LEITOR)
        );
    }

    @Test
    void deveImpedirCadastroComLoginInvalido() throws Exception {
        this.autenticar(Cargo.ADMINISTRADOR);

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.cadastrarUsuario("João", null, "123456", Cargo.LEITOR)
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.cadastrarUsuario("João", "", "123456", Cargo.LEITOR)
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.cadastrarUsuario("João", "   ", "123456", Cargo.LEITOR)
        );
    }

    @Test
    void deveImpedirCadastroComSenhaInvalida() throws Exception {
        this.autenticar(Cargo.ADMINISTRADOR);

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.cadastrarUsuario("João", "joao", null, Cargo.LEITOR)
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.cadastrarUsuario("João", "joao", "", Cargo.LEITOR)
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.cadastrarUsuario("João", "joao", "12345", Cargo.LEITOR)
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.cadastrarUsuario("João", "joao", "12345678901234567", Cargo.LEITOR)
        );
    }

    @Test
    void deveImpedirCadastroDeUsuarioDuplicado() throws Exception {
        this.autenticar(Cargo.ADMINISTRADOR);

        service.cadastrarUsuario("João Silva", "joao", "123456", Cargo.LEITOR);

        assertThrowsExactly(
                ItemDuplicadoException.class,
                () -> service.cadastrarUsuario(
                        "Outro João", "joao", "654321", Cargo.LEITOR
                )
        );
    }

    @Test
    void deveImpedirCadastroComCargoSemPermissao() throws Exception {
        autenticar(Cargo.BIBLIOTECARIO);

        assertThrowsExactly(
                OperacaoNaoPermitidaException.class,
                () -> service.cadastrarUsuario(
                        "João Silva",
                        "joao",
                        "123456",
                        Cargo.LEITOR
                )
        );
    }

    @Test
    void deveImpedirCadastroComCargoSemAutenticacao() {
        assertThrowsExactly(
                UsuarioNaoAutenticadoException.class,
                () -> service.cadastrarUsuario(
                        "João Silva",
                        "joao",
                        "123456",
                        Cargo.LEITOR
                )
        );
    }

    // AUTENTICAÇÃO

    @Test
    void deveRealizarLogin() throws Exception {
        this.autenticar(Cargo.ADMINISTRADOR);

        Usuario usuario = service.cadastrarUsuario(
                "João Silva", "joao", "123456", Cargo.LEITOR
        );

        service.logar("joao", "123456");

        assertTrue(sessao.estaAutenticado());
        assertEquals(usuario.getID(), sessao.getUsuarioLogado().getID());
    }

    @Test
    void deveImpedirLoginComSenhaIncorreta() throws Exception {
        this.autenticar(Cargo.ADMINISTRADOR);
        service.cadastrarUsuario("João Silva", "joao", "123456", Cargo.LEITOR);
        sessao.encerrarSessao();

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.logar("joao", "654321")
        );

        assertFalse(sessao.estaAutenticado());
    }

    @Test
    void deveImpedirLoginComUsuarioInexistente() {
        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.logar("inexistente", "123456")
        );
    }

    @Test
    void deveImpedirLoginComCredenciaisInvalidas() {
        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.logar("", "123456")
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.logar("joao", "")
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.logar("joao", "12345")
        );
    }

    @Test
    void deveEncerrarSessao() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        service.deslogar();

        assertFalse(sessao.estaAutenticado());
        assertNull(sessao.getUsuarioLogado());
    }

    @Test
    void deveImpedirLogoutSemAutenticacao() {
        assertThrowsExactly(
                UsuarioNaoAutenticadoException.class,
                () -> service.deslogar()
        );
    }

    // CONSULTA

    @Test
    void deveListarUsuariosCadastrados() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        Usuario usuarioTeste = service.cadastrarUsuario(
                "João Silva", "joao", "123456", Cargo.LEITOR
        );

        List<Usuario> usuarios = service.listarUsuarios();

        assertEquals(2, usuarios.size());
        assertTrue(usuarios.contains(usuarioTeste));
    }

    @Test
    void deveListarUsuariosPorLogin() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        Usuario usuario1 = service.cadastrarUsuario(
                "João Silva", "joao", "123456", Cargo.LEITOR
        );

        service.cadastrarUsuario(
                "Maria Silva", "maria", "123456", Cargo.LEITOR
        );

        List<Usuario> usuarios = service.listarUsuariosPorLogin("joao");

        assertEquals(1, usuarios.size());
        assertTrue(usuarios.contains(usuario1));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoEncontrarLogin() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        service.cadastrarUsuario(
                "João Silva", "joao", "123456", Cargo.LEITOR
        );

        List<Usuario> usuarios = service.listarUsuariosPorLogin("inexistente");

        assertTrue(usuarios.isEmpty());
    }

    @Test
    void deveImpedirListagemPorLoginSemAutenticacao() {
        assertThrowsExactly(
                UsuarioNaoAutenticadoException.class,
                () -> service.listarUsuariosPorLogin("joao")
        );
    }

    @Test
    void deveImpedirListagemPorLoginSemPermissao() throws Exception {
        autenticar(Cargo.LEITOR);

        assertThrowsExactly(
                OperacaoNaoPermitidaException.class,
                () -> service.listarUsuariosPorLogin("joao")
        );
    }

    @Test
    void deveListarUsuariosPorNome() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        Usuario usuario1 = service.cadastrarUsuario(
                "João Silva", "joao", "123456", Cargo.LEITOR
        );

        service.cadastrarUsuario(
                "Maria Silva", "maria", "123456", Cargo.LEITOR
        );

        List<Usuario> usuarios = service.listarUsuariosPorNome("João Silva");

        assertEquals(1, usuarios.size());
        assertTrue(usuarios.contains(usuario1));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoEncontrarNome() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        service.cadastrarUsuario(
                "João Silva", "joao", "123456", Cargo.LEITOR
        );

        List<Usuario> usuarios = service.listarUsuariosPorNome("Nome Inexistente");

        assertTrue(usuarios.isEmpty());
    }

    @Test
    void deveImpedirListagemPorNomeSemAutenticacao() {
        assertThrowsExactly(
                UsuarioNaoAutenticadoException.class,
                () -> service.listarUsuariosPorNome("João")
        );
    }

    @Test
    void deveImpedirListagemPorNomeSemPermissao() throws Exception {
        autenticar(Cargo.LEITOR);

        assertThrowsExactly(
                OperacaoNaoPermitidaException.class,
                () -> service.listarUsuariosPorNome("João")
        );
    }

    @Test
    void deveBuscarUsuarioPorId() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        Usuario usuarioTeste = service.cadastrarUsuario(
                "João Silva", "joao", "123456", Cargo.LEITOR
        );

        Usuario usuarioBuscado = service.buscarUsuario(
                usuarioTeste.getID()
        );

        assertEquals(usuarioTeste.getID(), usuarioBuscado.getID());
    }

    @Test
    void deveImpedirBuscaSemAutenticacao() {
        assertThrowsExactly(
                UsuarioNaoAutenticadoException.class,
                () -> service.buscarUsuario(1)
        );
    }

    @Test
    void deveImpedirListagemSemAutenticacao() {
        assertThrowsExactly(
                UsuarioNaoAutenticadoException.class,
                () -> service.listarUsuarios()
        );
    }

    @Test
    void deveImpedirConsultaSemPermissao() throws Exception {
        autenticar(Cargo.LEITOR);

        assertThrowsExactly(
                OperacaoNaoPermitidaException.class,
                () -> service.buscarUsuario(1)
        );

        assertThrowsExactly(
                OperacaoNaoPermitidaException.class,
                () -> service.listarUsuarios()
        );
    }

    @Test
    void deveImpedirBuscaDeUsuarioInexistente() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        assertThrowsExactly(
                RegistroNaoEncontradoException.class,
                () -> service.buscarUsuario(Integer.MIN_VALUE)
        );
    }

    // ATUALIZAÇÃO

    @Test
    void deveAtualizarUsuarioCadastrado() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        Usuario usuarioTeste = service.cadastrarUsuario(
                "João Silva", "joao", "123456", Cargo.LEITOR
        );

        service.atualizarUsuario(
                "Maria Silva",
                "maria",
                usuarioTeste.getID()
        );

        Usuario encontrado = usuarioRepositorio.buscarPorId(
                usuarioTeste.getID()
        );

        assertNotNull(encontrado);
        assertEquals(usuarioTeste.getID(), encontrado.getID());
        assertEquals("Maria Silva", encontrado.getNome());
        assertEquals("maria", encontrado.getLogin());
    }

    @Test
    void deveImpedirAtualizacaoSemAutenticacao() {
        assertThrowsExactly(
                UsuarioNaoAutenticadoException.class,
                () -> service.atualizarUsuario(
                        "Maria Silva", "maria", 1
                )
        );
    }

    @Test
    void deveImpedirAtualizacaoSemPermissao() throws Exception {
        autenticar(Cargo.BIBLIOTECARIO);

        assertThrowsExactly(
                OperacaoNaoPermitidaException.class,
                () -> service.atualizarUsuario(
                        "Maria Silva", "maria", 1
                )
        );
    }

    @Test
    void deveImpedirAtualizacaoComNomeInvalido() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        Usuario usuario = service.cadastrarUsuario(
                "João Silva", "joao", "123456", Cargo.LEITOR
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.atualizarUsuario(
                        null, "maria", usuario.getID()
                )
        );
    }

    @Test
    void deveImpedirAtualizacaoComLoginInvalido() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        Usuario usuario = service.cadastrarUsuario(
                "João Silva", "joao", "123456", Cargo.LEITOR
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.atualizarUsuario(
                        "Maria Silva", "", usuario.getID()
                )
        );
    }

    @Test
    void deveImpedirAtualizacaoDeUsuarioInexistente() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        assertThrowsExactly(
                RegistroNaoEncontradoException.class,
                () -> service.atualizarUsuario(
                        "Maria Silva",
                        "maria",
                        Integer.MIN_VALUE
                )
        );
    }

    @Test
    void deveImpedirAtualizacaoDeUsuarioComLoginDuplicado() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        Usuario usuario1 = service.cadastrarUsuario(
                "João Silva", "joao", "123456", Cargo.LEITOR
        );

        service.cadastrarUsuario(
                "Maria Silva", "maria", "654321", Cargo.LEITOR
        );

        assertThrowsExactly(
                ItemDuplicadoException.class,
                () -> service.atualizarUsuario(
                        "João Silva", "maria", usuario1.getID()
                )
        );
    }

    @Test
    void devePermitirManterProprioLoginNaAtualizacao() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        Usuario usuario = service.cadastrarUsuario(
                "João Silva", "joao", "123456", Cargo.LEITOR
        );

        service.atualizarUsuario(
                "João Santos", "joao", usuario.getID()
        );

        Usuario encontrado = usuarioRepositorio.buscarPorId(usuario.getID());

        assertEquals("João Santos", encontrado.getNome());
        assertEquals("joao", encontrado.getLogin());
    }

    // ATUALIZAÇÃO DE SENHA DE OUTRO USUÁRIO

    @Test
    void deveAtualizarSenhaDeOutroUsuario() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        Usuario usuarioTeste = service.cadastrarUsuario(
                "João Silva", "joao", "123456", Cargo.LEITOR
        );

        service.atualizarSenhaDoUsuario("654321", usuarioTeste.getID());

        Usuario encontrado = usuarioRepositorio.buscarPorId(usuarioTeste.getID());

        assertNotNull(encontrado);
        assertEquals(
                SenhaHashUtil.hash("654321"),
                encontrado.getSenha()
        );

        assertEquals("João Silva", encontrado.getNome());
        assertEquals("joao", encontrado.getLogin());
        assertEquals(Cargo.LEITOR, encontrado.getCargo());
        assertEquals(usuarioTeste.getID(), encontrado.getID());
    }

    @Test
    void deveImpedirAtualizacaoDeSenhaSemAutenticacao() {
        assertThrowsExactly(
                UsuarioNaoAutenticadoException.class,
                () -> service.atualizarSenhaDoUsuario("654321", 1)
        );
    }

    @Test
    void deveImpedirAtualizacaoDeSenhaSemPermissao() throws Exception {
        autenticar(Cargo.BIBLIOTECARIO);

        assertThrowsExactly(
                OperacaoNaoPermitidaException.class,
                () -> service.atualizarSenhaDoUsuario("654321", 1)
        );
    }

    @Test
    void deveImpedirAtualizacaoDeSenhaComSenhaInvalida() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        Usuario usuarioTeste = service.cadastrarUsuario(
                "João Silva", "joao", "123456", Cargo.LEITOR
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.atualizarSenhaDoUsuario(null, usuarioTeste.getID())
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.atualizarSenhaDoUsuario("12345", usuarioTeste.getID())
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.atualizarSenhaDoUsuario(
                        "12345678901234567", usuarioTeste.getID()
                )
        );
    }

    @Test
    void deveImpedirAtualizacaoDeSenhaDeUsuarioInexistente() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        assertThrowsExactly(
                RegistroNaoEncontradoException.class,
                () -> service.atualizarSenhaDoUsuario("654321", Integer.MIN_VALUE)
        );
    }

    // ATUALIZAÇÃO DOS PRÓPRIOS DADOS

    @Test
    void deveAtualizarPropriosDados() throws Exception {
        Usuario usuario = autenticar(Cargo.ADMINISTRADOR);

        service.atualizarMeusDados("Maria Silva", "maria");

        Usuario encontrado = usuarioRepositorio.buscarPorId(usuario.getID());

        assertNotNull(encontrado);
        assertEquals(usuario.getID(), encontrado.getID());
        assertEquals("Maria Silva", encontrado.getNome());
        assertEquals("maria", encontrado.getLogin());
        assertEquals(usuario.getSenha(), encontrado.getSenha());
        assertEquals(usuario.getCargo(), encontrado.getCargo());
    }

    @Test
    void deveImpedirAtualizacaoDosPropriosDadosSemAutenticacao() {
        assertThrowsExactly(
                UsuarioNaoAutenticadoException.class,
                () -> service.atualizarMeusDados("Maria Silva", "maria")
        );
    }

    @Test
    void deveImpedirAtualizacaoDosPropriosDadosComNomeInvalido() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.atualizarMeusDados(null, "maria")
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.atualizarMeusDados("", "maria")
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.atualizarMeusDados("   ", "maria")
        );
    }

    @Test
    void deveImpedirAtualizacaoDosPropriosDadosComLoginInvalido() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.atualizarMeusDados("Maria Silva", null)
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.atualizarMeusDados("Maria Silva", "")
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.atualizarMeusDados("Maria Silva", "   ")
        );
    }

    @Test
    void deveImpedirAtualizacaoDosPropriosDadosComLoginDuplicado() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        service.cadastrarUsuario(
                "João Silva", "joao", "123456", Cargo.LEITOR
        );

        assertThrowsExactly(
                ItemDuplicadoException.class,
                () -> service.atualizarMeusDados("Administrador", "joao")
        );
    }

    @Test
    void devePermitirManterOMesmoLoginNaAtualizacaoDosPropriosDados() throws Exception {
        Usuario usuario = autenticar(Cargo.ADMINISTRADOR);

        service.atualizarMeusDados("Maria Silva", usuario.getLogin());

        Usuario encontrado = usuarioRepositorio.buscarPorId(usuario.getID());

        assertEquals("Maria Silva", encontrado.getNome());
        assertEquals(usuario.getLogin(), encontrado.getLogin());
    }

    // ATUALIZAÇÃO DA PRÓPRIA SENHA

    @Test
    void deveAtualizarMinhaSenha() throws Exception {
        Usuario usuario = new Usuario(
                "Administrador",
                "admin",
                SenhaHashUtil.hash("123456"),
                Cargo.ADMINISTRADOR
        );

        usuarioRepositorio.adicionar(usuario);
        sessao.iniciarSessao(usuario);

        service.atualizarMinhaSenha("123456", "654321");

        Usuario encontrado = usuarioRepositorio.buscarPorId(usuario.getID());

        assertNotNull(encontrado);
        assertEquals(SenhaHashUtil.hash("654321"), encontrado.getSenha());
        assertEquals("Administrador", encontrado.getNome());
        assertEquals("admin", encontrado.getLogin());
        assertEquals(Cargo.ADMINISTRADOR, encontrado.getCargo());
    }

    @Test
    void deveImpedirAtualizacaoDaPropriaSenhaSemAutenticacao() {
        assertThrowsExactly(
                UsuarioNaoAutenticadoException.class,
                () -> service.atualizarMinhaSenha("123456", "654321")
        );
    }

    @Test
    void deveImpedirAtualizacaoDaPropriaSenhaComSenhaAtualIncorreta() throws Exception {
        Usuario usuario = new Usuario(
                "Administrador",
                "admin",
                SenhaHashUtil.hash("123456"),
                Cargo.ADMINISTRADOR
        );

        usuarioRepositorio.adicionar(usuario);
        sessao.iniciarSessao(usuario);

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.atualizarMinhaSenha("senhaerrada", "654321")
        );

        Usuario encontrado = usuarioRepositorio.buscarPorId(usuario.getID());

        assertEquals(SenhaHashUtil.hash("123456"), encontrado.getSenha());
    }

    @Test
    void deveImpedirAtualizacaoDaPropriaSenhaComNovaSenhaInvalida() throws Exception {
        Usuario usuario = new Usuario(
                "Administrador",
                "admin",
                SenhaHashUtil.hash("123456"),
                Cargo.ADMINISTRADOR
        );

        usuarioRepositorio.adicionar(usuario);
        sessao.iniciarSessao(usuario);

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.atualizarMinhaSenha("123456", "12345")
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.atualizarMinhaSenha("123456", null)
        );

        assertThrowsExactly(
                CredenciaisInvalidasException.class,
                () -> service.atualizarMinhaSenha(
                        "123456", "12345678901234567"
                )
        );

        Usuario encontrado = usuarioRepositorio.buscarPorId(usuario.getID());

        assertEquals(SenhaHashUtil.hash("123456"), encontrado.getSenha());
    }

    // REMOÇÃO

    @Test
    void deveRemoverUsuarioCadastrado() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        Usuario usuarioTeste = service.cadastrarUsuario(
                "João Silva", "joao", "123456", Cargo.LEITOR
        );

        service.removerUsuario(usuarioTeste.getID());

        assertThrowsExactly(
                RegistroNaoEncontradoException.class,
                () -> usuarioRepositorio.buscarPorId(
                        usuarioTeste.getID()
                )
        );
    }

    @Test
    void deveImpedirRemocaoSemAutenticacao() {
        assertThrowsExactly(
                UsuarioNaoAutenticadoException.class,
                () -> service.removerUsuario(1)
        );
    }

    @Test
    void deveImpedirRemocaoSemPermissao() throws Exception {
        autenticar(Cargo.BIBLIOTECARIO);

        assertThrowsExactly(
                OperacaoNaoPermitidaException.class,
                () -> service.removerUsuario(1)
        );
    }

    @Test
    void deveImpedirRemocaoDeUsuarioComEmprestimoAtivo() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        Usuario usuarioTeste = service.cadastrarUsuario(
                "João Silva", "joao", "123456", Cargo.LEITOR
        );

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuarioTeste);

        emprestimoRepositorio.adicionar(emprestimo);

        assertThrowsExactly(
                UsuarioPossuiEmprestimosAtivosException.class,
                () -> service.removerUsuario(usuarioTeste.getID())
        );

        assertNotNull(
                usuarioRepositorio.buscarPorId(usuarioTeste.getID())
        );
    }

    @Test
    void devePermitirRemocaoDeUsuarioComEmprestimoDevolvido() throws Exception {
        autenticar(Cargo.ADMINISTRADOR);

        Usuario usuarioTeste = service.cadastrarUsuario(
                "João Silva", "joao", "123456", Cargo.LEITOR
        );

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuarioTeste);

        emprestimo.registrarDevolucao();
        emprestimoRepositorio.adicionar(emprestimo);

        service.removerUsuario(usuarioTeste.getID());

        assertThrowsExactly(
                RegistroNaoEncontradoException.class,
                () -> usuarioRepositorio.buscarPorId(usuarioTeste.getID())
        );
    }
}