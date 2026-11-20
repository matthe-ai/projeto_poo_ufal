package biblioteca.repository;

import biblioteca.exception.ItemDuplicadoException;
import biblioteca.exception.RegistroNaoEncontradoException;
import biblioteca.model.SolicitacaoEmprestimo;
import biblioteca.model.StatusSolicitacao;

import java.util.ArrayList;
import java.util.List;

public class SolicitacaoEmprestimoRepositorio implements Repositorio<SolicitacaoEmprestimo> {

    private final ArrayList<SolicitacaoEmprestimo> solitacoesEmprestimos = new ArrayList<>();

    @Override
    public void adicionar(SolicitacaoEmprestimo solicitacaoEmprestimo) throws ItemDuplicadoException {
        for (SolicitacaoEmprestimo e : solitacoesEmprestimos) {
            if (e.getId() == solicitacaoEmprestimo.getId()) {
                throw new ItemDuplicadoException("solicitação");
            }
        }
        solitacoesEmprestimos.add(solicitacaoEmprestimo);
    }

    @Override
    public SolicitacaoEmprestimo buscarPorId(int solicitacaoId) throws RegistroNaoEncontradoException {
        for (SolicitacaoEmprestimo solicitacaoEmprestimo : solitacoesEmprestimos) {
            if (solicitacaoEmprestimo.getId() == solicitacaoId) {
                return solicitacaoEmprestimo;
            }
        }

        throw new RegistroNaoEncontradoException();
    }

    @Override
    public List<SolicitacaoEmprestimo> listarTodos() {
        return new ArrayList<SolicitacaoEmprestimo>(solitacoesEmprestimos);
    }

    public List<SolicitacaoEmprestimo> listarPorStatus(StatusSolicitacao status) {
        List<SolicitacaoEmprestimo> solicitacoesFiltradas = new ArrayList<>();

        for (SolicitacaoEmprestimo solicitacao : solitacoesEmprestimos) {
            if (solicitacao.getStatus() == status) {
                solicitacoesFiltradas.add(solicitacao);
            }
        }

        return solicitacoesFiltradas;
    }

    public List<SolicitacaoEmprestimo> listarPorUsuario(int usuarioId) {
        List<SolicitacaoEmprestimo> solicitacoesFiltradas = new ArrayList<>();

        for (SolicitacaoEmprestimo solicitacao : solitacoesEmprestimos) {
            if (solicitacao.getSolicitante().getID() == usuarioId) {
                solicitacoesFiltradas.add(solicitacao);
            }
        }

        return solicitacoesFiltradas;
    }

    public List<SolicitacaoEmprestimo> listarPorUsuarioEStatus(int usuarioId, StatusSolicitacao status) {
        List<SolicitacaoEmprestimo> solicitacoesFiltradas = new ArrayList<>();
        List<SolicitacaoEmprestimo> solicitacoesDoUsuario = this.listarPorUsuario(usuarioId);

        for (SolicitacaoEmprestimo solicitacao : solicitacoesDoUsuario) {
            if (solicitacao.getStatus() == status) {
                solicitacoesFiltradas.add(solicitacao);
            }
        }

        return solicitacoesFiltradas;
    }

    @Override
    public void atualizar(SolicitacaoEmprestimo solicitacao) throws RegistroNaoEncontradoException {
        for (int i = 0; i < solitacoesEmprestimos.size(); i++) {

            if (solitacoesEmprestimos.get(i).getId() == solicitacao.getId()) {
                solitacoesEmprestimos.set(i, solicitacao);
                return;
            }
        }

        throw new RegistroNaoEncontradoException();
    }
}
