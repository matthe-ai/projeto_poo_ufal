package biblioteca.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import biblioteca.auth.Sessao;
import biblioteca.exception.CredenciaisInvalidasException;
import biblioteca.exception.RegistroNaoEncontradoException;
import biblioteca.service.CatalogoService;
import biblioteca.service.EmprestimoService;
import biblioteca.service.MultaService;
import biblioteca.service.SolicitacaoEmprestimoService;
import biblioteca.service.UsuarioService;

public class TelaLogin extends JFrame{

	// atributos
	
	private JTextField login;
	private JPasswordField senha;

    final private UsuarioService usuarios;
	final private CatalogoService catalogo;
	final private Sessao sessao;
	final private EmprestimoService emprestimoService;
	final private SolicitacaoEmprestimoService solicitacaoEmprestimos;
	final private MultaService multaService;
	
	// construtor
	
	public TelaLogin(UsuarioService usuarios, CatalogoService catalogo, EmprestimoService emp, Sessao sessao, SolicitacaoEmprestimoService sol, MultaService mus) {
		configurarJanela();
		inicializarComponentes();
		this.usuarios = usuarios;
		this.catalogo = catalogo;
		this.emprestimoService = emp;
		this.sessao = sessao;
		this.solicitacaoEmprestimos = sol;
		this.multaService = mus;
	}
	
	private void configurarJanela() {
        setTitle("Biblioteca"); // titulo
        setSize(500, 350); // tamanho
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }
	
	private void inicializarComponentes() {
        JPanel painelPrincipal = new JPanel(new GridBagLayout());
        painelPrincipal.setBackground(Color.WHITE);
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campo de login
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;

        JLabel labelLogin = new JLabel("Login");
        labelLogin.setFont(new Font("Arial", Font.PLAIN, 22));
        painelPrincipal.add(labelLogin, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;

        login = new JTextField(15);
        login.setFont(new Font("Arial", Font.PLAIN, 16));
        login.setPreferredSize(new Dimension(200, 40));
        painelPrincipal.add(login, gbc);

        // Campo de senha
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;

        JLabel labelSenha = new JLabel("Senha");
        labelSenha.setFont(new Font("Arial", Font.PLAIN, 22));
        painelPrincipal.add(labelSenha, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;

        senha = new JPasswordField(15);
        senha.setFont(new Font("Arial", Font.PLAIN, 16));
        senha.setPreferredSize(new Dimension(200, 40));
        painelPrincipal.add(senha, gbc);

        // Espaçamento entre campos e botões
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 0.5;

        painelPrincipal.add(Box.createVerticalStrut(20), gbc);

        // Botão sair
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weighty = 0;

        JButton bsair = new JButton("Sair");
        estilizarBotao(bsair);

        bsair.addActionListener(e -> {
            System.exit(0);
        });

        painelPrincipal.add(bsair, gbc);

        // Botão fazer login
        gbc.gridx = 1;

        JButton blogin = new JButton("Fazer login");
        estilizarBotao(blogin);

        blogin.addActionListener(e -> {
        	String loginDigitado = this.login.getText();
        	String senhaDigitada = new String(this.senha.getPassword());
        	try {
				this.usuarios.logar(loginDigitado, senhaDigitada);
				MenuPrincipal tela = new MenuPrincipal(
						this.sessao,
						this.usuarios,
						this.catalogo,
						this.emprestimoService,
						() -> this.setVisible(true),
						this.solicitacaoEmprestimos,
						this.multaService
						);

                this.setVisible(false);
                tela.setVisible(true);
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
				JOptionPane.showMessageDialog(this, "Algum erro aconteceu", "ERRO", JOptionPane.ERROR_MESSAGE);
			} catch (CredenciaisInvalidasException e1) {
				JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro de credenciais", JOptionPane.ERROR_MESSAGE);
			} catch (RegistroNaoEncontradoException e1) {
				JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro ao buscar registro", JOptionPane.ERROR_MESSAGE);
			}
        });

        painelPrincipal.add(blogin, gbc);

        add(painelPrincipal);
    }
	
	private void estilizarBotao(JButton botao) {
        botao.setFont(new Font("Arial", Font.PLAIN, 18));
        botao.setPreferredSize(new Dimension(150, 50));
        botao.setFocusPainted(false);
        botao.setBackground(new Color(220, 220, 220));
        botao.setBorderPainted(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
