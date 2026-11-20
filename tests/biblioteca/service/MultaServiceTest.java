package biblioteca.service;

import biblioteca.auth.Sessao;
import biblioteca.exception.*;
import biblioteca.model.Emprestimo;
import biblioteca.model.Multa;
import biblioteca.model.StatusMulta;
import biblioteca.model.material.Livro;
import biblioteca.model.usuario.Cargo;
import biblioteca.model.usuario.Usuario;
import biblioteca.repository.MaterialRepositorio;
import biblioteca.repository.MultaRepositorio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultaServiceTest {

    private MultaService service;
    private MultaRepositorio repositorio;
    private Sessao sessao;
    private Usuario leitor;
    private Emprestimo emprestimo;

    @BeforeEach
    void setUp() {
        repositorio = new MultaRepositorio();
        sessao = new Sessao();

        AutorizacaoService autorizacaoService =
                new AutorizacaoService();

        service = new MultaService(
                autorizacaoService,
                sessao,
                repositorio
        );

        Usuario bibliotecario = new Usuario(
                "João",
                "joao",
                "123456",
                Cargo.BIBLIOTECARIO
        );

        leitor = new Usuario(
                "Maria",
                "maria",
                "123456",
                Cargo.LEITOR
        );

        Livro livro = new Livro(
                "Clean Code",
                2008,
                "Robert C. Martin",
                "9780132350884",
                "Prentice Hall",
                Path.of(MaterialRepositorio.uploadsDirectory, "clean_code.jpg")
        );

        emprestimo = new Emprestimo(
                LocalDate.now(),
                livro,
                leitor
        );

        sessao.iniciarSessao(bibliotecario);
    }

    // CADASTRO

    @Test
    void deveCadastrarMulta() throws Exception {
        service.cadastrarMulta(5, emprestimo);

        List<Multa> multas = repositorio.listarTodos();

        assertEquals(1, multas.size());
        assertEquals(5, multas.getFirst().getDiasAtraso());
        assertEquals(
                new BigDecimal("10.00"),
                multas.getFirst().getValor()
        );
        assertEquals(
                StatusMulta.PENDENTE,
                multas.getFirst().getStatus()
        );
    }

    @Test
    void deveCalcularMultaDeDoisReaisPorDia() throws Exception {
        service.cadastrarMulta(3, emprestimo);

        Multa multa = repositorio.listarTodos().getFirst();

        assertEquals(
                new BigDecimal("6.00"),
                multa.getValor()
        );
    }

    @Test
    void deveLimitarValorDaMultaACinquentaReais() throws Exception {
        service.cadastrarMulta(40, emprestimo);

        Multa multa = repositorio.listarTodos().getFirst();

        assertEquals(
                new BigDecimal("50.00"),
                multa.getValor()
        );
    }

    @Test
    void naoDeveCadastrarMultaDuplicadaParaEmprestimo() throws Exception {
        service.cadastrarMulta(5, emprestimo);

        assertThrows(
                ItemDuplicadoException.class,
                () -> service.cadastrarMulta(3, emprestimo)
        );
    }

    // PAGAMENTO

    @Test
    void devePagarMultaPendente() throws Exception {
        service.cadastrarMulta(5, emprestimo);

        Multa multa = repositorio.listarTodos().getFirst();

        service.pagarMulta(multa.getId());

        Multa multaAtualizada =
                repositorio.buscarPorId(multa.getId());

        assertEquals(StatusMulta.PAGA, multaAtualizada.getStatus());
        assertEquals(LocalDate.now(), multaAtualizada.getDataPagamento());
        assertNotSame(multa, multaAtualizada);
    }

    @Test
    void naoDevePagarMultaJaPaga() throws Exception {
        service.cadastrarMulta(5, emprestimo);

        Multa multa = repositorio.listarTodos().getFirst();

        service.pagarMulta(multa.getId());

        assertThrows(
                MultaJaProcessada.class,
                () -> service.pagarMulta(multa.getId())
        );
    }

    @Test
    void naoDevePagarMultaCancelada() throws Exception {
        service.cadastrarMulta(5, emprestimo);

        Multa multa = repositorio.listarTodos().getFirst();

        service.cancelarMulta(multa.getId());

        assertThrows(
                MultaJaProcessada.class,
                () -> service.pagarMulta(multa.getId())
        );
    }

    // CANCELAMENTO

    @Test
    void deveCancelarMultaPendente() throws Exception {
        service.cadastrarMulta(5, emprestimo);

        Multa multa = repositorio.listarTodos().getFirst();

        service.cancelarMulta(multa.getId());

        Multa multaAtualizada =
                repositorio.buscarPorId(multa.getId());

        assertEquals(
                StatusMulta.CANCELADA,
                multaAtualizada.getStatus()
        );
        assertNull(multaAtualizada.getDataPagamento());
        assertNotSame(multa, multaAtualizada);
    }

    @Test
    void naoDeveCancelarMultaJaCancelada() throws Exception {
        service.cadastrarMulta(5, emprestimo);

        Multa multa = repositorio.listarTodos().getFirst();

        service.cancelarMulta(multa.getId());

        assertThrows(
                MultaJaProcessada.class,
                () -> service.cancelarMulta(multa.getId())
        );
    }

    @Test
    void naoDeveCancelarMultaJaPaga() throws Exception {
        service.cadastrarMulta(5, emprestimo);

        Multa multa = repositorio.listarTodos().getFirst();

        service.pagarMulta(multa.getId());

        assertThrows(
                MultaJaProcessada.class,
                () -> service.cancelarMulta(multa.getId())
        );
    }

    // CONSULTAS

    @Test
    void deveBuscarMultaPorId() throws Exception {
        service.cadastrarMulta(5, emprestimo);

        Multa multa = repositorio.listarTodos().getFirst();

        Multa encontrada = service.buscarMulta(multa.getId());

        assertEquals(multa.getId(), encontrada.getId());
    }

    @Test
    void deveListarTodasAsMultas() throws Exception {
        service.cadastrarMulta(5, emprestimo);

        List<Multa> multas = service.listarMultas();

        assertEquals(1, multas.size());
    }

    @Test
    void deveListarMultasPorStatus() throws Exception {
        service.cadastrarMulta(5, emprestimo);

        Multa multa = repositorio.listarTodos().getFirst();
        service.pagarMulta(multa.getId());

        assertEquals(
                1,
                service.listarMultasPorStatus(StatusMulta.PAGA).size()
        );

        assertEquals(
                0,
                service.listarMultasPorStatus(StatusMulta.PENDENTE).size()
        );
    }

    @Test
    void deveListarMultasPorUsuario() throws Exception {
        service.cadastrarMulta(5, emprestimo);

        assertEquals(
                1,
                service.listarMultasPorUsuario(leitor.getID()).size()
        );
    }

    @Test
    void deveListarMultasPorUsuarioEStatus() throws Exception {
        service.cadastrarMulta(5, emprestimo);

        assertEquals(
                1,
                service.listarMultasPorUsuarioEStatus(
                        leitor.getID(),
                        StatusMulta.PENDENTE
                ).size()
        );

        assertEquals(
                0,
                service.listarMultasPorUsuarioEStatus(
                        leitor.getID(),
                        StatusMulta.PAGA
                ).size()
        );
    }

    @Test
    void deveListarMinhasMultas() throws Exception {
        service.cadastrarMulta(5, emprestimo);

        sessao.iniciarSessao(leitor);

        List<Multa> multas = service.listarMinhasMultas();

        assertEquals(1, multas.size());
        assertEquals(
                leitor.getID(),
                multas.getFirst().getEmprestimo().getUsuario().getID()
        );
    }

    @Test
    void deveListarMinhasMultasPorStatus() throws Exception {
        service.cadastrarMulta(5, emprestimo);

        sessao.iniciarSessao(leitor);

        List<Multa> multas =
                service.listarMinhasMultasPorStatus(StatusMulta.PENDENTE);

        assertEquals(1, multas.size());
    }

    // AUTENTICAÇÃO E AUTORIZAÇÃO

    @Test
    void naoDeveCadastrarMultaSemAutenticacao() {
        sessao.encerrarSessao();

        assertThrows(
                UsuarioNaoAutenticadoException.class,
                () -> service.cadastrarMulta(5, emprestimo)
        );
    }

    @Test
    void naoDevePagarMultaSemAutenticacao() {
        sessao.encerrarSessao();

        assertThrows(
                UsuarioNaoAutenticadoException.class,
                () -> service.pagarMulta(1)
        );
    }

    @Test
    void naoDeveCancelarMultaSemAutenticacao() {
        sessao.encerrarSessao();

        assertThrows(
                UsuarioNaoAutenticadoException.class,
                () -> service.cancelarMulta(1)
        );
    }

    @Test
    void leitorNaoDeveGerenciarMultas() {
        sessao.iniciarSessao(leitor);

        assertThrows(
                OperacaoNaoPermitidaException.class,
                () -> service.cadastrarMulta(5, emprestimo)
        );
    }

    @Test
    void leitorNaoDeveConsultarTodasAsMultas() {
        sessao.iniciarSessao(leitor);

        assertThrows(
                OperacaoNaoPermitidaException.class,
                () -> service.listarMultas()
        );
    }

    @Test
    void leitorDevePoderConsultarPropriasMultas() throws Exception {
        service.cadastrarMulta(5, emprestimo);

        sessao.iniciarSessao(leitor);

        assertDoesNotThrow(() -> service.listarMinhasMultas());
    }

    @Test
    void naoDeveBuscarMultaInexistente() {
        assertThrows(
                RegistroNaoEncontradoException.class,
                () -> service.buscarMulta(-1)
        );
    }
}