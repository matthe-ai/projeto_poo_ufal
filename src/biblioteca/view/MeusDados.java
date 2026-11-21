package biblioteca.view;

import biblioteca.auth.Sessao;
import biblioteca.exception.CredenciaisInvalidasException;
import biblioteca.exception.ItemDuplicadoException;
import biblioteca.exception.MaterialDevolvidoException;
import biblioteca.exception.OperacaoNaoPermitidaException;
import biblioteca.exception.RegistroNaoEncontradoException;
import biblioteca.exception.UsuarioNaoAutenticadoException;
import biblioteca.model.Emprestimo;
import biblioteca.model.SolicitacaoEmprestimo;
import biblioteca.model.StatusEmprestimo;
import biblioteca.model.StatusSolicitacao;
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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class MeusDados extends JPanel {
	
	// atributos
	
    final private Sessao sessao;
    final private EmprestimoService emprestimos;
    final private UsuarioService usuarioService;
    final private SolicitacaoEmprestimoService solicitacaoEmprestimos;
    
    private JPanel painelEmprestimos;
    
    final private Runnable voltarLogin;
    
    // construtor
    
    public MeusDados(Sessao sessao, UsuarioService usuarioService, EmprestimoService emprestimos, Runnable voltarLogin, SolicitacaoEmprestimoService sol) {
        this.sessao = sessao;
        this.usuarioService = usuarioService;
        this.emprestimos = emprestimos;
        this.voltarLogin = voltarLogin;
        this.solicitacaoEmprestimos = sol;
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
        
        JPanel campos = new JPanel(new GridLayout(1, 3, 30, 0));
        campos.setBackground(new Color(220, 220, 220));

        // Campo de login
        JPanel campoLogin = new JPanel(new BorderLayout(0, 5));
        campoLogin.setBackground(new Color(220, 220, 220));

        JLabel labelLogin = new JLabel("Login atual: "+this.sessao.getUsuarioLogado().getLogin());
        JTextField inputLogin = new JTextField();
        inputLogin.setBorder(new EmptyBorder(5, 5, 5, 5));

        campoLogin.add(labelLogin, BorderLayout.NORTH);
        campoLogin.add(inputLogin, BorderLayout.CENTER);

        // Campo de senha
        JPanel campoSenha = new JPanel();
        campoSenha.setBackground(new Color(220, 220, 220));
        campoSenha.setLayout(new BoxLayout(campoSenha, BoxLayout.Y_AXIS));

        JLabel labelSenha = new JLabel("Senha atual:");
        JTextField inputSenhaAtual = new JTextField();
        JLabel labelNovaSenha = new JLabel("Nova senha: ");
        JTextField inputSenhaNova = new JTextField();
        inputSenhaAtual.setBorder(new EmptyBorder(5, 5, 5, 5));
        inputSenhaNova.setBorder(new EmptyBorder(5, 5, 5, 5));

        campoSenha.add(labelSenha);
        campoSenha.add(inputSenhaAtual);
        campoSenha.add(Box.createVerticalStrut(10));
        campoSenha.add(labelNovaSenha);
        campoSenha.add(inputSenhaNova);

        // Campo de nome
        JPanel campoNome = new JPanel(new BorderLayout(0, 5));
        campoNome.setBackground(new Color(220, 220, 220));

        JLabel labelNome = new JLabel("Nome atual: "+this.sessao.getUsuarioLogado().getNome());
        JTextField inputNome = new JTextField();
        inputNome.setBorder(new EmptyBorder(5, 5, 5, 5));

        campoNome.add(labelNome, BorderLayout.NORTH);
        campoNome.add(inputNome, BorderLayout.CENTER);

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
            String novaSenha = new String(inputSenhaNova.getText());
            String novoNome = inputNome.getText();
            try {
            	if (novoLogin.isBlank() || novoLogin.isEmpty()) {
            		novoLogin = this.sessao.getUsuarioLogado().getLogin();
            	} if (novoNome.isBlank() || novoNome.isEmpty()) {
            		novoNome = this.sessao.getUsuarioLogado().getNome();
            	} if (!(novaSenha.isBlank() || novaSenha.isEmpty())) {
            		try {
						this.usuarioService.atualizarMinhaSenha(antigaSenha, novaSenha);
					} catch (NoSuchAlgorithmException e1) {
						JOptionPane.showMessageDialog(this, "Ocorreu um erro ao atualizar a senha", "Erro ao atualizar dados", JOptionPane.ERROR_MESSAGE);
					} catch (InvalidKeySpecException e1) {
						JOptionPane.showMessageDialog(this, "Ocorreu um erro ao atualizar a senha", "Erro ao atualizar dados", JOptionPane.ERROR_MESSAGE);
					}
            	}
            	JOptionPane.showMessageDialog(this, "Seus dados serão atualizados no próximo login", "Dados atualizados com sucesso", JOptionPane.INFORMATION_MESSAGE);
				this.usuarioService.atualizarMeusDados(novoNome, novoLogin);
				this.criarComponentes();
			} catch (OperacaoNaoPermitidaException one) {
				JOptionPane.showMessageDialog(this, one.getMessage(), "Erro de permissão", JOptionPane.ERROR_MESSAGE);
			} catch (RegistroNaoEncontradoException e1) {
				JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro ao atualizar dados", JOptionPane.ERROR_MESSAGE);
			} catch (UsuarioNaoAutenticadoException une) {
				JOptionPane.showMessageDialog(this, une.getMessage(), "Erro de autenticação", JOptionPane.ERROR_MESSAGE);
				voltarLogin.run();
			} catch (CredenciaisInvalidasException e1) {
				JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro ao atualizar dados", JOptionPane.ERROR_MESSAGE);
			} catch (ItemDuplicadoException e1) {
				JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro ao atualizar dados", JOptionPane.ERROR_MESSAGE);
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
		}
	}
	
	private JPanel criarMaterial(SolicitacaoEmprestimo solicitacao) {
		JPanel itemEmprestimo = new JPanel(new BorderLayout());
		
	    itemEmprestimo.setBackground(Color.WHITE);
	    itemEmprestimo.setBorder(new EmptyBorder(20, 30, 20, 30));
	    itemEmprestimo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
	    itemEmprestimo.setAlignmentX(Component.LEFT_ALIGNMENT);
	    
	    JLabel informacao;
	    
    	informacao = new JLabel(solicitacao.getMaterialSolicitado().getTitulo() + " - Status: solicitação " + solicitacao.getStatus());
    	itemEmprestimo.add(informacao, BorderLayout.CENTER);
    	return itemEmprestimo;
	    
	}
    
	private JPanel criarMaterial(Emprestimo emprestimo) {
	    JPanel itemEmprestimo = new JPanel(new BorderLayout());
	
	    itemEmprestimo.setBackground(Color.WHITE);
	    itemEmprestimo.setBorder(new EmptyBorder(15, 20, 15, 20));
	    itemEmprestimo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
	    itemEmprestimo.setAlignmentX(Component.LEFT_ALIGNMENT);
	    
	    JLabel informacao;
	    
	    if (emprestimo.getStatus() == StatusEmprestimo.DEVOLVIDO) {
	    	informacao = new JLabel(
	    			"<HTML>" +
	    			emprestimo.getMaterial().getTitulo() + 
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
	    		"<html>" +
	    	    emprestimo.getMaterial().getTitulo() +
	    	    "<br>Tempo restante: " + diasRestantes + " dias" +
	    	    "</html>"
	    		);
	
	    // Botão de devolução
	    JButton botaoDevolver = new JButton("Devolver");
	
	    botaoDevolver.setBorderPainted(false);
	    botaoDevolver.setContentAreaFilled(false);
	    botaoDevolver.setFocusPainted(false);
	
	    botaoDevolver.addActionListener(e -> {
	    	int opcao = JOptionPane.showConfirmDialog(this, "Deseja devolver este material?", "Confirmar devolução", JOptionPane.YES_NO_OPTION);
	    	if (opcao == JOptionPane.YES_OPTION) {
	    		try {
					this.emprestimos.registrarDevolucao(emprestimo.getId());
					this.criarComponentes();
				} catch (OperacaoNaoPermitidaException one) {
					JOptionPane.showMessageDialog(this, one.getMessage(), "Erro de permissão", JOptionPane.ERROR_MESSAGE);
				} catch (RegistroNaoEncontradoException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro ao devolver", JOptionPane.ERROR_MESSAGE);
				} catch (MaterialDevolvidoException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro ao devolver", JOptionPane.ERROR_MESSAGE);
				} catch (UsuarioNaoAutenticadoException une) {
					JOptionPane.showMessageDialog(this, une.getMessage(), "Erro de autenticação", JOptionPane.ERROR_MESSAGE);
					voltarLogin.run();
				} catch (ItemDuplicadoException ex) {
                    throw new RuntimeException(ex);
                }
            }
	    });
	
	    itemEmprestimo.add(informacao, BorderLayout.CENTER);
	    itemEmprestimo.add(botaoDevolver, BorderLayout.EAST);
	
	    return itemEmprestimo;
	}

}