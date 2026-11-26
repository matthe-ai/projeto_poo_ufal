package biblioteca.service;

import biblioteca.auth.Sessao;
import biblioteca.exception.*;
import biblioteca.model.material.Livro;
import biblioteca.model.material.MaterialBibliografico;
import biblioteca.model.material.Periodico;
import biblioteca.model.material.TipoPeriodico;
import biblioteca.model.usuario.Permissao;
import biblioteca.repository.MaterialRepositorio;
import biblioteca.util.GerenciarCapas;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

public class CatalogoService extends Service {

    private final MaterialRepositorio repositorio;

    public CatalogoService(
            MaterialRepositorio repositorio,
            AutorizacaoService autorizacaoService,
            Sessao sessao
    )
    {
        super(autorizacaoService, sessao);
        this.repositorio = repositorio;
    }

    private void validarAnoPublicacao(int anoPublicacao) throws CredenciaisInvalidasException {
        if (anoPublicacao <= 0 || anoPublicacao > LocalDate.now().getYear()) {
            throw new CredenciaisInvalidasException();
        }
    }

    private void validarLivro(
            String titulo,
            int anoPublicacao,
            String autor,
            String isbn,
            String editora,
            String capa
    ) throws CredenciaisInvalidasException {
        this.validarStrings(titulo);
        this.validarStrings(editora);
        this.validarStrings(autor);
        this.validarStrings(isbn);
        this.validarAnoPublicacao(anoPublicacao);
        this.validarCapa(capa);
    }

    private void validarCapa(String capa) throws CredenciaisInvalidasException {

        if (capa == null || capa.isBlank()) {
            throw new CredenciaisInvalidasException();
        }

        // Verifica se foi salva externamente
        File arquivo = GerenciarCapas.getCapa(capa);

        if (arquivo.isFile() && arquivo.canRead()) {
            return;
        }

        // Verifica se está dentro dos recursos do projeto/JAR
        URL recurso = GerenciarCapas.class.getResource("/" + capa);

        if (recurso != null) {
            return;
        }

        throw new CredenciaisInvalidasException();
    }

    private void validarDataPublicacao(String dataPublicacao) throws CredenciaisInvalidasException {

        this.validarStrings(dataPublicacao);

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("dd/MM/uuuu")
                .withResolverStyle(ResolverStyle.STRICT);

        try {
            LocalDate data = LocalDate.parse(dataPublicacao, formatter);
            if (data.isAfter(LocalDate.now())) {
                throw new CredenciaisInvalidasException();
            }
        } catch (DateTimeParseException e) {
            throw new CredenciaisInvalidasException();
        }
    }

    private void validarPeriodico
            (
                    String titulo,
                    String editora, int numeroEdicao,
                    String dataPublicacao,
                    String capa
            ) throws CredenciaisInvalidasException {

        this.validarStrings(titulo);
        this.validarStrings(editora);
        this.validarDataPublicacao(dataPublicacao);
        this.validarCapa(capa);

        if (numeroEdicao <= 0) {
            throw new CredenciaisInvalidasException();
        }
    }

    public Livro cadastrarLivro
            (
                    String titulo,
                    int anoPublicacao,
                    String autor,
                    String isbn,
                    String editora,
                    String capa
            )
            throws OperacaoNaoPermitidaException, ItemDuplicadoException, UsuarioNaoAutenticadoException, CredenciaisInvalidasException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.GERENCIAR_CATALOGO);

        this.validarLivro(titulo, anoPublicacao, autor, isbn, editora, capa);
        Livro novoLivro = new Livro(titulo, anoPublicacao, autor, isbn, editora, capa);
        repositorio.adicionar(novoLivro);

        return novoLivro;
    }

    public Periodico cadastrarPeriodico
            (
                    String titulo,
                    int anoPublicacao,
                    String editora,
                    int numeroEdicao,
                    String dataPublicacao,
                    TipoPeriodico tipoPeriodico,
                    String capa
            )
            throws OperacaoNaoPermitidaException, ItemDuplicadoException, UsuarioNaoAutenticadoException, CredenciaisInvalidasException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.GERENCIAR_CATALOGO);

        this.validarPeriodico(titulo, editora, numeroEdicao, dataPublicacao, capa);
        Periodico novoPeriodico = new Periodico(titulo, editora, numeroEdicao, dataPublicacao, tipoPeriodico, capa);
        repositorio.adicionar(novoPeriodico);
        return novoPeriodico;
    }

    public MaterialBibliografico buscarMaterial(int materialId)
            throws
            OperacaoNaoPermitidaException,
            RegistroNaoEncontradoException,
            UsuarioNaoAutenticadoException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_CATALOGO);

        return repositorio.buscarPorId(materialId);
    }

    public List<MaterialBibliografico> listarMateriais() throws OperacaoNaoPermitidaException, UsuarioNaoAutenticadoException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_CATALOGO);

        return repositorio.listarTodos();
    }

    public List<MaterialBibliografico> listarMateriaisPorTitulo(String titulo) throws OperacaoNaoPermitidaException, UsuarioNaoAutenticadoException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_CATALOGO);

        return repositorio.listarPorTitulo(titulo);
    }

    public void atualizarLivro(
            String titulo,
            int anoPublicacao,
            String editora,
            String capa,
            String autor,
            String isbn,
            int livroId
    )
            throws OperacaoNaoPermitidaException,
            RegistroNaoEncontradoException,
            UsuarioNaoAutenticadoException,
            TipoMaterialInvalidoException,
            CredenciaisInvalidasException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.GERENCIAR_CATALOGO);

        MaterialBibliografico livroParaAtualizar = repositorio.buscarPorId(livroId);

        if (!(livroParaAtualizar instanceof Livro)) {
            throw new TipoMaterialInvalidoException(livroParaAtualizar);
        }

        this.validarLivro(titulo, anoPublicacao, autor, isbn, editora, capa);
        Livro livroAtualizado = new Livro(titulo, anoPublicacao, autor, isbn, editora, livroId, capa);
        repositorio.atualizar(livroAtualizado);
    }

    public void atualizarPeriodico
            (
                    String titulo,
                    String editora,
                    String capa,
                    int numeroEdicao,
                    String dataPublicacao,
                    int periodicoId
            )
            throws OperacaoNaoPermitidaException,
            RegistroNaoEncontradoException,
            UsuarioNaoAutenticadoException,
            TipoMaterialInvalidoException,
            CredenciaisInvalidasException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.GERENCIAR_CATALOGO);
        MaterialBibliografico periodico = repositorio.buscarPorId(periodicoId);

        if (!(periodico instanceof Periodico)) {
            throw new TipoMaterialInvalidoException(periodico);
        }

        this.validarPeriodico(titulo, editora, numeroEdicao, dataPublicacao, capa);
        TipoPeriodico tipoPeriodico = ((Periodico) periodico).getTipoPeriodico();
        Periodico periodicoAtualizado = new Periodico(titulo, editora, numeroEdicao, dataPublicacao, tipoPeriodico, periodicoId, capa);
        repositorio.atualizar(periodicoAtualizado);
    }

    public void removerMaterial(int materialId) throws
            OperacaoNaoPermitidaException,
            RegistroNaoEncontradoException,
            UsuarioNaoAutenticadoException,
            MaterialIndisponivelException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.GERENCIAR_CATALOGO);

        MaterialBibliografico materialParaRemover = repositorio.buscarPorId(materialId);

        if (!materialParaRemover.estaDisponivel()) {
            throw new MaterialIndisponivelException();
        }

        repositorio.remover(materialId);
    }
}
