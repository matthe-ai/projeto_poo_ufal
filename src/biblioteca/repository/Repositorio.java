package biblioteca.repository;

import biblioteca.exception.ItemDuplicadoException;
import biblioteca.exception.RegistroNaoEncontradoException;
import biblioteca.model.Emprestimo;

import java.util.List;

public interface Repositorio<N> {

	void adicionar(N objeto) throws ItemDuplicadoException;

    N buscarPorId(int id) throws RegistroNaoEncontradoException;

    List<N> listarTodos();

    void atualizar(N objeto) throws RegistroNaoEncontradoException;
}
