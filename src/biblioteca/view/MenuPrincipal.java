package biblioteca.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import biblioteca.auth.Sessao;
import biblioteca.exception.ItemDuplicadoException;
import biblioteca.exception.OperacaoNaoPermitidaException;
import biblioteca.exception.RegistroNaoEncontradoException;
import biblioteca.exception.UsuarioNaoAutenticadoException;
import biblioteca.model.Multa;
import biblioteca.model.StatusEnums.StatusMulta;
import biblioteca.model.material.Livro;
import biblioteca.model.material.MaterialBibliografico;
import biblioteca.model.material.Periodico;
import biblioteca.model.usuario.Cargo;
import biblioteca.service.CatalogoService;
import biblioteca.service.EmprestimoService;
import biblioteca.service.MultaService;
import biblioteca.service.SolicitacaoEmprestimoService;
import biblioteca.service.UsuarioService;

public class MenuPrincipal extends JFrame {

	// atributos

	private JTextField campoPesquisa;
	private JPanel painelMateriais;

	private CardLayout cardLayout;
	private JPanel painelCentral;

	final private Sessao sessao;
	final private UsuarioService usuarios;
	final private CatalogoService catalogo;
	final private EmprestimoService emprestimos;
	final private SolicitacaoEmprestimoService solicitacaoEmprestimos;
	final private MultaService multaService;

	private final Runnable voltarLogin;

	// construtor

	public MenuPrincipal(Sessao usuario, UsuarioService usuarios, CatalogoService catalogo,
			EmprestimoService emprestimos, Runnable voltarLogin, SolicitacaoEmprestimoService sol, MultaService mus) {
		this.sessao = usuario;
		this.usuarios = usuarios;
		this.catalogo = catalogo;
		this.emprestimos = emprestimos;
		this.voltarLogin = voltarLogin;
		this.solicitacaoEmprestimos = sol;
		this.multaService = mus;
		configurarJanela();
		criarMenu();
		criarConteudo();
		atualizarMateriais();
	}

	// metodos

	private void configurarJanela() {
		setTitle("Biblioteca");
		setSize(800, 650);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setMinimumSize(new Dimension(700, 600));
		setResizable(false);
	}

	private void criarMenu() {
		JMenuBar barraMenu = new JMenuBar();

		JMenu menuPrincipal = new JMenu("Menu");
		JMenu multas = new JMenu("Multas");

		JMenuItem itemInicio = new JMenuItem("Página inicial");
		JMenuItem itemMeusDados = new JMenuItem("Meus dados");
		JMenuItem itemSair = new JMenuItem("Sair");

		itemInicio.addActionListener(e -> {
			cardLayout.show(painelCentral, "CATALOGO");
			this.atualizarMateriais();
		});

		itemMeusDados.addActionListener(e -> {
			cardLayout.show(painelCentral, "MEUS_DADOS");
		});

		itemSair.addActionListener(e -> {
			int opcao = JOptionPane.showConfirmDialog(this, "Deseja realmente sair?", "Sair",
					JOptionPane.YES_NO_OPTION);

			if (opcao == JOptionPane.YES_OPTION) {
				try {
					this.usuarios.deslogar();
					dispose();
					voltarLogin.run();
				} catch (UsuarioNaoAutenticadoException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro de autenticação",
							JOptionPane.ERROR_MESSAGE);
					dispose();
					voltarLogin.run();
				}
			}
		});

		menuPrincipal.add(itemInicio);
		menuPrincipal.add(itemMeusDados);

		barraMenu.add(menuPrincipal);
		barraMenu.add(multas);

		// LEITOR
		if (this.sessao.getUsuarioLogado().getCargo() == Cargo.LEITOR) {
			JMenuItem minhasMultas = this.createMinhasMultas();
			multas.add(minhasMultas);
		}

