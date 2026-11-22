package biblioteca.models;

import static org.junit.jupiter.api.Assertions.*;

import biblioteca.exception.MaterialDevolvidoException;
import org.junit.jupiter.api.Test;

import biblioteca.model.Emprestimo;
import biblioteca.model.StatusEnums.StatusEmprestimo;

import java.time.LocalDate;

public class EmprestimoTest {

	@Test
    void emprestimoDeveComecarAtivo() {
        Emprestimo emprestimo = new Emprestimo(LocalDate.now(), 123, 123);

        assertTrue(emprestimo.estaAtivo());
        assertEquals(StatusEmprestimo.ATIVO, emprestimo.getStatus());
        assertNotNull(emprestimo.getDataEmprestimo());
    }

    @Test
    void registrarDevolucaoDeveFinalizarEmprestimo() throws MaterialDevolvidoException {
        Emprestimo emprestimo = new Emprestimo(LocalDate.now(), 123, 123);

        emprestimo.registrarDevolucao();

        assertFalse(emprestimo.estaAtivo());
        assertEquals(StatusEmprestimo.DEVOLVIDO, emprestimo.getStatus());
    }
}
