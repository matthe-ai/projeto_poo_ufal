package biblioteca.service;

import biblioteca.auth.SenhaHashUtil;
import biblioteca.auth.Sessao;
import biblioteca.exception.*;
import biblioteca.model.Emprestimo;
import biblioteca.model.StatusEmprestimo;
import biblioteca.model.usuario.Cargo;
import biblioteca.model.usuario.Permissao;
import biblioteca.model.usuario.Usuario;
import biblioteca.repository.EmprestimoRepositorio;
import biblioteca.repository.UsuarioRepositorio;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioService extends Service  {

    private final UsuarioRepositorio usuarioRepositorio;
    private final EmprestimoRepositorio emprestimoRepositorio;

    public UsuarioService(
            UsuarioRepositorio usuarioRepositorio,
            EmprestimoRepositorio emprestimoRepositorio,
            AutorizacaoService autorizacaoService,
            Sessao sessao
    )
    {
        super(autorizacaoService, sessao);
        this.usuarioRepositorio = usuarioRepositorio;
        this.emprestimoRepositorio = emprestimoRepositorio;
    }

    private void validarSenha(String senha)
            throws CredenciaisInvalidasException {

        this.validarStrings(senha);

        if (senha.length() < 6 || senha.length() > 16) {
            throw new CredenciaisInvalidasException();
        }
    }

    public void logar(String login, String senha)
            throws
            CredenciaisInvalidasException,
            RegistroNaoEncontradoException,
            NoSuchAlgorithmException,
            InvalidKeySpecException {

        this.validarStrings(login);
        this.validarSenha(senha);
        Usuario usuarioParaLogar = usuarioRepositorio.buscarPorLogin(login);

        if (usuarioParaLogar.getSenha().equals(SenhaHashUtil.hash(senha))) {
            this.sessao.iniciarSessao(usuarioParaLogar);
            return;
        }

       throw new CredenciaisInvalidasException();
    }

    public  void deslogar() throws UsuarioNaoAutenticadoException {

       this.verificarAutenticacao();
       this.sessao.encerrarSessao();
    }

    public Usuario cadastrarUsuario
            (
                    String nome,
                    String login,
                    String senha,
                    Cargo cargo
            )
            throws
            CredenciaisInvalidasException,
            ItemDuplicadoException,
            OperacaoNaoPermitidaException,
            UsuarioNaoAutenticadoException,
            NoSuchAlgorithmException,
            InvalidKeySpecException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CADASTRAR_USUARIO);

        this.validarStrings(nome);
        this.validarStrings(login);
        this.validarSenha(senha);
        String senhaHash = SenhaHashUtil.hash(senha);
        Usuario novoUsuario = new Usuario(nome, login, senhaHash, cargo);
        usuarioRepositorio.adicionar(novoUsuario);

        return novoUsuario;
    }
    public Usuario buscarUsuario(int id) throws RegistroNaoEncontradoException, OperacaoNaoPermitidaException, UsuarioNaoAutenticadoException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_USUARIOS);

        return usuarioRepositorio.buscarPorId(id);
    }
    public List<Usuario> listarUsuarios() throws OperacaoNaoPermitidaException, UsuarioNaoAutenticadoException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_USUARIOS);

        return usuarioRepositorio.listarTodos();
    }

    public List<Usuario> listarUsuariosPorLogin(String login) throws OperacaoNaoPermitidaException, UsuarioNaoAutenticadoException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_USUARIOS);

        return usuarioRepositorio.listarPorLogin(login);
    }

    public List<Usuario> listarUsuariosPorNome(String nome) throws OperacaoNaoPermitidaException, UsuarioNaoAutenticadoException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.CONSULTAR_USUARIOS);

        return usuarioRepositorio.listarPorNome(nome);
    }

    public void atualizarUsuario
            (
                String nome,
                String login,
                int usuarioId
            )
            throws OperacaoNaoPermitidaException,
            RegistroNaoEncontradoException,
            UsuarioNaoAutenticadoException,
            CredenciaisInvalidasException,
            ItemDuplicadoException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.ATUALIZAR_USUARIOS);

        this.validarStrings(nome);
        this.validarStrings(login);
        Usuario usuarioParaAtualizar = usuarioRepositorio.buscarPorId(usuarioId);
        boolean usuarioComMesmoLogin = usuarioRepositorio.existeOutroUsuarioComLogin(login, usuarioId);

        if (usuarioComMesmoLogin) {
            throw new ItemDuplicadoException("usuário", "login");
        }

        String senha = usuarioParaAtualizar.getSenha();
        Cargo cargo = usuarioParaAtualizar.getCargo();
        Usuario usuarioAtualizado = new Usuario(nome, login, senha, cargo, usuarioId);
        usuarioRepositorio.atualizar(usuarioAtualizado);
    }

    public void atualizarSenhaDoUsuario(String senhaNova, int usuarioId) throws RegistroNaoEncontradoException, NoSuchAlgorithmException, InvalidKeySpecException, CredenciaisInvalidasException, UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.ATUALIZAR_USUARIOS);
        this.validarSenha(senhaNova);
        Usuario usuarioParaAtualizar = usuarioRepositorio.buscarPorId(usuarioId);

        String nome = usuarioParaAtualizar.getNome();
        String login = usuarioParaAtualizar.getLogin();
        String senhaNovaHash = SenhaHashUtil.hash(senhaNova);
        Cargo cargo = usuarioParaAtualizar.getCargo();

        Usuario usuarioAtualizado = new Usuario(nome, login, senhaNovaHash, cargo, usuarioId);
        usuarioRepositorio.atualizar(usuarioAtualizado);
    }

    public void atualizarMeusDados
            (
                    String nome,
                    String login
            )
            throws OperacaoNaoPermitidaException,
            RegistroNaoEncontradoException,
            UsuarioNaoAutenticadoException,
            CredenciaisInvalidasException, ItemDuplicadoException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.ATUALIZAR_PROPRIOS_DADOS);

        this.validarStrings(nome);
        this.validarStrings(login);
        Usuario usuarioParaAtualizar = sessao.getUsuarioLogado();
        String senha = usuarioParaAtualizar.getSenha();
        Cargo cargo = usuarioParaAtualizar.getCargo();
        int usuarioId = usuarioParaAtualizar.getID();

        boolean usuarioComMesmoLogin = usuarioRepositorio.existeOutroUsuarioComLogin(login, usuarioId);

        if (usuarioComMesmoLogin) {
            throw new ItemDuplicadoException("usuário", "login");
        }

        Usuario usuarioAtualizado = new Usuario(nome, login, senha, cargo, usuarioId);
        usuarioRepositorio.atualizar(usuarioAtualizado);
        this.sessao.iniciarSessao(usuarioAtualizado);
    }

    public void atualizarMinhaSenha(String senhaAtual, String senhaNova) throws RegistroNaoEncontradoException, NoSuchAlgorithmException, InvalidKeySpecException, CredenciaisInvalidasException, UsuarioNaoAutenticadoException, OperacaoNaoPermitidaException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.ATUALIZAR_PROPRIOS_DADOS);

        Usuario usuarioParaAtualizar = sessao.getUsuarioLogado();
        String senhaAtualHash = SenhaHashUtil.hash(senhaAtual);

        if (!usuarioParaAtualizar.getSenha().equals(senhaAtualHash)) {
            throw new CredenciaisInvalidasException();
        }

        this.validarSenha(senhaNova);
        String nome = usuarioParaAtualizar.getNome();
        String login = usuarioParaAtualizar.getLogin();
        String senhaNovaHash = SenhaHashUtil.hash(senhaNova);
        Cargo cargo = usuarioParaAtualizar.getCargo();
        int usuarioId = usuarioParaAtualizar.getID();
        
        Usuario usuarioAtualizado = new Usuario(nome, login, senhaNovaHash, cargo, usuarioId);
        usuarioRepositorio.atualizar(usuarioAtualizado);
        this.sessao.iniciarSessao(usuarioAtualizado);
    }

    public void removerUsuario(int usuarioId)
            throws
            RegistroNaoEncontradoException,
            OperacaoNaoPermitidaException,
            UsuarioNaoAutenticadoException,
            UsuarioPossuiEmprestimosAtivosException {

        this.verificarAutenticacao();
        autorizacaoService.exigirPermissao(sessao.getUsuarioLogado(), Permissao.REMOVER_USUARIO);

        if (usuarioId == sessao.getUsuarioLogado().getID()) {
            throw  new OperacaoNaoPermitidaException();
        }

        Usuario usuarioParaRemover = usuarioRepositorio.buscarPorId(usuarioId);

        if (usuarioParaRemover.getCargo() == Cargo.ADMINISTRADOR) {
            throw  new OperacaoNaoPermitidaException();
        }

        List<Emprestimo> emprestimosDoUsuario = emprestimoRepositorio.listarPorUsuario(usuarioId);
        List<Emprestimo> emprestimosAtivos = new ArrayList<>();

        for (Emprestimo emprestimo : emprestimosDoUsuario) {
            if (emprestimo.getStatus() == StatusEmprestimo.ATIVO) {
                emprestimosAtivos.add(emprestimo);
            }
        }

        if (!emprestimosAtivos.isEmpty()) {
            throw new UsuarioPossuiEmprestimosAtivosException();
        }

        usuarioRepositorio.remover(usuarioId);
    }
}
