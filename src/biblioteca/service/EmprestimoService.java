package biblioteca.service;

import biblioteca.auth.Sessao;
import biblioteca.exception.*;
import biblioteca.model.Emprestimo;
import biblioteca.model.Multa;
import biblioteca.model.StatusEmprestimo;
import biblioteca.model.StatusMulta;
import biblioteca.model.material.Livro;
import biblioteca.model.material.MaterialBibliografico;
import biblioteca.model.material.Periodico;
import biblioteca.model.material.TipoPeriodico;
import biblioteca.model.usuario.Permissao;
import biblioteca.model.usuario.Usuario;
import biblioteca.repository.EmprestimoRepositorio;
import biblioteca.repository.MaterialRepositorio;
import biblioteca.repository.UsuarioRepositorio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class EmprestimoService extends Service {

    private final EmprestimoRepositorio emprestimoRepositorio;
    private final MaterialRepositorio materialRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final MultaService multaService;

    public static final int MAXIMO_EMPRESTIMOS_ATIVOS = 3;
    public static final int PRAZO_EMPRESTIMO_LIVRO = 14;
    public static final int PRAZO_EMPRESTIMO_JORNAL = 3;
    public static final int PRAZO_EMPRESTIMO_REVISTA = 2;

    public EmprestimoService
            (
                    EmprestimoRepositorio emprestimoRepositorio,
                    MaterialRepositorio materialRepositorio,
                    UsuarioRepositorio usuarioRepositorio,
                    AutorizacaoService autorizacaoService,
                    Sessao sessao,
                    MultaService multaService
            ) {
            super(autorizacaoService, sessao);
            this.emprestimoRepositorio = emprestimoRepositorio;
            this.materialRepositorio = materialRepositorio;
            this.usuarioRepositorio = usuarioRepositorio;
            this.multaService = multaService;
    }

    private void verificarEmprestimosAtivosDoUsuario(int usuarioId) throws LimiteEmprestimosException {
        List<Emprestimo> emprestimosDoUsuario = emprestimoRepositorio.listarPorUsuario(usuarioId);
        List<Emprestimo> emprestimosAtivos = new ArrayList<>();

        for (Emprestimo emprestimo : emprestimosDoUsuario) {
            if (emprestimo.getStatus().equals(StatusEmprestimo.ATIVO)) {
                emprestimosAtivos.add(emprestimo);
            }
        }

        if (emprestimosAtivos.size() >= MAXIMO_EMPRESTIMOS_ATIVOS) {
            throw new LimiteEmprestimosException();
        }
    }

    private void verificarMultasPendentesDoUsuario(int usuarioId) throws UsuarioPossuiMultasPendentes, OperacaoNaoPermitidaException, UsuarioNaoAutenticadoException {

        List<Multa> multasPendentesDoUsuario = multaService.listarMultasPorUsuarioEStatus(usuarioId, StatusMulta.PENDENTE);

        if (!multasPendentesDoUsuario.isEmpty()) {
            throw new UsuarioPossuiMultasPendentes();
        }
    }

    private LocalDate calcularDataPrevista(MaterialBibliografico materialParaEmprestar) {

        LocalDate dataPrevistaDevolucao = LocalDate.now();

        if (materialParaEmprestar instanceof Livro) {
            dataPrevistaDevolucao = dataPrevistaDevolucao.plusDays(PRAZO_EMPRESTIMO_LIVRO);
        } else {
            TipoPeriodico tipoPeriodico = ((Periodico) materialParaEmprestar).getTipoPeriodico();
            switch (tipoPeriodico) {
                case JORNAL -> dataPrevistaDevolucao = dataPrevistaDevolucao.plusDays(PRAZO_EMPRESTIMO_JORNAL);
                case REVISTA -> dataPrevistaDevolucao = dataPrevistaDevolucao.plusDays(PRAZO_EMPRESTIMO_REVISTA);
            }
        }

        return dataPrevistaDevolucao;
    }

    public void registrarEmprestimo
            (
                    int usuarioId,
                    int materialId
            )
            throws UsuarioNaoAutenticadoException,
            OperacaoNaoPermitidaException,
            RegistroNaoEncontradoException,
            MaterialIndisponivelException,
            LimiteEmprestimosException,
            ItemDuplicadoException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.REGISTRAR_EMPRESTIMO);

        MaterialBibliografico materialParaEmprestar = materialRepositorio.buscarPorId(materialId);
        Usuario usuario = usuarioRepositorio.buscarPorId(usuarioId);

        if (!materialParaEmprestar.estaDisponivel()) {
            throw new MaterialIndisponivelException();
        }

        this.verificarEmprestimosAtivosDoUsuario(usuario.getID());
        this.verificarMultasPendentesDoUsuario(usuario.getID());

        LocalDate dataPrevistaDevolucao = calcularDataPrevista(materialParaEmprestar);
        Emprestimo novoEmprestimo = new Emprestimo(dataPrevistaDevolucao, materialParaEmprestar, usuario);
        emprestimoRepositorio.adicionar(novoEmprestimo);
        materialParaEmprestar.marcarComoIndisponivel();
    }

    private long calcularDiasAtraso(LocalDate dataPrevista) {
        LocalDate hoje = LocalDate.now();

        return ChronoUnit.DAYS.between(
                dataPrevista,
                hoje
        );
    }

    // registrarDevolucao: até o momento, apenas muda o status do empréstimo.
    public void registrarDevolucao(int emprestimoId)
            throws
            OperacaoNaoPermitidaException,
            RegistroNaoEncontradoException,
            MaterialDevolvidoException,
            UsuarioNaoAutenticadoException,
            ItemDuplicadoException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.REGISTRAR_DEVOLUCAO);

        Emprestimo emprestimoParaDevolver = emprestimoRepositorio.buscarPorId(emprestimoId);

        if (emprestimoParaDevolver.getStatus() != StatusEmprestimo.ATIVO) {
            throw new MaterialDevolvidoException();
        }

        long diasAtraso = calcularDiasAtraso(emprestimoParaDevolver.getDataPrevistaDevolucao());

        if (diasAtraso > 0) {
            multaService.cadastrarMulta(diasAtraso, emprestimoParaDevolver);
        }
        emprestimoParaDevolver.registrarDevolucao();
        emprestimoParaDevolver.getMaterial().marcarComoDisponivel();
    }
    
    public Emprestimo buscarEmprestimo(int emprestimoId)
            throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException, RegistroNaoEncontradoException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_TODOS_EMPRESTIMOS);

        return emprestimoRepositorio.buscarPorId(emprestimoId);
    }

    public List<Emprestimo> listarEmprestimos() throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_TODOS_EMPRESTIMOS);

        return emprestimoRepositorio.listarTodos();
    }
    
    public List<Emprestimo> listarEmprestimosPorUsuario(int usuarioId) throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_TODOS_EMPRESTIMOS);

        return emprestimoRepositorio.listarPorUsuario(usuarioId);
    }

    public List<Emprestimo> listarEmprestimosPorStatus(StatusEmprestimo status) throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_TODOS_EMPRESTIMOS);

        return emprestimoRepositorio.listarPorStatus(status);
    }

    public List<Emprestimo> listarEmprestimosPorUsuarioEStatus(int usuarioId, StatusEmprestimo status) throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_TODOS_EMPRESTIMOS);

        return emprestimoRepositorio.listarPorUsuarioEStatus(usuarioId, status);
    }

    public List<Emprestimo> listarMeusEmprestimos() throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_PROPRIOS_EMPRESTIMOS);

        return emprestimoRepositorio.listarPorUsuario(sessao.getUsuarioLogado().getID());
    }

    public List<Emprestimo> listarMeusEmprestimosPorStatus(StatusEmprestimo status) throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_PROPRIOS_EMPRESTIMOS);

        return emprestimoRepositorio.listarPorUsuarioEStatus(sessao.getUsuarioLogado().getID(), status);
    }
}
