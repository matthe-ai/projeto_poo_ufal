package biblioteca.view;

import biblioteca.auth.Sessao;
import biblioteca.exception.CredenciaisInvalidasException;
import biblioteca.exception.ItemDuplicadoException;
import biblioteca.exception.OperacaoNaoPermitidaException;
import biblioteca.exception.RegistroNaoEncontradoException;
import biblioteca.exception.UsuarioNaoAutenticadoException;
import biblioteca.model.Emprestimo;
import biblioteca.model.SolicitacaoEmprestimo;
import biblioteca.model.StatusEnums.StatusEmprestimo;
import biblioteca.model.StatusEnums.StatusSolicitacao;
import biblioteca.service.CatalogoService;
import biblioteca.service.EmprestimoService;
import biblioteca.service.SolicitacaoEmprestimoService;
import biblioteca.service.UsuarioService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MeusDados extends JPanel {
	
	// atributos
	
    final private Sessao sessao;
    final private EmprestimoService emprestimos;
    final private UsuarioService usuarioService;
    final private SolicitacaoEmprestimoService solicitacaoEmprestimos;
	final private CatalogoService catalogo;
    
    final private Runnable voltarLogin;
    
    // construtor
    
    public MeusDados(Sessao sessao, UsuarioService usuarioService, EmprestimoService emprestimos, Runnable voltarLogin, SolicitacaoEmprestimoService sol, CatalogoService catalogo) {
        this.sessao = sessao;
        this.usuarioService = usuarioService;
        this.emprestimos = emprestimos;
        this.voltarLogin = voltarLogin;
        this.solicitacaoEmprestimos = sol;
        this.catalogo = catalogo;
        configurarPainel();
        criarComponentes();
    }
    
    // metodos

    private void configurarPainel() {
        setLayout(new GridBagLayout());
        setBackground(new Color(220, 220, 220));
    }

	private void criarComponentes() {

		setLayout(new BorderLayout(20, 20));
		setBorder(new EmptyBorder(40, 40, 30, 40));
		setBackground(new Color(220, 220, 220));

		// Painel superior: alteração dos dados
		JPanel painelDados = new JPanel(new BorderLayout(0, 15));
		painelDados.setBackground(new Color(220, 220, 220));

		JPanel campos = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
		campos.setBackground(new Color(220, 220, 220));

		Dimension tamanhoCampo = new Dimension(210, 30);

		// Campo de login
		JPanel campoLogin = new JPanel();
		campoLogin.setLayout(new BoxLayout(campoLogin, BoxLayout.Y_AXIS));
		campoLogin.setBackground(new Color(220, 220, 220));

		JLabel labelLogin = new JLabel(
				"Login atual: " + this.sessao.getUsuarioLogado().getLogin());

		JTextField inputLogin = new JTextField();

		inputLogin.setPreferredSize(tamanhoCampo);
		inputLogin.setMaximumSize(tamanhoCampo);
		inputLogin.setMinimumSize(tamanhoCampo);
		inputLogin.setBorder(new EmptyBorder(5, 5, 5, 5));

		labelLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
		inputLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

		campoLogin.add(labelLogin);
		campoLogin.add(Box.createVerticalStrut(5));
		campoLogin.add(inputLogin);
		campoLogin.add(Box.createVerticalGlue());

		// Campo de senha
		JPanel campoSenha = new JPanel();
		campoSenha.setBackground(new Color(220, 220, 220));
		campoSenha.setLayout(new BoxLayout(campoSenha, BoxLayout.Y_AXIS));

		JLabel labelSenha = new JLabel("Senha atual:");
		labelSenha.setAlignmentX(Component.LEFT_ALIGNMENT);

		JTextField inputSenhaAtual = new JTextField();
		inputSenhaAtual.setAlignmentX(Component.LEFT_ALIGNMENT);
		inputSenhaAtual.setPreferredSize(tamanhoCampo);
		inputSenhaAtual.setMaximumSize(tamanhoCampo);
		inputSenhaAtual.setMinimumSize(tamanhoCampo);

		JLabel labelNovaSenha = new JLabel("Nova senha:");
		labelNovaSenha.setAlignmentX(Component.LEFT_ALIGNMENT);

		JTextField inputSenhaNova = new JTextField();
		inputSenhaNova.setAlignmentX(Component.LEFT_ALIGNMENT);
		inputSenhaNova.setPreferredSize(tamanhoCampo);
		inputSenhaNova.setMaximumSize(tamanhoCampo);
		inputSenhaNova.setMinimumSize(tamanhoCampo);

		inputSenhaAtual.setBorder(new EmptyBorder(5, 5, 5, 5));
		inputSenhaNova.setBorder(new EmptyBorder(5, 5, 5, 5));

		campoSenha.add(labelSenha);
		campoSenha.add(Box.createVerticalStrut(5));
		campoSenha.add(inputSenhaAtual);
		campoSenha.add(Box.createVerticalStrut(10));
		campoSenha.add(labelNovaSenha);
		campoSenha.add(Box.createVerticalStrut(5));
		campoSenha.add(inputSenhaNova);

		// Campo de nome
		JPanel campoNome = new JPanel();
		campoNome.setLayout(new BoxLayout(campoNome, BoxLayout.Y_AXIS));
		campoNome.setBackground(new Color(220, 220, 220));

		JLabel labelNome = new JLabel(
				"Nome atual: " + this.sessao.getUsuarioLogado().getNome());

		JTextField inputNome = new JTextField();

		inputNome.setPreferredSize(tamanhoCampo);
		inputNome.setMaximumSize(tamanhoCampo);
		inputNome.setMinimumSize(tamanhoCampo);
		inputNome.setBorder(new EmptyBorder(5, 5, 5, 5));

		labelNome.setAlignmentX(Component.LEFT_ALIGNMENT);
		inputNome.setAlignmentX(Component.LEFT_ALIGNMENT);

		campoNome.add(labelNome);
		campoNome.add(Box.createVerticalStrut(5));
		campoNome.add(inputNome);
		campoNome.add(Box.createVerticalGlue());

		// Padronização da altura dos três grupos
		Dimension tamanhoGrupo = new Dimension(
				tamanhoCampo.width,
				campoSenha.getPreferredSize().height
		);

		campoLogin.setPreferredSize(tamanhoGrupo);
		campoLogin.setMinimumSize(tamanhoGrupo);
		campoLogin.setMaximumSize(tamanhoGrupo);

		campoSenha.setPreferredSize(tamanhoGrupo);
		campoSenha.setMinimumSize(tamanhoGrupo);
		campoSenha.setMaximumSize(tamanhoGrupo);

		campoNome.setPreferredSize(tamanhoGrupo);
		campoNome.setMinimumSize(tamanhoGrupo);
		campoNome.setMaximumSize(tamanhoGrupo);

		// Adicionando os grupos ao painel
		campos.add(campoLogin);
		campos.add(campoSenha);
		campos.add(campoNome);

		JPanel painelBotao = new JPanel(new FlowLayout(FlowLayout.CENTER));
		painelBotao.setBackground(new Color(220, 220, 220));

		// Botão para atualizar os dados
		JButton botaoAtualizar = new JButton("Atualizar dados");

		painelBotao.add(botaoAtualizar);

		botaoAtualizar.addActionListener(e -> {

			String novoLogin = inputLogin.getText();
			String antigaSenha = inputSenhaAtual.getText();
			String novaSenha = inputSenhaNova.getText();
			String novoNome = inputNome.getText();

			try {

				if (novoLogin.isBlank()) {
					novoLogin = this.sessao.getUsuarioLogado().getLogin();
				}

				if (novoNome.isBlank()) {
					novoNome = this.sessao.getUsuarioLogado().getNome();
				}

				if (!novaSenha.isBlank()) {
					try {
						this.usuarioService.atualizarMinhaSenha(antigaSenha, novaSenha);

					} catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
						JOptionPane.showMessageDialog(
								this,
								"Ocorreu um erro ao atualizar a senha",
								"Erro ao atualizar dados",
								JOptionPane.ERROR_MESSAGE
						);
					}
				}

				JOptionPane.showMessageDialog(
						this,
						"Seus dados serão atualizados no próximo login",
						"Dados atualizados com sucesso",
						JOptionPane.INFORMATION_MESSAGE
				);

				this.usuarioService.atualizarMeusDados(novoNome, novoLogin);
				this.criarComponentes();

			} catch (OperacaoNaoPermitidaException one) {

				JOptionPane.showMessageDialog(
						this,
						one.getMessage(),
						"Erro de permissão",
						JOptionPane.ERROR_MESSAGE
				);

			} catch (RegistroNaoEncontradoException | CredenciaisInvalidasException
			         | ItemDuplicadoException e1) {

				JOptionPane.showMessageDialog(
						this,
						e1.getMessage(),
						"Erro ao atualizar dados",
						JOptionPane.ERROR_MESSAGE
				);

			} catch (UsuarioNaoAutenticadoException une) {

				JOptionPane.showMessageDialog(
						this,
						une.getMessage(),
						"Erro de autenticação",
						JOptionPane.ERROR_MESSAGE
				);

				voltarLogin.run();
			}
		});

		painelDados.add(campos, BorderLayout.CENTER);
		painelDados.add(painelBotao, BorderLayout.SOUTH);

		add(painelDados, BorderLayout.NORTH);

		criarPainelEmprestimos();
	}


	private void criarPainelEmprestimos() {
        JPanel painelEmprestimos = new JPanel(new GridLayout(1, 2, 20, 20));
	    painelEmprestimos.setBackground(new Color(220, 220, 220));
	
	    // =================================
	    // EMPRÉSTIMOS ATIVOS
	    // =================================
	
	    JPanel areaAtivos = new JPanel(new BorderLayout(0, 10));
	    areaAtivos.setBackground(new Color(220, 220, 220));
	
	    JPanel cabecalhoEmprestimos = new JPanel();
	    cabecalhoEmprestimos.setLayout(new BoxLayout(cabecalhoEmprestimos, BoxLayout.X_AXIS));
	    cabecalhoEmprestimos.setOpaque(false);
	    cabecalhoEmprestimos.setBorder(new EmptyBorder(5, 0, 10, 0));
	
	    JLabel tituloEmprestimos = new JLabel("Empréstimos ativos");
	
	    JButton btnAtualizar = new JButton("Atualizar");
	
	    cabecalhoEmprestimos.add(tituloEmprestimos);
	    cabecalhoEmprestimos.add(Box.createHorizontalGlue());
	    cabecalhoEmprestimos.add(btnAtualizar);
	
	    JPanel painelListaEmprestimos = new JPanel();
	    painelListaEmprestimos.setLayout(new BoxLayout(painelListaEmprestimos, BoxLayout.Y_AXIS));
	    painelListaEmprestimos.setOpaque(false);
	
	    areaAtivos.add(cabecalhoEmprestimos, BorderLayout.NORTH);
	    areaAtivos.add(painelListaEmprestimos, BorderLayout.CENTER);
	
	    // =================================
	    // HISTÓRICO DE EMPRÉSTIMOS
	    // =================================
	
	    JPanel areaHistorico = new JPanel(new BorderLayout(0, 10));
	    areaHistorico.setBackground(new Color(220, 220, 220));
	
	    JLabel tituloHistorico = new JLabel("Histórico de empréstimos");
	
	    JPanel painelListaHistorico = new JPanel();
	    painelListaHistorico.setLayout(new BoxLayout(painelListaHistorico, BoxLayout.Y_AXIS));
	    painelListaHistorico.setBackground(new Color(220, 220, 220));
	
	    JScrollPane scrollHistorico = new JScrollPane(painelListaHistorico);
	    scrollHistorico.setBorder(null);
	    scrollHistorico.getVerticalScrollBar().setUnitIncrement(15);
	
	    areaHistorico.add(tituloHistorico, BorderLayout.NORTH);
	    areaHistorico.add(scrollHistorico, BorderLayout.CENTER);
	    
	    btnAtualizar.addActionListener(e -> {
	        painelListaEmprestimos.removeAll();
	        painelListaHistorico.removeAll();
	
	        this.criarMateriais(painelListaEmprestimos, painelListaHistorico);
	
	        painelListaEmprestimos.revalidate();
	        painelListaEmprestimos.repaint();
	    });
	
	    // =================================
	    // ORGANIZAÇÃO FINAL
	    // =================================
	
	    painelEmprestimos.add(areaAtivos);
	    painelEmprestimos.add(areaHistorico);
	
	    add(painelEmprestimos, BorderLayout.CENTER);
	}


	private void criarMateriais(JPanel painelEmprestimos, JPanel painelHistorico) {
		try {
			List<Emprestimo> emprestimosUsuario = this.emprestimos.listarMeusEmprestimos();
			List<SolicitacaoEmprestimo> solicitacoesUsuario = this.solicitacaoEmprestimos.listarMinhasSolicitacoes();
			for (Emprestimo emprestimo : emprestimosUsuario) {
				if (emprestimo.getStatus() == StatusEmprestimo.DEVOLVIDO) {
					painelHistorico.add(this.criarMaterial(emprestimo));
					painelHistorico.add(Box.createVerticalStrut(15));
				} else {
		            painelEmprestimos.add(this.criarMaterial(emprestimo));
		            painelEmprestimos.add(Box.createVerticalStrut(15));
				}
	        }
			for (SolicitacaoEmprestimo solicitacaoEmprestimo : solicitacoesUsuario) {
				if (solicitacaoEmprestimo.getStatus() != StatusSolicitacao.APROVADA) {
					painelHistorico.add(this.criarMaterial(solicitacaoEmprestimo));
				}
			}
		} catch (OperacaoNaoPermitidaException one) {
			JOptionPane.showMessageDialog(this, one.getMessage(), "Erro de permissão", JOptionPane.ERROR_MESSAGE);
		} catch (UsuarioNaoAutenticadoException une) {
			JOptionPane.showMessageDialog(this, une.getMessage(), "Erro de autenticação", JOptionPane.ERROR_MESSAGE);
			voltarLogin.run();
		} catch (RegistroNaoEncontradoException e) {
            throw new RuntimeException(e);
        }
    }
	
	private JPanel criarMaterial(SolicitacaoEmprestimo solicitacao) throws OperacaoNaoPermitidaException, RegistroNaoEncontradoException, UsuarioNaoAutenticadoException {
		JPanel itemEmprestimo = new JPanel(new BorderLayout());
		
	    itemEmprestimo.setBackground(Color.WHITE);
	    itemEmprestimo.setBorder(new EmptyBorder(20, 30, 20, 30));
	    itemEmprestimo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
	    itemEmprestimo.setAlignmentX(Component.LEFT_ALIGNMENT);
	    
	    JLabel informacao;

		String materialTitulo = catalogo.buscarMaterial(solicitacao.getMaterialSolicitadoId()).getTitulo();
    	informacao = new JLabel(materialTitulo + " - Status: solicitação " + solicitacao.getStatus());
    	itemEmprestimo.add(informacao, BorderLayout.CENTER);
    	return itemEmprestimo;
	    
	}
    
	private JPanel criarMaterial(Emprestimo emprestimo) throws OperacaoNaoPermitidaException, RegistroNaoEncontradoException, UsuarioNaoAutenticadoException {
	    JPanel itemEmprestimo = new JPanel(new BorderLayout());
	
	    itemEmprestimo.setBackground(Color.WHITE);
	    itemEmprestimo.setBorder(new EmptyBorder(15, 20, 15, 20));
	    itemEmprestimo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
	    itemEmprestimo.setAlignmentX(Component.LEFT_ALIGNMENT);
	    
	    JLabel informacao;
		String materialTitulo = catalogo.buscarMaterial(emprestimo.getMaterialId()).getTitulo();
	    if (emprestimo.getStatus() == StatusEmprestimo.DEVOLVIDO) {
	    	informacao = new JLabel(
	    			"<HTML>" + materialTitulo +
	    			"<br>Devolvido em: " +
	    			emprestimo.getDataDevolucao() +
	    			"</HTML>"
	    			);
	    	itemEmprestimo.add(informacao, BorderLayout.CENTER);
	    	return itemEmprestimo;
	    }
	
	    // Informações do material
	    long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), emprestimo.getDataPrevistaDevolucao());
	    
	    informacao = new JLabel(
	    		"<html>" + materialTitulo +
	    	    "<br>Tempo restante: " + diasRestantes + " dias" +
	    	    "</html>"
	    		);
	
	    itemEmprestimo.add(informacao, BorderLayout.CENTER);
	
	    return itemEmprestimo;
	}

}