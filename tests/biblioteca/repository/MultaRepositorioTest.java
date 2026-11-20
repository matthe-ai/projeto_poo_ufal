package biblioteca.repository;
import biblioteca.exception.ItemDuplicadoException;
import biblioteca.exception.RegistroNaoEncontradoException;
import biblioteca.model.Emprestimo;
import biblioteca.model.Multa;
import biblioteca.model.StatusMulta;
import biblioteca.model.material.Livro;
import biblioteca.model.usuario.Cargo;
import biblioteca.model.usuario.Usuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultaRepositorioTest {

    private MultaRepositorio repositorio;

    private Usuario usuario;

    private Emprestimo emprestimo;
    private Emprestimo outroEmprestimo;

    private Multa multa;

    @BeforeEach
    void setUp() {
        repositorio = new MultaRepositorio();

        usuario = new Usuario(
                "Maria",
                "maria",
                "123456",
                Cargo.LEITOR
        );

        Usuario outroUsuario = new Usuario(
                "João",
                "joao",
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

        Livro outroLivro = new Livro(
                "Java Efetivo",
                2018,
                "Joshua Bloch",
                "9780134685991",
                "Alta Books",
                Path.of(MaterialRepositorio.uploadsDirectory, "clean_code.jpg")
        );

        emprestimo = new Emprestimo(
                LocalDate.now(),
                livro,
                usuario
        );

        outroEmprestimo = new Emprestimo(
                LocalDate.now(),
                outroLivro,
                outroUsuario
        );

        multa = new Multa(
                5,
                new BigDecimal("10.00"),
                emprestimo
        );
    }

    @Test
    void deveAdicionarMulta() throws Exception {
        repositorio.adicionar(multa);

        assertEquals(1, repositorio.listarTodos().size());
    }

    @Test
    void deveBuscarMultaPorId() throws Exception {
        repositorio.adicionar(multa);

        Multa encontrada = repositorio.buscarPorId(multa.getId());

        assertEquals(multa.getId(), encontrada.getId());
    }

    @Test
    void deveImpedirBuscaDeMultaInexistente() {
        assertThrows(
                RegistroNaoEncontradoException.class,
                () -> repositorio.buscarPorId(-1)
        );
    }

    @Test
    void deveListarTodasAsMultas() throws Exception {
        repositorio.adicionar(multa);

        Multa outraMulta = new Multa(
                3,
                new BigDecimal("6.00"),
                outroEmprestimo
        );

        repositorio.adicionar(outraMulta);

        assertEquals(2, repositorio.listarTodos().size());
    }

    @Test
    void deveRetornarUmaCopiaDaListaDeMultas() throws Exception {
        repositorio.adicionar(multa);

        List<Multa> multas = repositorio.listarTodos();
        multas.clear();

        assertEquals(1, repositorio.listarTodos().size());
    }

    @Test
    void naoDeveAdicionarMultaComIdDuplicado() throws Exception {
        repositorio.adicionar(multa);

        Multa multaDuplicada = new Multa(
                3,
                new BigDecimal("6.00"),
                LocalDate.now(),
                outroEmprestimo,
                multa.getId()
        );

        assertThrows(
                ItemDuplicadoException.class,
                () -> repositorio.adicionar(multaDuplicada)
        );
    }

    @Test
    void naoDeveAdicionarDuasMultasParaOMesmoEmprestimo() throws Exception {
        repositorio.adicionar(multa);

        Multa multaDuplicada = new Multa(
                3,
                new BigDecimal("6.00"),
                emprestimo
        );

        assertThrows(
                ItemDuplicadoException.class,
                () -> repositorio.adicionar(multaDuplicada)
        );
    }

    @Test
    void deveListarMultasPorStatus() throws Exception {
        repositorio.adicionar(multa);

        Multa multaPaga = new Multa(
                3,
                new BigDecimal("6.00"),
                outroEmprestimo
        );

        multaPaga.registrarPagamento();
        repositorio.adicionar(multaPaga);

        List<Multa> pendentes =
                repositorio.listarPorStatus(StatusMulta.PENDENTE);

        List<Multa> pagas =
                repositorio.listarPorStatus(StatusMulta.PAGA);

        assertEquals(1, pendentes.size());
        assertEquals(1, pagas.size());
        assertEquals(StatusMulta.PENDENTE, pendentes.getFirst().getStatus());
        assertEquals(StatusMulta.PAGA, pagas.getFirst().getStatus());
    }

    @Test
    void deveListarMultasPorUsuario() throws Exception {
        repositorio.adicionar(multa);

        Multa outraMulta = new Multa(
                3,
                new BigDecimal("6.00"),
                outroEmprestimo
        );

        repositorio.adicionar(outraMulta);

        List<Multa> multasDoUsuario =
                repositorio.listarPorUsuario(usuario.getID());

        assertEquals(1, multasDoUsuario.size());
        assertEquals(
                usuario.getID(),
                multasDoUsuario.getFirst()
                        .getEmprestimo()
                        .getUsuario()
                        .getID()
        );
    }

    @Test
    void deveListarMultasPorUsuarioEStatus() throws Exception {
        repositorio.adicionar(multa);

        Multa multaPaga = new Multa(
                3,
                new BigDecimal("6.00"),
                outroEmprestimo
        );

        multaPaga.registrarPagamento();
        repositorio.adicionar(multaPaga);

        List<Multa> multasPendentes =
                repositorio.listarPorUsuarioEStatus(
                        usuario.getID(),
                        StatusMulta.PENDENTE
                );

        assertEquals(1, multasPendentes.size());
        assertEquals(
                StatusMulta.PENDENTE,
                multasPendentes.getFirst().getStatus()
        );
    }

    @Test
    void deveAtualizarMulta() throws Exception {
        repositorio.adicionar(multa);

        Multa multaAtualizada = new Multa(
                multa.getDiasAtraso(),
                multa.getValor(),
                multa.getDataGeracao(),
                multa.getEmprestimo(),
                multa.getId()
        );

        multaAtualizada.registrarPagamento();

        repositorio.atualizar(multaAtualizada);

        Multa encontrada = repositorio.buscarPorId(multa.getId());

        assertEquals(StatusMulta.PAGA, encontrada.getStatus());
        assertNotSame(multa, encontrada);
    }

    @Test
    void naoDeveAtualizarMultaInexistente() {
        Multa multaInexistente = new Multa(
                5,
                new BigDecimal("10.00"),
                emprestimo
        );

        assertThrows(
                RegistroNaoEncontradoException.class,
                () -> repositorio.atualizar(multaInexistente)
        );
    }
}