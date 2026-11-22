
package biblioteca.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import biblioteca.exception.CredenciaisInvalidasException;
import biblioteca.exception.ItemDuplicadoException;
import biblioteca.exception.OperacaoNaoPermitidaException;
import biblioteca.exception.RegistroNaoEncontradoException;
import biblioteca.exception.UsuarioNaoAutenticadoException;
import biblioteca.exception.UsuarioPossuiEmprestimosAtivosException;
import biblioteca.model.usuario.Cargo;
import biblioteca.model.usuario.Usuario;
import biblioteca.service.UsuarioService;

public class MenuAdministrador extends JPanel {

    // Atributos

    private final UsuarioService usuarios;
    private final Runnable voltarLogin;
    private JPanel painelListaUsuarios;

    private JButton botaoBuscar;

    // Construtor

    public MenuAdministrador(UsuarioService usuarios, Runnable voltarLogin) {
        this.usuarios = usuarios;
        this.voltarLogin = voltarLogin;
        configurarPainel();
        criarComponentes();
    }

    private void configurarPainel() {
        setLayout(new BorderLayout());
        setBackground(new Color(220, 220, 220));
        setBorder(new EmptyBorder(25, 30, 30, 30));
    }

    // Retorna o botão padrão desta tela para o MenuPrincipal.

    public JButton getBotaoPadrao() {
        return botaoBuscar;
    }

