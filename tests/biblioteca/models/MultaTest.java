package biblioteca.models;
import biblioteca.exception.MultaJaProcessada;
import biblioteca.model.Emprestimo;
import biblioteca.model.Multa;
import biblioteca.model.StatusMulta;
import biblioteca.model.material.Livro;
import biblioteca.model.usuario.Cargo;
import biblioteca.model.usuario.Usuario;

import biblioteca.repository.MaterialRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MultaTest {

    private Emprestimo emprestimo;

    @BeforeEach
    void setUp() {
        Usuario usuario = new Usuario(
                "Maria Silva",
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
                usuario
        );
    }

    @Test
    void deveCriarMultaComStatusPendente() {
        Multa multa = new Multa(
                5,
                new BigDecimal("10.00"),
                emprestimo
        );

        assertNotEquals(0, multa.getId());
        assertEquals(5, multa.getDiasAtraso());
        assertEquals(new BigDecimal("10.00"), multa.getValor());
        assertEquals(StatusMulta.PENDENTE, multa.getStatus());
        assertEquals(LocalDate.now(), multa.getDataGeracao());
        assertNull(multa.getDataPagamento());
        assertEquals(emprestimo, multa.getEmprestimo());
    }

    @Test
    void deveCriarMultaComIdInformado() {
        Multa multa = new Multa(
                5,
                new BigDecimal("10.00"),
                LocalDate.now(),
                emprestimo,
                123
        );

        assertEquals(123, multa.getId());
        assertEquals(5, multa.getDiasAtraso());
        assertEquals(new BigDecimal("10.00"), multa.getValor());
        assertEquals(StatusMulta.PENDENTE, multa.getStatus());
        assertNull(multa.getDataPagamento());
    }

    @Test
    void deveRegistrarPagamento() {
        Multa multa = new Multa(
                5,
                new BigDecimal("10.00"),
                emprestimo
        );

        multa.registrarPagamento();

        assertEquals(StatusMulta.PAGA, multa.getStatus());
        assertEquals(LocalDate.now(), multa.getDataPagamento());
    }

    @Test
    void naoDeveRegistrarPagamentoDuasVezes() {
        Multa multa = new Multa(
                5,
                new BigDecimal("10.00"),
                emprestimo
        );

        multa.registrarPagamento();

        assertThrows(
                MultaJaProcessada.class,
                multa::registrarPagamento
        );
    }

    @Test
    void deveRegistrarCancelamento() {
        Multa multa = new Multa(
                5,
                new BigDecimal("10.00"),
                emprestimo
        );

        multa.registrarCancelamento();

        assertEquals(StatusMulta.CANCELADA, multa.getStatus());
        assertNull(multa.getDataPagamento());
    }

    @Test
    void naoDeveCancelarMultaDuasVezes() {
        Multa multa = new Multa(
                5,
                new BigDecimal("10.00"),
                emprestimo
        );

        multa.registrarCancelamento();

        assertThrows(
                MultaJaProcessada.class,
                multa::registrarCancelamento
        );
    }

    @Test
    void naoDevePagarMultaCancelada() {
        Multa multa = new Multa(
                5,
                new BigDecimal("10.00"),
                emprestimo
        );

        multa.registrarCancelamento();

        assertThrows(
                MultaJaProcessada.class,
                multa::registrarPagamento
        );
    }

    @Test
    void naoDeveCancelarMultaPaga() {
        Multa multa = new Multa(
                5,
                new BigDecimal("10.00"),
                emprestimo
        );

        multa.registrarPagamento();

        assertThrows(
                MultaJaProcessada.class,
                multa::registrarCancelamento
        );
    }
}