		// BIBLIOTECARIO
		if (this.sessao.getUsuarioLogado().getCargo() == Cargo.BIBLIOTECARIO
				|| this.sessao.getUsuarioLogado().getCargo() == Cargo.ADMINISTRADOR) {

			JMenuItem itemGerenciar = new JMenuItem("Gerenciar materiais");
			JMenuItem solicitacoes = new JMenuItem("Gerenciar solicitacoes");
			JMenuItem emprestimos = new JMenuItem("Gerenciar emprestimos");
			JMenuItem multasGerenciar = new JMenuItem("Gerenciar multas");

			itemGerenciar.addActionListener(e -> {
				cardLayout.show(painelCentral, "BIBLIOTECARIO");
			});

			solicitacoes.addActionListener(e -> {
				cardLayout.show(painelCentral, "SOLICITACOES");
			});
			
			emprestimos.addActionListener(e -> {
				cardLayout.show(painelCentral, "EMPRESTIMOS");
			});
			
			multasGerenciar.addActionListener(e -> {
				cardLayout.show(painelCentral, "MULTAS");
			});

			multas.add(multasGerenciar);

			menuPrincipal.add(itemGerenciar);
			menuPrincipal.add(solicitacoes);
			menuPrincipal.add(emprestimos);
		}

		// SOMENTE ADMINISTRADOR
		if (this.sessao.getUsuarioLogado().getCargo() == Cargo.ADMINISTRADOR) {

			JMenuItem itemGerenciarUsuarios = new JMenuItem("Gerenciar usuários");

			itemGerenciarUsuarios.addActionListener(e -> {
				cardLayout.show(painelCentral, "ADMINISTRADOR");
			});

			menuPrincipal.add(itemGerenciarUsuarios);

		}

		menuPrincipal.addSeparator();
		menuPrincipal.add(itemSair);