    private void criarComponentes() {

        JPanel painelPrincipal = new JPanel(new GridBagLayout());
        painelPrincipal.setBackground(new Color(220, 220, 220));

        GridBagConstraints gbcPrincipal = new GridBagConstraints();
        gbcPrincipal.gridx = 0;
        gbcPrincipal.weightx = 1;
        gbcPrincipal.fill = GridBagConstraints.HORIZONTAL;
        gbcPrincipal.anchor = GridBagConstraints.WEST;
        gbcPrincipal.insets = new Insets(0, 0, 10, 0);

        // =================================
        // PESQUISAR USUÁRIO
        // =================================

        JPanel painelPesquisa = new JPanel();
        painelPesquisa.setLayout(new BoxLayout(painelPesquisa, BoxLayout.Y_AXIS));
        painelPesquisa.setBackground(new Color(220, 220, 220));
        painelPesquisa.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelPesquisa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 77));

        JLabel tituloPesquisa = new JLabel("Pesquisar usuário");
        tituloPesquisa.setFont(new Font("Arial", Font.PLAIN, 16));
        tituloPesquisa.setAlignmentX(Component.LEFT_ALIGNMENT);

        painelPesquisa.add(tituloPesquisa);
        painelPesquisa.add(Box.createVerticalStrut(20));

        // Campos de pesquisa

        JPanel camposPesquisa = new JPanel(new BorderLayout(15, 0));
        camposPesquisa.setBackground(new Color(220, 220, 220));
        camposPesquisa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        camposPesquisa.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> comboPesquisa =
                new JComboBox<>(new String[]{"Nome", "Login"});

        comboPesquisa.setPreferredSize(new Dimension(110, 38));

        JTextField campoPesquisa = new JTextField();
        botaoBuscar = new JButton("Buscar Usuário");

        botaoBuscar.setBackground(Color.WHITE);
        botaoBuscar.setFocusPainted(false);
        botaoBuscar.setBorderPainted(false);

        camposPesquisa.add(comboPesquisa, BorderLayout.WEST);
        camposPesquisa.add(campoPesquisa, BorderLayout.CENTER);
        camposPesquisa.add(botaoBuscar, BorderLayout.EAST);

        painelPesquisa.add(camposPesquisa);

        gbcPrincipal.gridy = 0;
        gbcPrincipal.weighty = 0;
        gbcPrincipal.fill = GridBagConstraints.HORIZONTAL;
        gbcPrincipal.anchor = GridBagConstraints.WEST;

        painelPrincipal.add(painelPesquisa, gbcPrincipal);

        botaoBuscar.addActionListener(e -> {

            String pesquisa = campoPesquisa.getText();
            String criterio = (String) comboPesquisa.getSelectedItem();

            List<Usuario> resultado;

            switch (criterio) {

                case "Nome":
                    try {
                        resultado = this.usuarios.listarUsuariosPorNome(pesquisa);
                        this.criarUsuarios(resultado, pesquisa, "nome");

                    } catch (OperacaoNaoPermitidaException one) {
                        JOptionPane.showMessageDialog(
                                this, one.getMessage(),
                                "Erro de permissão",
                                JOptionPane.ERROR_MESSAGE
                        );

                    } catch (UsuarioNaoAutenticadoException une) {
                        JOptionPane.showMessageDialog(
                                this, une.getMessage(),
                                "Erro de autenticação",
                                JOptionPane.ERROR_MESSAGE
                        );
                        voltarLogin.run();
                    }
                    break;

                case "Login":
                    try {
                        resultado = this.usuarios.listarUsuariosPorLogin(pesquisa);
                        this.criarUsuarios(resultado, pesquisa, "login");

                    } catch (OperacaoNaoPermitidaException one) {
                        JOptionPane.showMessageDialog(
                                this, one.getMessage(),
                                "Erro de permissão",
                                JOptionPane.ERROR_MESSAGE
                        );

                    } catch (UsuarioNaoAutenticadoException une) {
                        JOptionPane.showMessageDialog(
                                this, une.getMessage(),
                                "Erro de autenticação",
                                JOptionPane.ERROR_MESSAGE
                        );
                        voltarLogin.run();
                    }
                    break;
            }
        });

        // =================================
        // LISTA DE USUÁRIOS
        // =================================

        painelListaUsuarios = new JPanel();
        painelListaUsuarios.setLayout(
                new BoxLayout(painelListaUsuarios, BoxLayout.Y_AXIS)
        );
        painelListaUsuarios.setBackground(new Color(220, 220, 220));

        JScrollPane scroll = new JScrollPane(painelListaUsuarios);

        scroll.setBorder(null);
        scroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );
        scroll.setPreferredSize(new Dimension(600, 250));
        scroll.setMinimumSize(new Dimension(0, 150));
        scroll.getVerticalScrollBar().setUnitIncrement(15);

        gbcPrincipal.gridy = 1;
        gbcPrincipal.weighty = 1;
        gbcPrincipal.fill = GridBagConstraints.BOTH;
        gbcPrincipal.anchor = GridBagConstraints.WEST;

        painelPrincipal.add(scroll, gbcPrincipal);

        // =================================
        // CADASTRAR USUÁRIO
        // =================================

        JPanel painelCadastro = new JPanel();
        painelCadastro.setLayout(
                new BoxLayout(painelCadastro, BoxLayout.Y_AXIS)
        );
        painelCadastro.setBackground(new Color(220, 220, 220));
        painelCadastro.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelCadastro.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)
        );

        JLabel tituloCadastro = new JLabel("Cadastrar usuário");
        tituloCadastro.setFont(new Font("Arial", Font.PLAIN, 16));
        tituloCadastro.setAlignmentX(Component.LEFT_ALIGNMENT);

        painelCadastro.add(tituloCadastro);
        painelCadastro.add(Box.createVerticalStrut(5));

        // Formulário

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(new Color(220, 220, 220));
        formulario.setAlignmentX(Component.LEFT_ALIGNMENT);
        formulario.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)
        );

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField campoNome = new JTextField();
        JTextField campoLogin = new JTextField();
        JTextField campoSenha = new JTextField();

        campoNome.setMinimumSize(new Dimension(400, 25));
        campoLogin.setMinimumSize(new Dimension(400, 25));
        campoSenha.setMinimumSize(new Dimension(400, 25));

        JComboBox<String> comboTipo = new JComboBox<>(
                new String[]{
                        "Usuário comum",
                        "Bibliotecário",
                        "Administrador"
                }
        );

        // Nome

        adicionarCampo(formulario, gbc, 0, "Nome", campoNome);

        // Login

        adicionarCampo(formulario, gbc, 1, "Login", campoLogin);

        // Tipo de usuário

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        formulario.add(new JLabel("Tipo de usuário"), gbc);

        comboTipo.setPreferredSize(new Dimension(110, 25));

        gbc.gridx = 1;
        gbc.weightx = 1;

        formulario.add(comboTipo, gbc);

        // Senha

        adicionarCampo(formulario, gbc, 3, "Senha", campoSenha);

        painelCadastro.add(formulario);

        gbcPrincipal.gridy = 2;
        gbcPrincipal.weighty = 0;
        gbcPrincipal.fill = GridBagConstraints.HORIZONTAL;
        gbcPrincipal.anchor = GridBagConstraints.WEST;

        painelPrincipal.add(painelCadastro, gbcPrincipal);

        // =================================
        // BOTÃO ADICIONAR USUÁRIO
        // =================================

        JPanel painelBotao = new JPanel(
                new FlowLayout(FlowLayout.CENTER)
        );

        painelBotao.setBackground(new Color(220, 220, 220));

        JButton botaoAdicionar = new JButton("Adicionar usuário");

        botaoAdicionar.setFocusPainted(false);
        botaoAdicionar.setBackground(Color.WHITE);
        botaoAdicionar.setBorderPainted(false);

        painelBotao.add(botaoAdicionar);

        gbcPrincipal.gridy = 3;
        gbcPrincipal.weighty = 0;
        gbcPrincipal.fill = GridBagConstraints.NONE;
        gbcPrincipal.anchor = GridBagConstraints.CENTER;

        painelPrincipal.add(painelBotao, gbcPrincipal);

        botaoAdicionar.addActionListener(e -> {

            String nome = campoNome.getText();
            String login = campoLogin.getText();
            String tipo = (String) comboTipo.getSelectedItem();
            String senha = campoSenha.getText();

            Cargo cargo;

            switch (tipo) {

                case "Administrador":
                    cargo = Cargo.ADMINISTRADOR;
                    break;

                case "Bibliotecário":
                    cargo = Cargo.BIBLIOTECARIO;
                    break;

                default:
                    cargo = Cargo.LEITOR;
                    break;
            }

            try {
                this.usuarios.cadastrarUsuario(nome, login, senha, cargo);
                this.limparCampos(formulario);

            } catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
                JOptionPane.showMessageDialog(
                        this, "Algum erro aconteceu",
                        "ERRO",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (CredenciaisInvalidasException e1) {
                JOptionPane.showMessageDialog(
                        this, e1.getMessage(),
                        "Erro nas credenciais digitadas",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (ItemDuplicadoException e1) {
                JOptionPane.showMessageDialog(
                        this, e1.getMessage(),
                        "Erro ao cadastrar/atualizar",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (OperacaoNaoPermitidaException one) {
                JOptionPane.showMessageDialog(
                        this, one.getMessage(),
                        "Erro de permissão",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (UsuarioNaoAutenticadoException une) {
                JOptionPane.showMessageDialog(
                        this, une.getMessage(),
                        "Erro de autenticação",
                        JOptionPane.ERROR_MESSAGE
                );
                voltarLogin.run();
            }
        });

        // =================================
        // ADICIONAR PAINEL PRINCIPAL
        // =================================

        add(painelPrincipal, BorderLayout.CENTER);
    }

    private void adicionarCampo(
            JPanel painel,
            GridBagConstraints gbc,
            int linha,
            String texto,
            JTextField campo
    ) {
        gbc.gridy = linha;

        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        painel.add(new JLabel(texto), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        campo.setPreferredSize(new Dimension(400, 30));
        painel.add(campo, gbc);
    }

    private void limparCampos(JPanel painel) {
        for (Component component : painel.getComponents()) {
            if (component instanceof JTextField) {
                ((JTextField) component).setText("");
            }
        }
    }

    // =================================
    // CRIAR COMPONENTES DOS USUÁRIOS
    // =================================

    private void criarUsuarios(List<Usuario> usuarios, String busca, String criterio) {

        painelListaUsuarios.removeAll();

        for (Usuario usuario : usuarios) {

            JPanel itemUsuario = new JPanel(new BorderLayout());
            itemUsuario.setBackground(Color.WHITE);
            itemUsuario.setBorder(new EmptyBorder(5, 12, 5, 20));
            itemUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

            JLabel nomeUsuario;

            nomeUsuario = criterio.equals("nome")
                    ? new JLabel(usuario.getNome())
                    : new JLabel(usuario.getLogin());

            JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            painelBotoes.setBackground(Color.WHITE);

            JButton botaoEditar = new JButton("Editar");
            botaoEditar.setBackground(Color.GRAY);
            botaoEditar.setForeground(Color.WHITE);
            botaoEditar.setFocusPainted(false);
            botaoEditar.setBorderPainted(false);

            botaoEditar.addActionListener(e -> {
                this.abrirPopupUsuario(usuario);
            });

            JButton botaoRemover = this.createBotaoRemover(busca, criterio, usuario);

            painelBotoes.add(botaoEditar);
            painelBotoes.add(botaoRemover);

            itemUsuario.add(nomeUsuario, BorderLayout.CENTER);
            itemUsuario.add(painelBotoes, BorderLayout.EAST);

            painelListaUsuarios.add(itemUsuario);
            painelListaUsuarios.add(Box.createVerticalStrut(10));
        }

        painelListaUsuarios.revalidate();
        painelListaUsuarios.repaint();
    }

    private JButton createBotaoRemover(String busca, String criterio, Usuario usuario) {

        JButton botaoRemover = new JButton("Remover");
        botaoRemover.setBackground(new Color(220, 20, 30));
        botaoRemover.setForeground(Color.WHITE);
        botaoRemover.setFocusPainted(false);
        botaoRemover.setBorderPainted(false);

        botaoRemover.addActionListener(e -> {

            int opcao = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja remover esse usuario?",
                    "Remover usuario",
                    JOptionPane.YES_NO_OPTION
            );

            if (opcao == JOptionPane.YES_OPTION) {

                try {
                    this.usuarios.removerUsuario(usuario.getID());

                    if (criterio.equals("nome")) {
                        this.criarUsuarios(
                                this.usuarios.listarUsuariosPorNome(busca),
                                busca,
                                criterio
                        );
                    } else {
                        this.criarUsuarios(
                                this.usuarios.listarUsuariosPorLogin(busca),
                                busca,
                                criterio
                        );
                    }

                } catch (RegistroNaoEncontradoException | UsuarioPossuiEmprestimosAtivosException e1) {
                    JOptionPane.showMessageDialog(
                            this,
                            e1.getMessage(),
                            "Erro ao remover usuario",
                            JOptionPane.ERROR_MESSAGE
                    );

                } catch (OperacaoNaoPermitidaException one) {
                    JOptionPane.showMessageDialog(
                            this,
                            one.getMessage(),
                            "Erro de permissão",
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
            }
        });

        return botaoRemover;
    }

    private void abrirPopupUsuario(Usuario usuario) {

        LinkedHashMap<String, String> dados = new LinkedHashMap<>();

        dados.put("Nome", usuario.getNome());
        dados.put("Login", usuario.getLogin());
        dados.put("Senha", "");

        FormularioDialog popup = new FormularioDialog(
                SwingUtilities.getWindowAncestor(this),
                "Atualizar Usuário",
                dados,
                false
        );

        popup.setVisible(true);

        if (popup.isConfirmado()) {

            String nome = popup.getValor("Nome");

            if (nome.isBlank() || nome.isEmpty() || nome == null) {
                nome = usuario.getNome();
            }

            String login = popup.getValor("Login");

            if (login.isBlank() || login.isEmpty() || login == null) {
                login = usuario.getNome();
            }

            String senha = popup.getValor("Senha");

            if (!(senha.isBlank() || senha.isEmpty() || senha == null)) {

                try {
                    this.usuarios.atualizarSenhaDoUsuario(senha, usuario.getID());

                } catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Algum erro aconteceu ao atualizar a senha",
                            "ERRO",
                            JOptionPane.ERROR_MESSAGE
                    );

                } catch (RegistroNaoEncontradoException e1) {
                    JOptionPane.showMessageDialog(
                            this,
                            e1.getMessage(),
                            "Erro ao remover usuario",
                            JOptionPane.ERROR_MESSAGE
                    );

                } catch (CredenciaisInvalidasException e1) {
                    JOptionPane.showMessageDialog(
                            this,
                            e1.getMessage(),
                            "Erro nas credenciais digitadas",
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

                } catch (OperacaoNaoPermitidaException one) {
                    JOptionPane.showMessageDialog(
                            this,
                            one.getMessage(),
                            "Erro de permissão",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

            try {
                this.usuarios.atualizarUsuario(nome, login, usuario.getID());

                JOptionPane.showMessageDialog(
                        popup,
                        "Dados atualizados com sucesso!",
                        "Sucesso ao atualizar dados",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (OperacaoNaoPermitidaException one) {
                JOptionPane.showMessageDialog(
                        this,
                        one.getMessage(),
                        "Erro de permissão",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (RegistroNaoEncontradoException e1) {
                JOptionPane.showMessageDialog(
                        this,
                        e1.getMessage(),
                        "Erro ao remover usuario",
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

            } catch (CredenciaisInvalidasException e1) {
                JOptionPane.showMessageDialog(
                        this,
                        e1.getMessage(),
                        "Erro nas credenciais digitadas",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (ItemDuplicadoException e1) {
                JOptionPane.showMessageDialog(
                        this,
                        e1.getMessage(),
                        "Login já usado",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
