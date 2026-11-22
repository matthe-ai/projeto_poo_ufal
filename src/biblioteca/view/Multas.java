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

import biblioteca.exception.MultaJaProcessadaException;
import biblioteca.exception.OperacaoNaoPermitidaException;
import biblioteca.exception.RegistroNaoEncontradoException;
import biblioteca.exception.UsuarioNaoAutenticadoException;
import biblioteca.model.Multa;
import biblioteca.model.StatusEnums.StatusMulta;
import biblioteca.service.CatalogoService;
import biblioteca.service.MultaService;

public class Multas extends JPanel {

	// atributos
	
	private final MultaService multaService;
    private final CatalogoService catalogo;
	private Runnable voltarLogin;
	
	private JPanel painelLista;
	
	// construtor
	
    public Multas(MultaService mus,  CatalogoService catalogo, Runnable voltarLogin) {
        this.multaService = mus;
        this.catalogo = catalogo;
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

        JLabel titulo = new JLabel("Multas");
        titulo.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton btnAtualizar = new JButton("Atualizar");

        cabecalho.add(titulo);
        cabecalho.add(Box.createHorizontalGlue());
        cabecalho.add(btnAtualizar);

        btnAtualizar.addActionListener(e -> {
            painelLista.removeAll();

            this.criarMultas();

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
        
        this.criarMultas();

        add(scroll, BorderLayout.CENTER);
    }

    // =================================
    // CRIAR COMPONENTE DA MULTA
    // =================================

    private JPanel criarMulta(Multa multa) throws OperacaoNaoPermitidaException, RegistroNaoEncontradoException, UsuarioNaoAutenticadoException {

        JPanel item = new JPanel(new BorderLayout(10, 0));

        item.setBackground(Color.WHITE);
        item.setBorder(new EmptyBorder(10, 20, 10, 20));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        item.setPreferredSize(new Dimension(500, 60));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Informações da multa
        JPanel informacoes = new JPanel();
        informacoes.setLayout(new BoxLayout(informacoes, BoxLayout.Y_AXIS));
        informacoes.setOpaque(false);

        String materalTitulo = catalogo.buscarMaterial(multa.getEmprestimo().getMaterialId()).getTitulo();
        JLabel labelTitulo = new JLabel("Obra: " + materalTitulo);
        JLabel labelSolicitante = new JLabel("Valor: " + multa.getValor());

        informacoes.add(labelTitulo);
        informacoes.add(Box.createVerticalStrut(3));
        informacoes.add(labelSolicitante);

        // Painel dos botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        painelBotoes.setOpaque(false);

        JButton botaoAprovar = new JButton("Marcar como paga");
        botaoAprovar.setBackground(new Color(80, 240, 30));
        botaoAprovar.setForeground(Color.BLACK);
        botaoAprovar.setFocusPainted(false);
        botaoAprovar.setBorderPainted(false);

        JButton botaoRejeitar = new JButton("Cancelar multa");
        botaoRejeitar.setBackground(new Color(230, 55, 55));
        botaoRejeitar.setForeground(Color.BLACK);
        botaoRejeitar.setFocusPainted(false);
        botaoRejeitar.setBorderPainted(false);

        botaoAprovar.addActionListener(e -> {
            int opcao = JOptionPane.showConfirmDialog(this, "Deseja marcar como paga?", "Registrar pagamento", JOptionPane.YES_NO_OPTION);
            if (opcao == JOptionPane.YES_OPTION) {
            	try {
					this.multaService.pagarMulta(multa.getId());
					this.limparSolicitacoes();
					this.criarMultas();
				} catch (MultaJaProcessadaException e1) {
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

        botaoRejeitar.addActionListener(e -> {
            int opcao = JOptionPane.showConfirmDialog(this, "Deseja cancelar essa multa?", "Cancelar Multa", JOptionPane.YES_NO_OPTION);
            if (opcao == JOptionPane.YES_OPTION) {
            	try {
					this.multaService.cancelarMulta(multa.getId());
					this.limparSolicitacoes();
					this.criarMultas();
				} catch (MultaJaProcessadaException e1) {
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
    
    private void criarMultas() {
    	List<Multa> multas;
		try {
			multas = this.multaService.listarMultasPorStatus(StatusMulta.PENDENTE);
			for (Multa multa : multas) {
				this.adicionarMulta(multa);
			}
		} catch (UsuarioNaoAutenticadoException une) {
			JOptionPane.showMessageDialog(this, une.getMessage(), "Erro de autenticação", JOptionPane.ERROR_MESSAGE);
			voltarLogin.run();
		} catch (OperacaoNaoPermitidaException one) {
			JOptionPane.showMessageDialog(this, one.getMessage(), "Erro de permissão", JOptionPane.ERROR_MESSAGE);
		} catch (RegistroNaoEncontradoException e) {
            throw new RuntimeException(e);
        }
    }

    // =================================
    // ADICIONAR SOLICITAÇÃO À LISTA
    // =================================

    private void adicionarMulta(Multa multa) throws OperacaoNaoPermitidaException, RegistroNaoEncontradoException, UsuarioNaoAutenticadoException {

        JPanel item = criarMulta(multa);

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
