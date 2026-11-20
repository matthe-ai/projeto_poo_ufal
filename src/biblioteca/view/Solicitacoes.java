package biblioteca.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import biblioteca.exception.ItemDuplicadoException;
import biblioteca.exception.LimiteEmprestimosException;
import biblioteca.exception.MaterialIndisponivelException;
import biblioteca.exception.OperacaoNaoPermitidaException;
import biblioteca.exception.RegistroNaoEncontradoException;
import biblioteca.exception.SolicitacaoJaProcessadaException;
import biblioteca.exception.UsuarioNaoAutenticadoException;
import biblioteca.model.SolicitacaoEmprestimo;
import biblioteca.model.StatusSolicitacao;
import biblioteca.service.SolicitacaoEmprestimoService;

public class Solicitacoes extends JPanel {

	// atributos
	
	private SolicitacaoEmprestimoService solicitacoesService;
	private Runnable voltarLogin;
	
	private JPanel painelLista;
	
	// construtor
	
    public Solicitacoes(SolicitacaoEmprestimoService sol, Runnable voltarLogin) {
        this.solicitacoesService = sol;
    	
    	configurarPainel();
        criarComponentes();
    }
    
    // metodos

    private void configurarPainel() {
        setLayout(new BorderLayout());
        setBackground(new Color(220, 220, 220));
        setBorder(new EmptyBorder(25, 22, 20, 22));
    }

