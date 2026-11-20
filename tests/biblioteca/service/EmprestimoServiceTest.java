package biblioteca.service;

import biblioteca.auth.Sessao;
import biblioteca.exception.*;
import biblioteca.model.Emprestimo;
import biblioteca.model.Multa;
import biblioteca.model.StatusEmprestimo;
import biblioteca.model.StatusMulta;
import biblioteca.model.material.Livro;
import biblioteca.model.material.MaterialBibliografico;
import biblioteca.model.usuario.Cargo;
import biblioteca.model.usuario.Usuario;
import biblioteca.repository.EmprestimoRepositorio;
import biblioteca.repository.MaterialRepositorio;
import biblioteca.repository.MultaRepositorio;
import biblioteca.repository.UsuarioRepositorio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmprestimoServiceTest {

    private EmprestimoService service;
    private EmprestimoRepositorio emprestimoRepositorio;
    private MaterialRepositorio materialRepositorio;
    private UsuarioRepositorio usuarioRepositorio;
    private MultaRepositorio multaRepositorio;
    private MultaService multaService;
    private Sessao sessao;

    private Usuario usuarioPermitido;
    private MaterialBibliografico material;

    @BeforeEach
    void setUp() throws ItemDuplicadoException {
        emprestimoRepositorio = new EmprestimoRepositorio();
        materialRepositorio = new MaterialRepositorio();
        usuarioRepositorio = new UsuarioRepositorio();
        multaRepositorio = new MultaRepositorio();

        AutorizacaoService autorizacaoService = new AutorizacaoService();
        sessao = new Sessao();

        multaService = new MultaService(
                autorizacaoService,
                sessao,
                multaRepositorio
        );

        service = new EmprestimoService(
                emprestimoRepositorio,
                materialRepositorio,
                usuarioRepositorio,
                autorizacaoService,
                sessao,
                multaService
        );

        usuarioPermitido = new Usuario(
                "João Silva",
                "joao",
                "123456",
                Cargo.BIBLIOTECARIO
        );

        material = new Livro(
                "Clean Code",
                2008,
                "Robert C. Martin",
                "9780132350884",
                "Prentice Hall",
                Path.of(MaterialRepositorio.uploadsDirectory, "clean_code.jpg")
        );

        usuarioRepositorio.adicionar(usuarioPermitido);
        materialRepositorio.adicionar(material);

        sessao.iniciarSessao(usuarioPermitido);
    }

    // REGISTRO DE EMPRÉSTIMOS

    @Test
    void deveRegistrarEmprestimo() throws Exception {
        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        List<Emprestimo> emprestimos =
                emprestimoRepositorio.listarTodos();

        assertEquals(1, emprestimos.size());
        assertTrue(emprestimos.getFirst().estaAtivo());
    }

    @Test
    void deveMarcarMaterialComoIndisponivelAposEmprestimo()
            throws Exception {

        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        MaterialBibliografico encontrado =
                materialRepositorio.buscarPorId(material.getId());

        assertFalse(encontrado.estaDisponivel());
    }

    @Test
    void deveImpedirEmprestimoDeMaterialIndisponivel()
            throws Exception {

        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        assertThrows(
                MaterialIndisponivelException.class,
                () -> service.registrarEmprestimo(
                        usuarioPermitido.getID(),
                        material.getId()
                )
        );
    }

    @Test
    void deveImpedirEmprestimoComUsuarioInexistente() {
        assertThrows(
                RegistroNaoEncontradoException.class,
                () -> service.registrarEmprestimo(
                        -1,
                        material.getId()
                )
        );
    }

    @Test
    void deveImpedirEmprestimoComMaterialInexistente() {
        assertThrows(
                RegistroNaoEncontradoException.class,
                () -> service.registrarEmprestimo(
                        usuarioPermitido.getID(),
                        -1
                )
        );
    }

    @Test
    void deveImpedirMaisDeTresEmprestimosAtivos()
            throws Exception {

        MaterialBibliografico livro2 = new Livro(
                "Clean Architecture",
                2017,
                "Robert C. Martin",
                "9780134494166",
                "Prentice Hall",
                Path.of(MaterialRepositorio.uploadsDirectory, "clean_code.jpg")
        );

        MaterialBibliografico livro3 = new Livro(
                "Domain-Driven Design",
                2003,
                "Eric Evans",
                "9780321125217",
                "Addison-Wesley",
                Path.of(MaterialRepositorio.uploadsDirectory, "clean_code.jpg")
        );

        MaterialBibliografico livro4 = new Livro(
                "Refactoring",
                1999,
                "Martin Fowler",
                "9780201485677",
                "Addison-Wesley",
                Path.of(MaterialRepositorio.uploadsDirectory, "clean_code.jpg")
        );

        materialRepositorio.adicionar(livro2);
        materialRepositorio.adicionar(livro3);
        materialRepositorio.adicionar(livro4);

        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                livro2.getId()
        );

        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                livro3.getId()
        );

        assertThrows(
                LimiteEmprestimosException.class,
                () -> service.registrarEmprestimo(
                        usuarioPermitido.getID(),
                        livro4.getId()
                )
        );
    }

    @Test
    void devePermitirNovoEmprestimoAposDevolucao()
            throws Exception {

        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        Emprestimo emprestimo =
                emprestimoRepositorio.listarTodos().getFirst();

        service.registrarDevolucao(emprestimo.getId());

        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        assertEquals(2, emprestimoRepositorio.listarTodos().size());
    }

    @Test
    void deveCalcularDataPrevistaDeDevolucaoParaLivro()
            throws Exception {

        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        Emprestimo emprestimo =
                emprestimoRepositorio.listarTodos().getFirst();

        assertEquals(
                LocalDate.now().plusDays(
                        EmprestimoService.PRAZO_EMPRESTIMO_LIVRO
                ),
                emprestimo.getDataPrevistaDevolucao()
        );
    }

    // DEVOLUÇÃO DE EMPRÉSTIMOS

    @Test
    void deveRegistrarDevolucao() throws Exception {
        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        Emprestimo emprestimo =
                emprestimoRepositorio.listarTodos().getFirst();

        service.registrarDevolucao(emprestimo.getId());

        assertFalse(emprestimo.estaAtivo());
        assertEquals(StatusEmprestimo.DEVOLVIDO, emprestimo.getStatus());
    }

    @Test
    void deveMarcarMaterialComoDisponivelAposDevolucao()
            throws Exception {

        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        Emprestimo emprestimo =
                emprestimoRepositorio.listarTodos().getFirst();

        service.registrarDevolucao(emprestimo.getId());

        MaterialBibliografico encontrado =
                materialRepositorio.buscarPorId(material.getId());

        assertTrue(encontrado.estaDisponivel());
    }

    @Test
    void deveImpedirDevolucaoDuplicada() throws Exception {
        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        Emprestimo emprestimo =
                emprestimoRepositorio.listarTodos().getFirst();

        service.registrarDevolucao(emprestimo.getId());

        assertThrows(
                MaterialDevolvidoException.class,
                () -> service.registrarDevolucao(emprestimo.getId())
        );
    }

    @Test
    void deveImpedirDevolucaoDeEmprestimoInexistente() {
        assertThrows(
                RegistroNaoEncontradoException.class,
                () -> service.registrarDevolucao(-1)
        );
    }

    // INTEGRAÇÃO COM MULTAS

    @Test
    void deveRegistrarMultaQuandoDevolucaoAconteceComAtraso()
            throws Exception {

        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        Emprestimo emprestimo =
                emprestimoRepositorio.listarTodos().getFirst();

        emprestimo.setDataPrevistaDevolucao(
                LocalDate.now().minusDays(5)
        );

        service.registrarDevolucao(emprestimo.getId());

        List<Multa> multas = multaRepositorio.listarTodos();

        assertEquals(1, multas.size());
        assertEquals(StatusMulta.PENDENTE, multas.getFirst().getStatus());
        assertEquals(5, multas.getFirst().getDiasAtraso());
        assertEquals(
                new BigDecimal("10.00"),
                multas.getFirst().getValor()
        );
    }

    @Test
    void naoDeveRegistrarMultaQuandoDevolucaoAconteceNoPrazo()
            throws Exception {

        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        Emprestimo emprestimo =
                emprestimoRepositorio.listarTodos().getFirst();

        emprestimo.setDataPrevistaDevolucao(LocalDate.now());

        service.registrarDevolucao(emprestimo.getId());

        assertTrue(multaRepositorio.listarTodos().isEmpty());
    }

    @Test
    void deveBloquearEmprestimoQuandoUsuarioPossuiMultaPendente()
            throws Exception {

        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        Emprestimo emprestimo =
                emprestimoRepositorio.listarTodos().getFirst();

        emprestimo.setDataPrevistaDevolucao(
                LocalDate.now().minusDays(3)
        );

        service.registrarDevolucao(emprestimo.getId());

        MaterialBibliografico outroLivro = new Livro(
                "Effective Java",
                2018,
                "Joshua Bloch",
                "9780134685991",
                "Addison-Wesley",
                Path.of(MaterialRepositorio.uploadsDirectory, "clean_code.jpg")
        );

        materialRepositorio.adicionar(outroLivro);

        assertThrows(
                UsuarioPossuiMultasPendentes.class,
                () -> service.registrarEmprestimo(
                        usuarioPermitido.getID(),
                        outroLivro.getId()
                )
        );
    }

    @Test
    void devePermitirEmprestimoQuandoUsuarioPossuiApenasMultasPagas()
            throws Exception {

        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        Emprestimo emprestimo =
                emprestimoRepositorio.listarTodos().getFirst();

        emprestimo.setDataPrevistaDevolucao(
                LocalDate.now().minusDays(3)
        );

        service.registrarDevolucao(emprestimo.getId());

        Multa multa = multaRepositorio.listarTodos().getFirst();

        multaService.pagarMulta(multa.getId());

        MaterialBibliografico outroLivro = new Livro(
                "Effective Java",
                2018,
                "Joshua Bloch",
                "9780134685991",
                "Addison-Wesley",
                Path.of(MaterialRepositorio.uploadsDirectory, "clean_code.jpg")
        );

        materialRepositorio.adicionar(outroLivro);

        assertDoesNotThrow(
                () -> service.registrarEmprestimo(
                        usuarioPermitido.getID(),
                        outroLivro.getId()
                )
        );
    }

    @Test
    void deveLiberarMaterialMesmoQuandoDevolucaoGeraMulta()
            throws Exception {

        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        Emprestimo emprestimo =
                emprestimoRepositorio.listarTodos().getFirst();

        emprestimo.setDataPrevistaDevolucao(
                LocalDate.now().minusDays(2)
        );

        service.registrarDevolucao(emprestimo.getId());

        assertTrue(material.estaDisponivel());
        assertEquals(1, multaRepositorio.listarTodos().size());
    }

    // CONSULTAS DE EMPRÉSTIMOS

    @Test
    void deveBuscarEmprestimoPorId() throws Exception {
        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        Emprestimo esperado =
                emprestimoRepositorio.listarTodos().getFirst();

        Emprestimo encontrado =
                service.buscarEmprestimo(esperado.getId());

        assertEquals(esperado.getId(), encontrado.getId());
    }

    @Test
    void deveImpedirBuscaDeEmprestimoInexistente() {
        assertThrows(
                RegistroNaoEncontradoException.class,
                () -> service.buscarEmprestimo(-1)
        );
    }

    @Test
    void deveListarTodosOsEmprestimos() throws Exception {
        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        assertEquals(1, service.listarEmprestimos().size());
    }

    @Test
    void deveListarEmprestimosPorUsuario() throws Exception {
        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        List<Emprestimo> emprestimos =
                service.listarEmprestimosPorUsuario(
                        usuarioPermitido.getID()
                );

        assertEquals(1, emprestimos.size());
        assertEquals(
                usuarioPermitido.getID(),
                emprestimos.getFirst().getUsuario().getID()
        );
    }

    @Test
    void deveListarEmprestimosPorStatus() throws Exception {
        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        List<Emprestimo> emprestimos =
                service.listarEmprestimosPorStatus(StatusEmprestimo.ATIVO);

        assertEquals(1, emprestimos.size());
        assertEquals(
                StatusEmprestimo.ATIVO,
                emprestimos.getFirst().getStatus()
        );
    }

    @Test
    void deveListarEmprestimosPorUsuarioEStatus()
            throws Exception {

        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        List<Emprestimo> emprestimos =
                service.listarEmprestimosPorUsuarioEStatus(
                        usuarioPermitido.getID(),
                        StatusEmprestimo.ATIVO
                );

        assertEquals(1, emprestimos.size());
    }

    @Test
    void deveListarMeusEmprestimos() throws Exception {
        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        List<Emprestimo> emprestimos =
                service.listarMeusEmprestimos();

        assertEquals(1, emprestimos.size());
    }

    @Test
    void deveListarMeusEmprestimosPorStatus() throws Exception {
        service.registrarEmprestimo(
                usuarioPermitido.getID(),
                material.getId()
        );

        List<Emprestimo> emprestimos =
                service.listarMeusEmprestimosPorStatus(
                        StatusEmprestimo.ATIVO
                );

        assertEquals(1, emprestimos.size());
    }

    // AUTORIZAÇÃO

    @Test
    void deveImpedirLeitorDeRegistrarEmprestimo() {
        Usuario leitor = new Usuario(
                "Maria",
                "maria",
                "123456",
                Cargo.LEITOR
        );

        sessao.iniciarSessao(leitor);

        assertThrows(
                OperacaoNaoPermitidaException.class,
                () -> service.registrarEmprestimo(
                        usuarioPermitido.getID(),
                        material.getId()
                )
        );
    }

    @Test
    void deveImpedirUsuarioNaoAutenticadoDeRegistrarEmprestimo() {
        sessao.encerrarSessao();

        assertThrows(
                UsuarioNaoAutenticadoException.class,
                () -> service.registrarEmprestimo(
                        usuarioPermitido.getID(),
                        material.getId()
                )
        );
    }

    @Test
    void deveImpedirLeitorDeConsultarTodosOsEmprestimos() {
        Usuario leitor = new Usuario(
                "Maria",
                "maria",
                "123456",
                Cargo.LEITOR
        );

        sessao.iniciarSessao(leitor);

        assertThrows(
                OperacaoNaoPermitidaException.class,
                () -> service.listarEmprestimos()
        );
    }

    @Test
    void devePermitirLeitorConsultarPropriosEmprestimos()
            throws Exception {

        Usuario leitor = new Usuario(
                "Maria",
                "maria",
                "123456",
                Cargo.LEITOR
        );

        usuarioRepositorio.adicionar(leitor);

        sessao.iniciarSessao(usuarioPermitido);

        service.registrarEmprestimo(
                leitor.getID(),
                material.getId()
        );

        sessao.iniciarSessao(leitor);

        assertDoesNotThrow(
                () -> service.listarMeusEmprestimos()
        );
    }
}
