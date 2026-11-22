package biblioteca;

import javax.swing.SwingUtilities;

import biblioteca.auth.Sessao;
import biblioteca.exception.CredenciaisInvalidasException;
import biblioteca.repository.EmprestimoRepositorio;
import biblioteca.repository.MaterialRepositorio;
import biblioteca.repository.SolicitacaoEmprestimoRepositorio;
import biblioteca.repository.UsuarioRepositorio;
import biblioteca.service.AutorizacaoService;
import biblioteca.service.CatalogoService;
import biblioteca.service.EmprestimoService;
import biblioteca.service.MultaService;
import biblioteca.service.SolicitacaoEmprestimoService;
import biblioteca.service.UsuarioService;
import biblioteca.repository.MultaRepositorio;
import biblioteca.view.TelaLogin;

public class Main {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {

			// session

			final Sessao sessao = new Sessao();

			// repository
			
			final SolicitacaoEmprestimoRepositorio solicitacaoRepositorio= new SolicitacaoEmprestimoRepositorio();
			
			final UsuarioRepositorio usuarioRepositorio = new UsuarioRepositorio();
			usuarioRepositorio.cadastrarUsuariosIniciais();

			final MaterialRepositorio materialRepositorio = new MaterialRepositorio();
			materialRepositorio.cadastrarMateriaisIniciais();

			final EmprestimoRepositorio emprestimoRepositorio = new EmprestimoRepositorio();
			try {
				// simula emprestimo com atraso de N dias
				emprestimoRepositorio.criarEmprestimoExemplo(usuarioRepositorio.buscarPorLogin("jorge"), materialRepositorio.listarPorTitulo("clean").getFirst(), 15);
				emprestimoRepositorio.criarEmprestimoExemplo(usuarioRepositorio.buscarPorLogin("jorge"), materialRepositorio.listarPorTitulo("revista").getFirst(), 4);
			} catch (CredenciaisInvalidasException e) {
			}

			final MultaRepositorio multaRepositorio = new MultaRepositorio();

			// services

			final AutorizacaoService autorizacaoService = new AutorizacaoService();
			
			final MultaService multaService = new MultaService(autorizacaoService, sessao, multaRepositorio);

			final UsuarioService usuarioService = new UsuarioService(usuarioRepositorio, emprestimoRepositorio, autorizacaoService, sessao);

			final CatalogoService catalogoService = new CatalogoService(materialRepositorio, autorizacaoService, sessao);

			final EmprestimoService emprestimoService = new EmprestimoService(emprestimoRepositorio, materialRepositorio, usuarioRepositorio, autorizacaoService, sessao, multaService);
			
			final SolicitacaoEmprestimoService solicitacaoService = new SolicitacaoEmprestimoService(autorizacaoService, sessao, solicitacaoRepositorio, materialRepositorio, emprestimoService, multaService);

			// Tela inicial
			TelaLogin telalogin = new TelaLogin(usuarioService, catalogoService, emprestimoService, sessao, solicitacaoService, multaService);
			telalogin.setVisible(true);
		});

	}

}
