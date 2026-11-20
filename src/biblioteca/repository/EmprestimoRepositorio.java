package biblioteca.repository;

import java.util.ArrayList;
import java.util.List;

import biblioteca.exception.ItemDuplicadoException;
import biblioteca.exception.RegistroNaoEncontradoException;
import biblioteca.model.Emprestimo;
import biblioteca.model.StatusEmprestimo;

public class EmprestimoRepositorio implements Repositorio<Emprestimo> {

    private final ArrayList<Emprestimo> emprestimos = new ArrayList<Emprestimo>();
	
	@Override
    public void adicionar(Emprestimo emprestimo) throws ItemDuplicadoException {

        for (Emprestimo e : emprestimos) {
            if (e.getId() == emprestimo.getId()) {
                throw new ItemDuplicadoException("empréstimo");
            }
        }
        emprestimos.add(emprestimo);
    }
	
	@Override
    public Emprestimo buscarPorId(int emprestimoId) throws RegistroNaoEncontradoException {

        for (Emprestimo emprestimo : emprestimos) {
            if (emprestimo.getId() == emprestimoId) {
                return emprestimo;
            }
        }

        throw new RegistroNaoEncontradoException();
    }
	
	@Override
    public List<Emprestimo> listarTodos() {
        return new ArrayList<Emprestimo>(emprestimos);
    }

    public List<Emprestimo> listarPorStatus(StatusEmprestimo status) {
        List<Emprestimo> emprestimosFiltrados = new ArrayList<>();

        for (Emprestimo emprestimo : emprestimos) {
            if (emprestimo.getStatus() == status) {
                emprestimosFiltrados.add(emprestimo);
            }
        }

        return emprestimosFiltrados;
    }

    public List<Emprestimo> listarPorUsuario(int usuarioId) {
        List<Emprestimo> emprestimosFiltrados = new ArrayList<>();

        for (Emprestimo emprestimo : emprestimos) {
            if (emprestimo.getUsuario().getID() == (usuarioId)) {
                emprestimosFiltrados.add(emprestimo);
            }
        }

        return emprestimosFiltrados;
    }

    public List<Emprestimo> listarPorUsuarioEStatus(int usuarioId, StatusEmprestimo status) {
        List<Emprestimo> emprestimosFiltrados = new ArrayList<>();
        List<Emprestimo> emprestimosDoUsuario = this.listarPorUsuario(usuarioId);

        for (Emprestimo emprestimo : emprestimosDoUsuario) {
            if (emprestimo.getStatus() == status) {
                emprestimosFiltrados.add(emprestimo);
            }
        }

        return emprestimosFiltrados;
    }

    @Override
    public void atualizar(Emprestimo emprestimo) throws RegistroNaoEncontradoException {

        for (int i = 0; i < emprestimos.size(); i++) {
            if (emprestimos.get(i).getId() == emprestimo.getId()) {
                emprestimos.set(i, emprestimo);
                return;
            }
        }

        throw new RegistroNaoEncontradoException();
    }
}
