package biblioteca.service;

import biblioteca.auth.Sessao;
import biblioteca.exception.*;
import biblioteca.model.Emprestimo;
import biblioteca.model.Multa;
import biblioteca.model.StatusMulta;
import biblioteca.model.usuario.Permissao;
import biblioteca.repository.MultaRepositorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MultaService extends Service {

    private static final BigDecimal VALOR_DIARIO = new BigDecimal("2.00");
    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("50.00");
    private final MultaRepositorio multaRepositorio;

    public MultaService(AutorizacaoService autorizacaoService, Sessao sessao, MultaRepositorio multaRepositorio) {
        super(autorizacaoService, sessao);
        this.multaRepositorio = multaRepositorio;
    }

    private BigDecimal calcularValor(long diasAtraso) {

        BigDecimal valor =  VALOR_DIARIO.multiply(BigDecimal.valueOf(diasAtraso));

        if (valor.compareTo(VALOR_MAXIMO) > 0) {
            valor = VALOR_MAXIMO;
        }

        return valor;
    }

    public void cadastrarMulta(long diasAtraso, Emprestimo emprestimo)
            throws ItemDuplicadoException, UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.GERENCIAR_MULTAS);

        BigDecimal valorMulta = calcularValor(diasAtraso);
        Multa novaMulta = new Multa(diasAtraso, valorMulta, emprestimo);
        multaRepositorio.adicionar(novaMulta);
    }

    public void pagarMulta(int multaId) throws MultaJaProcessada, RegistroNaoEncontradoException, UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.GERENCIAR_MULTAS);
        Multa multaParaPagar = multaRepositorio.buscarPorId(multaId);

        if (multaParaPagar.getStatus() != StatusMulta.PENDENTE) {
            throw new MultaJaProcessada();
        }

        long diasAtraso = multaParaPagar.getDiasAtraso();
        BigDecimal valor = multaParaPagar.getValor();
        Emprestimo emprestimoAssociado = multaParaPagar.getEmprestimo();
        LocalDate dataGeracao = multaParaPagar.getDataGeracao();
        Multa multaAtualizada = new Multa(diasAtraso, valor, dataGeracao, emprestimoAssociado, multaId);
        multaAtualizada.registrarPagamento();
        multaRepositorio.atualizar(multaAtualizada);
    }

    public void cancelarMulta(int multaId) throws MultaJaProcessada, RegistroNaoEncontradoException, UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {
        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.GERENCIAR_MULTAS);

        Multa multaParaCancelar = multaRepositorio.buscarPorId(multaId);

        if (multaParaCancelar.getStatus() != StatusMulta.PENDENTE) {
            throw new MultaJaProcessada();
        }

        long diasAtraso = multaParaCancelar.getDiasAtraso();
        BigDecimal valor = multaParaCancelar.getValor();
        Emprestimo emprestimoAssociado = multaParaCancelar.getEmprestimo();
        LocalDate dataGeracao = multaParaCancelar.getDataGeracao();
        Multa multaAtualizada = new Multa(diasAtraso, valor, dataGeracao, emprestimoAssociado, multaId);
        multaAtualizada.registrarCancelamento();
        multaRepositorio.atualizar(multaAtualizada);
    }

    public Multa buscarMulta(int multaId) throws RegistroNaoEncontradoException, UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {
        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_TODAS_MULTAS);
        return multaRepositorio.buscarPorId(multaId);
    }

    public List<Multa> listarMultas() throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {
        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_TODAS_MULTAS);
        return multaRepositorio.listarTodos();
    }

    public List<Multa> listarMultasPorStatus(StatusMulta status) throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {
        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_TODAS_MULTAS);
        return multaRepositorio.listarPorStatus(status);
    }

    public List<Multa> listarMultasPorUsuario(int usuarioId) throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {
        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_TODAS_MULTAS);
        return multaRepositorio.listarPorUsuario(usuarioId);
    }

    public List<Multa> listarMultasPorUsuarioEStatus(int usuarioId, StatusMulta status) throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {
        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_TODAS_MULTAS);
        return multaRepositorio.listarPorUsuarioEStatus(usuarioId, status);
    }

    public List<Multa> listarMinhasMultas() throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {
        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_PROPRIAS_MULTAS);
        int usuarioId = sessao.getUsuarioLogado().getID();
        return multaRepositorio.listarPorUsuario(usuarioId);
    }

    public List<Multa> listarMinhasMultasPorStatus(StatusMulta status) throws UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {
        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_PROPRIAS_MULTAS);
        int usuarioId = sessao.getUsuarioLogado().getID();
        return multaRepositorio.listarPorUsuarioEStatus(usuarioId, status);
    }
}
