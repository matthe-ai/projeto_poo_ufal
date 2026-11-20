package biblioteca.service;

import biblioteca.auth.Sessao;
import biblioteca.exception.*;
import biblioteca.model.SolicitacaoEmprestimo;
import biblioteca.model.StatusSolicitacao;
import biblioteca.model.material.MaterialBibliografico;
import biblioteca.model.usuario.Permissao;
import biblioteca.repository.MaterialRepositorio;
import biblioteca.repository.SolicitacaoEmprestimoRepositorio;

import java.util.List;

public class SolicitacaoEmprestimoService extends Service {

    private final SolicitacaoEmprestimoRepositorio solicitacaoEmprestimoRepositorio;
    private final MaterialRepositorio materialRepositorio;
    private final EmprestimoService emprestimoService;

    public SolicitacaoEmprestimoService
            (
            AutorizacaoService autorizacaoService,
            Sessao sessao,
            SolicitacaoEmprestimoRepositorio solicitacaoEmprestimoRepositorio,
            MaterialRepositorio materialRepositorio,
            EmprestimoService emprestimoService
            ) {
        super(autorizacaoService, sessao);
        this.solicitacaoEmprestimoRepositorio = solicitacaoEmprestimoRepositorio;
        this.materialRepositorio = materialRepositorio;
        this.emprestimoService = emprestimoService;
    }

    // Impede que o usuário spame solicitações para um mesmo material.
    private void verificarSolicitacaoPendenteDuplicada(int materialId) throws ItemDuplicadoException {
        List<SolicitacaoEmprestimo> solicitacoesPendentesDoUsuario =
                solicitacaoEmprestimoRepositorio.listarPorUsuarioEStatus(sessao.getUsuarioLogado().getID(), StatusSolicitacao.PENDENTE);

        for (SolicitacaoEmprestimo solicitacaoEmprestimo : solicitacoesPendentesDoUsuario) {
            if (solicitacaoEmprestimo.getMaterialSolicitado().getId() == materialId) {
                throw new ItemDuplicadoException("solicitação pendente", "material");
            }
        }
    }

    public void solicitarEmprestimo(int materialId)
            throws UsuarioNaoAutenticadoException,
            OperacaoNaoPermitidaException,
            RegistroNaoEncontradoException,
            ItemDuplicadoException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.SOLICITAR_EMPRESTIMO);

        MaterialBibliografico materialSolicitado = materialRepositorio.buscarPorId(materialId);
        this.verificarSolicitacaoPendenteDuplicada(materialId);

        SolicitacaoEmprestimo novaSolicitacao = new SolicitacaoEmprestimo(sessao.getUsuarioLogado(), materialSolicitado);
        solicitacaoEmprestimoRepositorio.adicionar(novaSolicitacao);
    }

    public List<SolicitacaoEmprestimo> listarSolicitacoes() throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {
        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_TODAS_SOLICITACOES);

        return solicitacaoEmprestimoRepositorio.listarTodos();
    }

    public List<SolicitacaoEmprestimo> listarSolicitacoesPorStatus(StatusSolicitacao status) throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {
        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_TODAS_SOLICITACOES);

        return solicitacaoEmprestimoRepositorio.listarPorStatus(status);
    }

    public List<SolicitacaoEmprestimo> listarSolicitacoesPorUsuario(int solicitanteId) throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {
        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_TODAS_SOLICITACOES);

        return solicitacaoEmprestimoRepositorio.listarPorUsuario(solicitanteId);
    }

    public List<SolicitacaoEmprestimo> listarSolicitacoesPorUsuarioEStatus(int solicitanteId, StatusSolicitacao status) throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_TODAS_SOLICITACOES);

        return solicitacaoEmprestimoRepositorio.listarPorUsuarioEStatus(solicitanteId, status);
    }

    public List<SolicitacaoEmprestimo> listarMinhasSolicitacoes() throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {
        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_PROPRIAS_SOLICITACOES);

        return solicitacaoEmprestimoRepositorio.listarPorUsuario(sessao.getUsuarioLogado().getID());
    }

    public List<SolicitacaoEmprestimo> listarMinhasSolicitacoesPorStatus(StatusSolicitacao status) throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {
        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_PROPRIAS_SOLICITACOES);

        return solicitacaoEmprestimoRepositorio.listarPorUsuarioEStatus(sessao.getUsuarioLogado().getID(), status);
    }

    private void validarSolicitacaoPendente(SolicitacaoEmprestimo solicitacao) throws SolicitacaoJaProcessadaException {

        if (solicitacao.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new SolicitacaoJaProcessadaException();
        }
    }

    public void aprovarSolicitacao(int solicitacaoId)
            throws UsuarioNaoAutenticadoException,
            OperacaoNaoPermitidaException,
            RegistroNaoEncontradoException,
            ItemDuplicadoException,
            LimiteEmprestimosException,
            MaterialIndisponivelException,
            SolicitacaoJaProcessadaException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.GERENCIAR_SOLICITACOES);

        SolicitacaoEmprestimo solicitacao = solicitacaoEmprestimoRepositorio.buscarPorId(solicitacaoId);
        this.validarSolicitacaoPendente(solicitacao);

        this.emprestimoService.registrarEmprestimo(solicitacao.getSolicitante().getID(), solicitacao.getMaterialSolicitado().getId());
        SolicitacaoEmprestimo solicitacaoAtualizada = new SolicitacaoEmprestimo(
                solicitacao.getSolicitante(),
                solicitacao.getMaterialSolicitado(),
                StatusSolicitacao.APROVADA,
                solicitacao.getId()
        );
        solicitacaoEmprestimoRepositorio.atualizar(solicitacaoAtualizada);
    }
    public void rejeitarSolicitacao(int solicitacaoId)
            throws UsuarioNaoAutenticadoException,
            OperacaoNaoPermitidaException,
            RegistroNaoEncontradoException,
            SolicitacaoJaProcessadaException {
        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.GERENCIAR_SOLICITACOES);

        SolicitacaoEmprestimo solicitacao = solicitacaoEmprestimoRepositorio.buscarPorId(solicitacaoId);
        this.validarSolicitacaoPendente(solicitacao);
        SolicitacaoEmprestimo solicitacaoAtualizada = new SolicitacaoEmprestimo(
                solicitacao.getSolicitante(),
                solicitacao.getMaterialSolicitado(),
                StatusSolicitacao.REJEITADA,
                solicitacao.getId()
        );
        solicitacaoEmprestimoRepositorio.atualizar(solicitacaoAtualizada);
    }
}