		setJMenuBar(barraMenu);
	}

	private JMenuItem createMinhasMultas() {
		JMenuItem minhasMultas = new JMenuItem("Minhas multas");
		minhasMultas.addActionListener(e -> {
			try {
				if (this.multaService.listarMinhasMultasPorStatus(StatusMulta.PENDENTE).isEmpty()) {
					JOptionPane.showMessageDialog(this, "Você não possui pendências.", "Sem multas",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(this, "Você possui R$"+this.calcularTotal(this.multaService.listarMinhasMultasPorStatus(StatusMulta.PENDENTE))+" pendente.", "Valor pendente", JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (UsuarioNaoAutenticadoException e1) {
				JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro de autenticação",
						JOptionPane.ERROR_MESSAGE);
				dispose();
				voltarLogin.run();
			} catch (OperacaoNaoPermitidaException one) {
				JOptionPane.showMessageDialog(this, one.getMessage(), "Erro de permissão",
						JOptionPane.ERROR_MESSAGE);
			}
		});
		return minhasMultas;
	}

	private String calcularTotal(List<Multa> listarMinhasMultasPorStatus) {
		BigDecimal total = new BigDecimal(0);
		for (Multa multa : listarMinhasMultasPorStatus) {
			total.add(multa.getValor());
		}
		return total.toString();
	}

	private void criarConteudo() {
		JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));

		painelPrincipal.setBackground(new Color(220, 220, 220));
		painelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));

		cardLayout = new CardLayout();
		painelCentral = new JPanel(cardLayout);

		// Adiciona o catálogo como tela inicial
		painelCentral.add(criarPainelCatalogo(), "CATALOGO");

		// Adiciona a tela de dados
		painelCentral.add(
				new MeusDados(this.sessao, this.usuarios, this.emprestimos, voltarLogin, this.solicitacaoEmprestimos),
				"MEUS_DADOS");

		// Adiciona a tela do bibliotecario
		if (this.sessao.getUsuarioLogado().getCargo() == Cargo.BIBLIOTECARIO
				|| this.sessao.getUsuarioLogado().getCargo() == Cargo.ADMINISTRADOR) {
			painelCentral.add(new MenuBibliotecario(this.catalogo, voltarLogin), "BIBLIOTECARIO");
			painelCentral.add(new Solicitacoes(this.solicitacaoEmprestimos, voltarLogin), "SOLICITACOES");
			painelCentral.add(new Multas(multaService, voltarLogin), "MULTAS");
			painelCentral.add(new Emprestimos(this.emprestimos, voltarLogin), "EMPRESTIMOS");
		}

		// Adiciona a tela do administrador
		if (this.sessao.getUsuarioLogado().getCargo() == Cargo.ADMINISTRADOR) {
			painelCentral.add(new MenuAdministrador(this.usuarios, voltarLogin), "ADMINISTRADOR");
		}
		add(painelCentral, BorderLayout.CENTER);
	}

	private JPanel criarPainelCatalogo() {
		JPanel painelCatalogo = new JPanel(new BorderLayout(10, 10));
		painelCatalogo.setBackground(new Color(220, 220, 220));

		// Pesquisa
		JPanel painelPesquisa = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

		painelPesquisa.setBackground(new Color(220, 220, 220));

		campoPesquisa = new JTextField(25);
		campoPesquisa.setPreferredSize(new Dimension(250, 35));

		JButton botaoBuscar = this.createBotaoBuscar();

		painelPesquisa.add(campoPesquisa);
		painelPesquisa.add(botaoBuscar);

		painelCatalogo.add(painelPesquisa, BorderLayout.NORTH);

		// Lista de materiais
		painelMateriais = new JPanel();

		painelMateriais.setLayout(new BoxLayout(painelMateriais, BoxLayout.Y_AXIS));

		painelMateriais.setBackground(new Color(220, 220, 220));

		JScrollPane scroll = new JScrollPane(painelMateriais);
		scroll.setBorder(null);
		scroll.getVerticalScrollBar().setUnitIncrement(15);

		painelCatalogo.add(scroll, BorderLayout.CENTER);

		return painelCatalogo;
	}

	private JButton createBotaoBuscar() {
		JButton botaoBuscar = new JButton("Buscar obra");

		botaoBuscar.addActionListener(e -> {
			String tituloBusca = this.campoPesquisa.getText();
			if (tituloBusca.isBlank()) {
				this.atualizarMateriais();
			} else {
				List<MaterialBibliografico> resultadoBusca;
				try {
					resultadoBusca = this.catalogo.listarMateriaisPorTitulo(tituloBusca);
					this.carregarMateriais(resultadoBusca);
				} catch (OperacaoNaoPermitidaException one) {
					JOptionPane.showMessageDialog(this, one.getMessage(), "Erro de permissão",
							JOptionPane.ERROR_MESSAGE);
				} catch (UsuarioNaoAutenticadoException une) {
					JOptionPane.showMessageDialog(this, une.getMessage(), "Erro de autenticação",
							JOptionPane.ERROR_MESSAGE);
					dispose();
					voltarLogin.run();
				}
			}
		});
		return botaoBuscar;
	}

	private void carregarMateriais(List<MaterialBibliografico> materiais) {
		this.painelMateriais.removeAll();

		for (MaterialBibliografico materialBibliografico : materiais) {
			this.adicionarMaterial(materialBibliografico);
		}

		this.painelMateriais.revalidate();
		this.painelMateriais.repaint();
	}

	private void adicionarMaterial(MaterialBibliografico material) {
		JPanel card = new JPanel(new BorderLayout(15, 5));

		card.setBackground(Color.WHITE);
		card.setBorder(new EmptyBorder(12, 12, 12, 12));
		card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
		card.setPreferredSize(new Dimension(750, 120));

		// CAPA
		JPanel capa = new JPanel(new GridBagLayout());
		capa.setPreferredSize(new Dimension(75, 90));
		capa.setBackground(new Color(210, 210, 210));

		ImageIcon capa_material = new ImageIcon(material.getCaminhoCapa().toString());
		Image imagemRedimencionada = capa_material.getImage().getScaledInstance(81, 93, Image.SCALE_SMOOTH);
		capa_material = new ImageIcon(imagemRedimencionada);

		capa.add(new JLabel(capa_material));
		card.add(capa, BorderLayout.WEST);

		// INFORMACOES
		JPanel informacoes = new JPanel();
		informacoes.setLayout(new BoxLayout(informacoes, BoxLayout.Y_AXIS));
		informacoes.setBackground(Color.WHITE);
		informacoes.add(new JLabel("Título: " + material.getTitulo()));
		informacoes.add(Box.createVerticalStrut(5));

		if (material instanceof Livro) {
			this.criarInformacoes(informacoes, (Livro) material);
		} else {
			this.criarInformacoes(informacoes, (Periodico) material);
		}

		informacoes.add(Box.createVerticalStrut(5));
		informacoes.add(new JLabel("Editora: " + material.getEditora()));

		card.add(informacoes, BorderLayout.CENTER);

		// DISPONIBILIDADE E EMPRESTIMO
		JPanel painelAcao = new JPanel();
		painelAcao.setPreferredSize(new Dimension(155, 100));
		painelAcao.setMinimumSize(new Dimension(155, 100));

		painelAcao.setLayout(new BoxLayout(painelAcao, BoxLayout.Y_AXIS));

		painelAcao.setBackground(Color.WHITE);

		JLabel status = new JLabel(material.estaDisponivel() ? "Disponível" : "Indisponível");

		status.setOpaque(true);
		status.setForeground(Color.BLACK);

		status.setBackground(material.estaDisponivel() ? new Color(100, 210, 120) : new Color(240, 70, 70));

		status.setBorder(new EmptyBorder(8, 10, 8, 10));
		status.setAlignmentX(Component.CENTER_ALIGNMENT);

		painelAcao.add(status);
		painelAcao.add(Box.createVerticalStrut(8));

		// O BOTAO SO APARECE SE O MATERIAL ESTIVER DISPONIVEL
		if (material.estaDisponivel()) {

			boolean existeSolicitacaoPendente = solicitacaoEmprestimos.verificarSolicitacaoPendenteDuplicada(material.getId());

			if (existeSolicitacaoPendente) {

				JLabel solicitadoFlag = new JLabel("Solicitado");

				solicitadoFlag.setOpaque(true);
				solicitadoFlag.setForeground(Color.BLACK);
				solicitadoFlag.setBackground(material.estaDisponivel() ? new Color(210, 195, 100) : new Color(240, 70, 70));
				solicitadoFlag.setBorder(new EmptyBorder(8, 10, 8, 10));
				solicitadoFlag.setAlignmentX(Component.CENTER_ALIGNMENT);
				painelAcao.add(solicitadoFlag);
			} else {

				JButton botaoEmprestar = this.createBotaoEmprestar(material);
				painelAcao.add(botaoEmprestar);
			}

		}

		card.add(painelAcao, BorderLayout.EAST);

		painelMateriais.add(card);
		painelMateriais.add(Box.createVerticalStrut(10));

		painelMateriais.revalidate();
		painelMateriais.repaint();
	}

	private JButton createBotaoEmprestar(MaterialBibliografico material) {
		JButton botaoEmprestar = new JButton("Pedir emprestado");

		botaoEmprestar.setAlignmentX(Component.CENTER_ALIGNMENT);

		botaoEmprestar.addActionListener(e -> {

			int opcao = JOptionPane.showConfirmDialog(this,
					"Deseja solicitar o material \"" + material.getTitulo() + "\" para pegar emprestado?",
					"Confirmar empréstimo", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (opcao == JOptionPane.YES_OPTION) {
				try {
					this.solicitacaoEmprestimos.solicitarEmprestimo(material.getId());
					this.atualizarMateriais();
				} catch (OperacaoNaoPermitidaException one) {
					JOptionPane.showMessageDialog(this, one.getMessage(), "Erro de permissão",
							JOptionPane.ERROR_MESSAGE);
				} catch (UsuarioNaoAutenticadoException une) {
					JOptionPane.showMessageDialog(this, une.getMessage(), "Erro de autenticação",
							JOptionPane.ERROR_MESSAGE);
					dispose();
					voltarLogin.run();
				} catch (RegistroNaoEncontradoException | ItemDuplicadoException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro ao emprestar",
							JOptionPane.ERROR_MESSAGE);
				}
            }
		});
		return botaoEmprestar;
	}

	private void criarInformacoes(JPanel informacoes, Livro material) {
		informacoes.add(new JLabel("Autor: " + material.getAutor()));
		informacoes.add(Box.createVerticalStrut(5));
		informacoes.add(new JLabel("Ano de publicação: " + material.getAnoPublicacao()));
		informacoes.add(Box.createVerticalStrut(5));
		informacoes.add(new JLabel("ISBN: " + material.getIsbn()));
	}

	private void criarInformacoes(JPanel informacoes, Periodico material) {
		informacoes.add(new JLabel("Ano de publicação: " + material.getAnoPublicacao()));
		informacoes.add(Box.createVerticalStrut(5));
		informacoes.add(new JLabel("Número de edição: " + material.getNumeroEdicao()));
		informacoes.add(Box.createVerticalStrut(5));
		informacoes.add(new JLabel("Data de publicação: " + material.getDataPublicacao()));
	}

	private void atualizarMateriais() {
		List<MaterialBibliografico> materiais;
		try {
			materiais = this.catalogo.listarMateriais();
			carregarMateriais(materiais);
		} catch (OperacaoNaoPermitidaException one) {
			JOptionPane.showMessageDialog(this, one.getMessage(), "Erro de permissão", JOptionPane.ERROR_MESSAGE);
		} catch (UsuarioNaoAutenticadoException une) {
			JOptionPane.showMessageDialog(this, une.getMessage(), "Erro de autenticação", JOptionPane.ERROR_MESSAGE);
			dispose();
			voltarLogin.run();
		}
	}
}
