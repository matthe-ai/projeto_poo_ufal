package biblioteca.repository;

import biblioteca.exception.ItemDuplicadoException;
import biblioteca.exception.RegistroNaoEncontradoException;
import biblioteca.model.Emprestimo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmprestimoRepositorioTest {

    private EmprestimoRepositorio repositorio;
    private Emprestimo emprestimo;

    @BeforeEach
    void setUp() {
        repositorio = new EmprestimoRepositorio();
        emprestimo = new Emprestimo();
    }

    @Test
    void deveCadastrarEmprestimo() throws ItemDuplicadoException, RegistroNaoEncontradoException {
        repositorio.adicionar(emprestimo);

        Emprestimo encontrado = repositorio.buscarPorId(emprestimo.getId());

        assertNotNull(encontrado);
        assertEquals(emprestimo.getId(), encontrado.getId());
    }

    @Test
    void deveListarEmprestimosCadastrados() throws ItemDuplicadoException {
        repositorio.adicionar(emprestimo);

        List<Emprestimo> emprestimos = repositorio.listarTodos();

        assertEquals(1, emprestimos.size());
        assertTrue(emprestimos.contains(emprestimo));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverEmprestimos() {
        List<Emprestimo> emprestimos = repositorio.listarTodos();

        assertNotNull(emprestimos);
        assertTrue(emprestimos.isEmpty());
    }

    @Test
    void deveLancarExcecaoAoBuscarEmprestimoInexistente() {
        assertThrows(
                RegistroNaoEncontradoException.class,
                () -> repositorio.buscarPorId(-1)
        );
    }

    @Test
    void deveAtualizarEmprestimoCadastrado() throws ItemDuplicadoException, RegistroNaoEncontradoException {
        repositorio.adicionar(emprestimo);

        repositorio.atualizar(emprestimo);

        Emprestimo encontrado = repositorio.buscarPorId(emprestimo.getId());

        assertNotNull(encontrado);
        assertEquals(emprestimo.getId(), encontrado.getId());
    }
}