    private void criarComponentes() {

    	// Cabeçalho
        JPanel cabecalho = new JPanel();
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.X_AXIS));
        cabecalho.setOpaque(false);
        cabecalho.setBorder(new EmptyBorder(5, 0, 20, 0));

        JLabel titulo = new JLabel("Solicitações");
        titulo.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton btnAtualizar = new JButton("Atualizar");

        cabecalho.add(titulo);
        cabecalho.add(Box.createHorizontalGlue());
        cabecalho.add(btnAtualizar);

        btnAtualizar.addActionListener(e -> {
            painelLista.removeAll();

            this.criarSolicitacoes();

            painelLista.revalidate();
            painelLista.repaint();
        });

        add(cabecalho, BorderLayout.NORTH);

        // Lista de solicitações
        painelLista = new JPanel();
        painelLista.setLayout(new BoxLayout(painelLista, BoxLayout.Y_AXIS));
        painelLista.setBackground(new Color(220, 220, 220));

        // Área de rolagem
        JScrollPane scroll = new JScrollPane(painelLista);
        scroll.setBorder(null);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(15);
        
        this.criarSolicitacoes();

        add(scroll, BorderLayout.CENTER);
    }

    // =================================
    // CRIAR COMPONENTE DA SOLICITAÇÃO
    // =================================

    private JPanel criarSolicitacao(SolicitacaoEmprestimo solicitacao) {

        JPanel item = new JPanel(new BorderLayout(10, 0));

        item.setBackground(Color.WHITE);
        item.setBorder(new EmptyBorder(10, 20, 10, 20));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        item.setPreferredSize(new Dimension(500, 60));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Informações da solicitação
        JPanel informacoes = new JPanel();
        informacoes.setLayout(new BoxLayout(informacoes, BoxLayout.Y_AXIS));
        informacoes.setOpaque(false);

        JLabel labelTitulo = new JLabel(solicitacao.getMaterialSolicitado().getTitulo());
        JLabel labelSolicitante = new JLabel("Solicitante: " + solicitacao.getSolicitante().getNome());

        informacoes.add(labelTitulo);
        informacoes.add(Box.createVerticalStrut(3));
        informacoes.add(labelSolicitante);

        // Painel dos botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        painelBotoes.setOpaque(false);

        JButton botaoAprovar = new JButton("Aprovar");
        botaoAprovar.setBackground(new Color(80, 240, 30));
        botaoAprovar.setForeground(Color.BLACK);
        botaoAprovar.setFocusPainted(false);
        botaoAprovar.setBorderPainted(false);

        JButton botaoRejeitar = new JButton("Rejeitar");
        botaoRejeitar.setBackground(new Color(230, 55, 55));
        botaoRejeitar.setForeground(Color.BLACK);
        botaoRejeitar.setFocusPainted(false);
        botaoRejeitar.setBorderPainted(false);

        botaoAprovar.addActionListener(e -> {
            int opcao = JOptionPane.showConfirmDialog(this, "Deseja aprovar essa solicitação?", "Aprovar solicitação", JOptionPane.YES_NO_OPTION);
            if (opcao == JOptionPane.YES_OPTION) {
            	try {
					this.solicitacoesService.aprovarSolicitacao(solicitacao.getId());
					this.limparSolicitacoes();
					this.criarSolicitacoes();
				} catch (SolicitacaoJaProcessadaException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro de processamento", JOptionPane.ERROR_MESSAGE);
				} catch (UsuarioNaoAutenticadoException une) {
					JOptionPane.showMessageDialog(this, une.getMessage(), "Erro de autenticação", JOptionPane.ERROR_MESSAGE);
					voltarLogin.run();
				} catch (OperacaoNaoPermitidaException one) {
					JOptionPane.showMessageDialog(this, one.getMessage(), "Erro de permissão", JOptionPane.ERROR_MESSAGE);
				} catch (RegistroNaoEncontradoException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro de registro", JOptionPane.ERROR_MESSAGE);
				} catch (ItemDuplicadoException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro de duplicação", JOptionPane.ERROR_MESSAGE);
				} catch (LimiteEmprestimosException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro de emprestimo", JOptionPane.ERROR_MESSAGE);
				} catch (MaterialIndisponivelException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro de disponibilidade", JOptionPane.ERROR_MESSAGE);
				}
            }
        });

        botaoRejeitar.addActionListener(e -> {
            int opcao = JOptionPane.showConfirmDialog(this, "Deseja rejeitar essa solicitação?", "Rejeitar solicitação", JOptionPane.YES_NO_OPTION);
            if (opcao == JOptionPane.YES_OPTION) {
            	try {
					this.solicitacoesService.rejeitarSolicitacao(solicitacao.getId());
					this.limparSolicitacoes();
					this.criarSolicitacoes();
				} catch (SolicitacaoJaProcessadaException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro de processamento", JOptionPane.ERROR_MESSAGE);
				} catch (UsuarioNaoAutenticadoException une) {
					JOptionPane.showMessageDialog(this, une.getMessage(), "Erro de autenticação", JOptionPane.ERROR_MESSAGE);
					voltarLogin.run();
				} catch (OperacaoNaoPermitidaException one) {
					JOptionPane.showMessageDialog(this, one.getMessage(), "Erro de permissão", JOptionPane.ERROR_MESSAGE);
				} catch (RegistroNaoEncontradoException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro de registro", JOptionPane.ERROR_MESSAGE);
				}
            }
        });

        painelBotoes.add(botaoAprovar);
        painelBotoes.add(botaoRejeitar);

        item.add(informacoes, BorderLayout.CENTER);
        item.add(painelBotoes, BorderLayout.EAST);

        return item;
    }
    
    private void criarSolicitacoes() {
    	List<SolicitacaoEmprestimo> solicitacoes;
		try {
			solicitacoes = this.solicitacoesService.listarSolicitacoesPorStatus(StatusSolicitacao.PENDENTE);
			for (SolicitacaoEmprestimo solicitacao : solicitacoes) {
				this.adicionarSolicitacao(solicitacao);
			}
		} catch (UsuarioNaoAutenticadoException une) {
			JOptionPane.showMessageDialog(this, une.getMessage(), "Erro de autenticação", JOptionPane.ERROR_MESSAGE);
			voltarLogin.run();
		} catch (OperacaoNaoPermitidaException one) {
			JOptionPane.showMessageDialog(this, one.getMessage(), "Erro de permissão", JOptionPane.ERROR_MESSAGE);
		}
    }

    // =================================
    // ADICIONAR SOLICITAÇÃO À LISTA
    // =================================

    private void adicionarSolicitacao(SolicitacaoEmprestimo solicitacao) {

        JPanel item = criarSolicitacao(solicitacao);

        this.painelLista.add(item);
        this.painelLista.add(Box.createVerticalStrut(15));

        this.painelLista.revalidate();
        this.painelLista.repaint();
    }

    // =================================
    // LIMPAR LISTA
    // =================================

    private void limparSolicitacoes() {
    	this.painelLista.removeAll();
    	this.painelLista.revalidate();
    	this.painelLista.repaint();
    }
}
