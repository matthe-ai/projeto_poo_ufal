package biblioteca.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import biblioteca.exception.MaterialDevolvidoException;
import biblioteca.exception.OperacaoNaoPermitidaException;
import biblioteca.exception.RegistroNaoEncontradoException;
import biblioteca.exception.UsuarioNaoAutenticadoException;
import biblioteca.model.Emprestimo;
import biblioteca.model.StatusEnums.StatusEmprestimo;
import biblioteca.service.EmprestimoService;

public class Emprestimos extends JPanel {

	// atributos
	
	private final EmprestimoService emprestimoService;
	private Runnable voltarLogin;
	
	private JPanel painelLista;
	
	// construtor
	
    public Emprestimos(EmprestimoService ems, Runnable voltarLogin) {
        this.emprestimoService = ems;
    	
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
    // CRIAR COMPONENTE DO EMPRESTIMO
    // =================================

    private JPanel criarEmprestimo(Emprestimo emprestimo) {

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

        JLabel labelTitulo = new JLabel(emprestimo.getMaterial().getTitulo());
        JLabel labelEmprestadoPara = new JLabel("Emprestado para: " + emprestimo.getUsuario().getNome());

        informacoes.add(labelTitulo);
        informacoes.add(Box.createVerticalStrut(3));
        informacoes.add(labelEmprestadoPara);

        JButton botaoAprovar = this.createBotaoAprovar(emprestimo);

        item.add(informacoes, BorderLayout.CENTER);
        item.add(botaoAprovar, BorderLayout.EAST);

        return item;
    }

    private JButton createBotaoAprovar(Emprestimo emprestimo) {
        JButton botaoAprovar = new JButton("Registrar devolução");
        botaoAprovar.setBackground(new Color(80, 240, 30));
        botaoAprovar.setForeground(Color.BLACK);
        botaoAprovar.setFocusPainted(false);
        botaoAprovar.setBorderPainted(false);

        botaoAprovar.addActionListener(e -> {
            int opcao = JOptionPane.showConfirmDialog(this, "Deseja registrar devolução?", "Registrar devolução", JOptionPane.YES_NO_OPTION);
            if (opcao == JOptionPane.YES_OPTION) {
            	try {
					this.emprestimoService.registrarDevolucao(emprestimo.getId());
					this.limparSolicitacoes();
					this.criarSolicitacoes();
				} catch (MaterialDevolvidoException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro de devolução", JOptionPane.ERROR_MESSAGE);
				} catch (UsuarioNaoAutenticadoException une) {
					JOptionPane.showMessageDialog(this, une.getMessage(), "Erro de autenticação", JOptionPane.ERROR_MESSAGE);
					voltarLogin.run();
				} catch (OperacaoNaoPermitidaException one) {
					JOptionPane.showMessageDialog(this, one.getMessage(), "Erro de permissão", JOptionPane.ERROR_MESSAGE);
				} catch (RegistroNaoEncontradoException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro de registro", JOptionPane.ERROR_MESSAGE);
				} catch (ItemDuplicadoException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro de duplicação", JOptionPane.ERROR_MESSAGE);
				}
            }
        });
        return botaoAprovar;
    }

    private void criarSolicitacoes() {
    	List<Emprestimo> emprestimos;
		try {
			emprestimos = this.emprestimoService.listarEmprestimosPorStatus(StatusEmprestimo.ATIVO);
			for (Emprestimo emprestimo : emprestimos) {
				this.adicionarEmprestimo(emprestimo);
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

    private void adicionarEmprestimo(Emprestimo emprestimo) {

        JPanel item = criarEmprestimo(emprestimo);

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
