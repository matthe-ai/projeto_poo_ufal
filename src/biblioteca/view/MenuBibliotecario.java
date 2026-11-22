
package biblioteca.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JRootPane;
import javax.swing.border.EmptyBorder;

import biblioteca.exception.CredenciaisInvalidasException;
import biblioteca.exception.ItemDuplicadoException;
import biblioteca.exception.MaterialIndisponivelException;
import biblioteca.exception.OperacaoNaoPermitidaException;
import biblioteca.exception.RegistroNaoEncontradoException;
import biblioteca.exception.TipoMaterialInvalidoException;
import biblioteca.exception.UsuarioNaoAutenticadoException;
import biblioteca.model.material.Livro;
import biblioteca.model.material.MaterialBibliografico;
import biblioteca.model.material.Periodico;
import biblioteca.model.material.TipoPeriodico;
import biblioteca.service.CatalogoService;

public class MenuBibliotecario extends JPanel {

    // Atributos

    private final CatalogoService catalogo;
    private final Runnable voltarLogin;
    private Path caminhoCapa;
    private String campoBuscar;

    private JButton botaoBuscar;

    private final JLabel labelAutor = new JLabel("Autor");
    private final JLabel labelISBN = new JLabel("ISBN");
    private final JLabel labelDataPublicacao = new JLabel("Data de publicação");
    private final JLabel labelNumeroEdicao = new JLabel("Número de edição");

    private JPanel painelPrincipal;
    private JPanel painelListaMateriais;
    private JScrollPane scrollMateriais;

    // Construtor

    public MenuBibliotecario(CatalogoService catalogo, Runnable voltarLogin) {
        this.catalogo = catalogo;
        this.voltarLogin = voltarLogin;
        configurarPainel();
        criarComponentes();
    }

    // Métodos

    private void configurarPainel() {
        setLayout(new BorderLayout());
        setBackground(new Color(220, 220, 220));
        setBorder(new EmptyBorder(25, 30, 30, 30));
    }

    public JButton getBotaoPadrao() {
        return botaoBuscar;
    }

    private void criarComponentes() {

        this.painelPrincipal = new JPanel();
        painelPrincipal.setLayout(new BoxLayout(painelPrincipal, BoxLayout.Y_AXIS));
        painelPrincipal.setBackground(new Color(220, 220, 220));

        // =================================
        // EDITAR OBRA
        // =================================

        JLabel tituloEditarObras = new JLabel("Editar obra");
        tituloEditarObras.setFont(new Font("Arial", Font.PLAIN, 16));
        tituloEditarObras.setAlignmentX(Component.LEFT_ALIGNMENT);

        painelPrincipal.add(tituloEditarObras);
        painelPrincipal.add(Box.createVerticalStrut(20));

        // Campo de pesquisa e botão

        JPanel painelPesquisa = new JPanel(new BorderLayout(28, 0));
        painelPesquisa.setBackground(new Color(220, 220, 220));
        painelPesquisa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        painelPesquisa.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField campoPesquisa = new JTextField();
        botaoBuscar = new JButton("Buscar obra");

        botaoBuscar.setBackground(Color.WHITE);
        botaoBuscar.setFocusPainted(false);
        botaoBuscar.setBorderPainted(false);

        painelPesquisa.add(campoPesquisa, BorderLayout.CENTER);
        painelPesquisa.add(botaoBuscar, BorderLayout.EAST);

        botaoBuscar.addActionListener(e -> {

            this.campoBuscar = campoPesquisa.getText();

            List<MaterialBibliografico> materiais;

            try {

                materiais = !campoBuscar.isBlank()
                        ? this.catalogo.listarMateriaisPorTitulo(campoBuscar)
                        : this.catalogo.listarMateriais();

                this.criarMateriais(materiais);

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
        });

        painelPrincipal.add(painelPesquisa);
        painelPrincipal.add(Box.createVerticalStrut(22));

        // Painel que receberá os materiais

        painelListaMateriais = new JPanel();

        painelListaMateriais.setLayout(
                new BoxLayout(painelListaMateriais, BoxLayout.Y_AXIS)
        );

        painelListaMateriais.setBackground(new Color(220, 220, 220));

        // Área de rolagem

        scrollMateriais = new JScrollPane(painelListaMateriais);
        scrollMateriais.setBorder(null);
        scrollMateriais.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );
        scrollMateriais.getVerticalScrollBar().setUnitIncrement(15);
        scrollMateriais.setPreferredSize(new Dimension(800, 200));
        scrollMateriais.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        scrollMateriais.setMinimumSize(new Dimension(0, 200));
        scrollMateriais.setAlignmentX(Component.LEFT_ALIGNMENT);

        painelPrincipal.add(scrollMateriais);
        painelPrincipal.add(Box.createVerticalStrut(15));

        // =================================
        // CADASTRAR OBRA
        // =================================

        JLabel tituloCadastro = new JLabel("Cadastrar obra");
        tituloCadastro.setFont(new Font("Arial", Font.PLAIN, 16));
        tituloCadastro.setAlignmentX(Component.LEFT_ALIGNMENT);

        painelPrincipal.add(tituloCadastro);
        painelPrincipal.add(Box.createVerticalStrut(5));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(new Color(220, 220, 220));
        formulario.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Campos do formulário

        JTextField campoTitulo = new JTextField();
        JTextField campoEditora = new JTextField();
        JTextField campoAno = new JTextField();
        JTextField campoAutor = new JTextField();
        JTextField campoISBN = new JTextField();
        JTextField campoDataPublicacao = new JTextField();

        campoDataPublicacao.addActionListener(e -> {

            String data = campoDataPublicacao.getText();

            try {

                DateTimeFormatter formatter =
                        DateTimeFormatter.ofPattern("dd/MM/uuuu")
                                .withResolverStyle(ResolverStyle.STRICT);

                LocalDate.parse(data, formatter);

            } catch (DateTimeParseException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Data inválida! Utilize o formato dd/mm/aaaa.",
                        "Erro de validação",
                        JOptionPane.ERROR_MESSAGE
                );

                campoDataPublicacao.requestFocus();
            }
        });

        JTextField campoNumeroEdicao = new JTextField();

        DefaultComboBoxModel<String> model =
                new DefaultComboBoxModel<String>(
                        new String[]{"Tipo", "Livro", "Revista", "Jornal"}
                ) {

                    @Override
                    public void setSelectedItem(Object anObject) {

                        if ("Tipo".equals(anObject)) {
                            return;
                        }

                        super.setSelectedItem(anObject);
                    }
                };

        JComboBox<String> comboTipo = new JComboBox<String>(model);

        comboTipo.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                boolean destacar = isSelected && !"Tipo".equals(value);

                super.getListCellRendererComponent(
                        list,
                        value,
                        index,
                        destacar,
                        cellHasFocus
                );

                if ("Tipo".equals(value)) {
                    setForeground(Color.GRAY);
                } else {
                    setForeground(
                            isSelected
                                    ? list.getSelectionForeground()
                                    : list.getForeground()
                    );
                }

                return this;
            }
        });

        // Título

        adicionarCampo(formulario, gbc, 0, "Título", campoTitulo);

        // Editora

        adicionarCampo(formulario, gbc, 1, "Editora", campoEditora);

        // Ano de publicação e tipo

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        formulario.add(new JLabel("Ano de publicação"), gbc);

        JPanel painelAnoTipo = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0)
        );

        painelAnoTipo.setBackground(new Color(220, 220, 220));

        campoAno.setPreferredSize(new Dimension(80, 25));
        comboTipo.setPreferredSize(new Dimension(100, 25));

        painelAnoTipo.add(campoAno);
        painelAnoTipo.add(Box.createHorizontalStrut(10));
        painelAnoTipo.add(comboTipo);
        painelAnoTipo.setAlignmentX(LEFT_ALIGNMENT);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        formulario.add(painelAnoTipo, gbc);

        comboTipo.addActionListener(e -> {

            if (!Objects.equals(comboTipo.getSelectedItem(), "Tipo")) {

                if (Objects.equals(comboTipo.getSelectedItem(), "Livro")) {

                    removerCampo(
                            formulario,
                            labelDataPublicacao,
                            campoDataPublicacao
                    );

                    removerCampo(
                            formulario,
                            labelNumeroEdicao,
                            campoNumeroEdicao
                    );

                    adicionarCampo(
                            formulario,
                            gbc,
                            3,
                            labelAutor,
                            campoAutor
                    );

                    adicionarCampo(
                            formulario,
                            gbc,
                            4,
                            labelISBN,
                            campoISBN
                    );

                } else {

                    removerCampo(formulario, labelAutor, campoAutor);
                    removerCampo(formulario, labelISBN, campoISBN);

                    adicionarCampo(
                            formulario,
                            gbc,
                            3,
                            labelDataPublicacao,
                            campoDataPublicacao
                    );

                    adicionarCampo(
                            formulario,
                            gbc,
                            6,
                            labelNumeroEdicao,
                            campoNumeroEdicao
                    );
                }
            }

            formulario.revalidate();
            formulario.repaint();
        });

        // =================================
        // ADICIONAR CAPA
        // =================================

        JPanel painelCapa = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0)
        );

        painelCapa.setOpaque(false);

        JLabel labelCapa = new JLabel("Adicionar capa");

        JButton botaoEscolherCapa = new JButton("Escolher Imagem");

        botaoEscolherCapa.addActionListener(e -> {

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Selecionar capa do livro");

            fileChooser.setFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter(
                            "Imagens (JPG, PNG)",
                            "jpg",
                            "png",
                            "jpeg"
                    )
            );

            int resultado = fileChooser.showOpenDialog(formulario);

            if (resultado == JFileChooser.APPROVE_OPTION) {

                File arquivoSelecionado = fileChooser.getSelectedFile();

                try {

                    Path pastaDestino = Path.of(
                            "src",
                            "biblioteca",
                            "upload"
                    );

                    Path destinoFinal = pastaDestino.resolve(
                            arquivoSelecionado.getName()
                    );

                    Files.copy(
                            arquivoSelecionado.toPath(),
                            destinoFinal,
                            StandardCopyOption.REPLACE_EXISTING
                    );

                    this.caminhoCapa = destinoFinal;

                    botaoEscolherCapa.setText("Capa selecionada!");

                    JOptionPane.showMessageDialog(
                            formulario,
                            "Capa carregada com sucesso!"
                    );

                } catch (Exception ex) {

                    this.caminhoCapa = MaterialBibliografico.capaDefault;

                    botaoEscolherCapa.setText("Capa padrão utilizada");

                    JOptionPane.showMessageDialog(
                            formulario,
                            "Não foi possível carregar a imagem.\n"
                                    + "Uma capa padrão será utilizada.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });

        painelCapa.add(labelCapa);
        painelCapa.add(Box.createHorizontalStrut(10));
        painelCapa.add(botaoEscolherCapa);

        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        formulario.add(painelCapa, gbc);

        // =================================
        // BOTÃO ADICIONAR
        // =================================

        JButton botaoAdicionar = new JButton("Adicionar material");

        botaoAdicionar.setBackground(new Color(220, 20, 30));
        botaoAdicionar.setForeground(Color.WHITE);
        botaoAdicionar.setFocusPainted(false);
        botaoAdicionar.setBorderPainted(false);

        botaoAdicionar.addActionListener(e -> {

            String tipo = (String) comboTipo.getSelectedItem();

            if (this.caminhoCapa == null) {
                this.caminhoCapa = MaterialBibliografico.capaDefault;
            }

            switch (tipo) {

                case "Livro": {

                    try {

                        this.catalogo.cadastrarLivro(
                                campoTitulo.getText(),
                                Integer.parseInt(campoAno.getText()),
                                campoAutor.getText(),
                                campoISBN.getText(),
                                campoEditora.getText(),
                                this.caminhoCapa
                        );

                        limparCampos(formulario);
                        botaoEscolherCapa.setText("Escolher imagem");
                        this.caminhoCapa = null;

                    } catch (NumberFormatException e1) {

                        JOptionPane.showMessageDialog(
                                this,
                                "O ano digitado deve ser númerico",
                                "Erro de digitação",
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

                    } catch (ItemDuplicadoException | CredenciaisInvalidasException ide) {

                        JOptionPane.showMessageDialog(
                                this,
                                ide.getMessage(),
                                "Erro de cadastro/atualização",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }

                    break;
                }

                case "Revista", "Jornal": {

                    try {

                        TipoPeriodico tipoP;

                        if (tipo.equals("Revista")) {
                            tipoP = TipoPeriodico.REVISTA;
                        } else {
                            tipoP = TipoPeriodico.JORNAL;
                        }

                        this.catalogo.cadastrarPeriodico(
                                campoTitulo.getText(),
                                Integer.parseInt(campoAno.getText()),
                                campoEditora.getText(),
                                Integer.parseInt(campoNumeroEdicao.getText()),
                                campoDataPublicacao.getText(),
                                tipoP,
                                this.caminhoCapa
                        );

                        limparCampos(formulario);
                        this.caminhoCapa = null;

                    } catch (NumberFormatException e1) {

                        JOptionPane.showMessageDialog(
                                this,
                                "O ano digitado e o número de edição devem ser númerico",
                                "Erro de digitação",
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

                    } catch (ItemDuplicadoException | CredenciaisInvalidasException ide) {

                        JOptionPane.showMessageDialog(
                                this,
                                ide.getMessage(),
                                "Erro de cadastro/atualização",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }

                    break;
                }
            }
        });

        formulario.add(botaoAdicionar);

        painelPrincipal.add(formulario);
        add(painelPrincipal, BorderLayout.NORTH);
    }

    private void limparCampos(JPanel painel) {

        for (Component component : painel.getComponents()) {

            if (component instanceof JTextField) {
                ((JTextField) component).setText("");
            }
        }
    }

    private void adicionarCampo(
            JPanel painel,
            GridBagConstraints gbc,
            int linha,
            JLabel label,
            JTextField campo
    ) {

        gbc.gridy = linha;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        painel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        campo.setPreferredSize(new Dimension(400, 25));

        painel.add(campo, gbc);
    }

    private void adicionarCampo(
            JPanel painel,
            GridBagConstraints gbc,
            int linha,
            String label,
            JTextField campo
    ) {

        gbc.gridy = linha;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        painel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        campo.setPreferredSize(new Dimension(400, 25));

        painel.add(campo, gbc);
    }

    private void removerCampo(
            JPanel painel,
            JLabel label,
            JTextField campo
    ) {

        painel.remove(label);
        painel.remove(campo);

        painel.revalidate();
        painel.repaint();
    }

    private void criarMateriais(List<MaterialBibliografico> materiais) {

        painelListaMateriais.removeAll();

        for (MaterialBibliografico material : materiais) {

            JPanel painelObra = new JPanel(new BorderLayout(15, 0));

            painelObra.setBackground(Color.WHITE);
            painelObra.setBorder(new EmptyBorder(12, 15, 12, 15));
            painelObra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
            painelObra.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Informações do material

            JPanel informacoes = new JPanel(new GridLayout(2, 3, 20, 8));
            informacoes.setBackground(Color.WHITE);

            informacoes.add(new JLabel(material.getTitulo()));

            if (material instanceof Livro) {

                Livro material_temp = (Livro) material;

                informacoes.add(new JLabel(material_temp.getAutor()));
            }

            informacoes.add(new JLabel(material.getEditora()));
            informacoes.add(
                    new JLabel(String.valueOf(material.getAnoPublicacao()))
            );

            if (material instanceof Periodico) {

                Periodico material_temp = (Periodico) material;

                informacoes.add(
                        new JLabel(String.valueOf(material_temp.getNumeroEdicao()))
                );
            }

            // Painel dos botões

            JPanel painelBotoes = this.createPainelBotoes(material);

            painelObra.add(informacoes, BorderLayout.CENTER);
            painelObra.add(painelBotoes, BorderLayout.EAST);

            painelListaMateriais.add(painelObra);
            painelListaMateriais.add(Box.createVerticalStrut(5));
        }

        painelListaMateriais.revalidate();
        painelListaMateriais.repaint();

        scrollMateriais.revalidate();
        scrollMateriais.repaint();

        this.painelPrincipal.revalidate();
        this.painelPrincipal.repaint();
    }

    private JPanel createPainelBotoes(MaterialBibliografico material) {

        JPanel painelBotoes = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 5, 0)
        );

        painelBotoes.setBackground(Color.WHITE);

        // Botão editar

        JButton botaoEditar = new JButton("Editar");

        botaoEditar.setBackground(new Color(230, 165, 35));
        botaoEditar.setForeground(Color.WHITE);
        botaoEditar.setFocusPainted(false);
        botaoEditar.setBorderPainted(false);

        botaoEditar.addActionListener(e -> {

            if (material instanceof Livro) {
                abrirPopupEdicao((Livro) material);
            } else {
                abrirPopupEdicao((Periodico) material);
            }
        });

        // Botão remover

        JButton botaoRemover = new JButton("Remover");

        botaoRemover.setBackground(new Color(220, 20, 30));
        botaoRemover.setForeground(Color.WHITE);
        botaoRemover.setFocusPainted(false);
        botaoRemover.setBorderPainted(false);

        botaoRemover.addActionListener(e -> {

            int opcao = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja realmente remover esse material?",
                    "Remover material",
                    JOptionPane.YES_NO_OPTION
            );

            if (opcao == JOptionPane.YES_OPTION) {

                try {

                    this.catalogo.removerMaterial(material.getId());

                    this.criarMateriais(
                            this.catalogo.listarMateriaisPorTitulo(this.campoBuscar)
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
                            "Erro de busca",
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

                } catch (MaterialIndisponivelException mie) {

                    JOptionPane.showMessageDialog(
                            this,
                            mie.getMessage(),
                            "Erro ao remover",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        painelBotoes.add(botaoEditar);
        painelBotoes.add(botaoRemover);

        return painelBotoes;
    }

    private void abrirPopupEdicao(Livro material) {

        LinkedHashMap<String, String> dados = new LinkedHashMap<>();

        dados.put("Titulo", material.getTitulo());
        dados.put("Autor", material.getAutor());
        dados.put("Ano de publicação", String.valueOf(material.getAnoPublicacao()));
        dados.put("Editora", material.getEditora());
        dados.put("ISBN", material.getIsbn());

        FormularioDialog popup = new FormularioDialog(
                SwingUtilities.getWindowAncestor(this),
                "Atualizar Material",
                dados,
                true
        );

        popup.setVisible(true);

        if (popup.isConfirmado()) {

            String titulo = popup.getValor("Titulo");
            String autor = popup.getValor("Autor");
            int ano = Integer.parseInt(popup.getValor("Ano de publicação"));
            String editora = popup.getValor("Editora");
            String isbn = popup.getValor("ISBN");

            File novaCapa = popup.getImagemSelecionada();

            Path novaCapaPath = material.getCaminhoCapa();

            if (novaCapa != null && novaCapa.isFile()) {

                try {

                    Path pastaDestino = Path.of(
                            "src",
                            "biblioteca",
                            "upload"
                    );

                    Path destinoFinal = pastaDestino.resolve(novaCapa.getName());

                    Files.copy(
                            novaCapa.toPath(),
                            destinoFinal,
                            StandardCopyOption.REPLACE_EXISTING
                    );

                    novaCapaPath = destinoFinal;

                    JOptionPane.showMessageDialog(
                            popup,
                            "Capa carregada com sucesso!"
                    );

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(
                            popup,
                            "Não foi possível carregar a imagem.\n"
                                    + "Uma capa padrão será utilizada.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }

            try {

                this.catalogo.atualizarLivro(
                        titulo,
                        ano,
                        editora,
                        novaCapaPath,
                        autor,
                        isbn,
                        material.getId()
                );

            } catch (OperacaoNaoPermitidaException one) {

                JOptionPane.showMessageDialog(
                        popup,
                        one.getMessage(),
                        "Erro de permissão",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (RegistroNaoEncontradoException e1) {

                JOptionPane.showMessageDialog(
                        popup,
                        e1.getMessage(),
                        "Erro de busca",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (UsuarioNaoAutenticadoException une) {

                JOptionPane.showMessageDialog(
                        popup,
                        une.getMessage(),
                        "Erro de autenticação",
                        JOptionPane.ERROR_MESSAGE
                );

                voltarLogin.run();

            } catch (TipoMaterialInvalidoException e) {

                JOptionPane.showMessageDialog(
                        popup,
                        e.getMessage(),
                        "Erro no tipo de material",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (CredenciaisInvalidasException cie) {

                JOptionPane.showMessageDialog(
                        popup,
                        cie.getMessage(),
                        "Erro de cadastro/atualização",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void abrirPopupEdicao(Periodico material) {

        LinkedHashMap<String, String> dados = new LinkedHashMap<>();

        dados.put("Titulo", material.getTitulo());
        dados.put("Ano de publicação", String.valueOf(material.getAnoPublicacao()));
        dados.put("Editora", material.getEditora());
        dados.put("Data de publicação", material.getDataPublicacao());
        dados.put("N edição", String.valueOf(material.getNumeroEdicao()));

        FormularioDialog popup = new FormularioDialog(
                SwingUtilities.getWindowAncestor(this),
                "Atualizar Material",
                dados,
                true
        );

        popup.setVisible(true);

        if (popup.isConfirmado()) {

            String titulo = popup.getValor("Titulo");
            int ano = Integer.parseInt(popup.getValor("Ano de publicação"));
            String editora = popup.getValor("Editora");
            String data = popup.getValor("Data de publicação");
            int edicao = Integer.parseInt(popup.getValor("N edição"));

            File novaCapa = popup.getImagemSelecionada();

            Path novaCapaPath = material.getCaminhoCapa();

            if (novaCapa != null && novaCapa.isFile()) {

                try {

                    Path pastaDestino = Path.of(
                            "src",
                            "biblioteca",
                            "upload"
                    );

                    Path destinoFinal = pastaDestino.resolve(novaCapa.getName());

                    Files.copy(
                            novaCapa.toPath(),
                            destinoFinal,
                            StandardCopyOption.REPLACE_EXISTING
                    );

                    novaCapaPath = destinoFinal;

                    JOptionPane.showMessageDialog(
                            popup,
                            "Capa carregada com sucesso!"
                    );

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(
                            popup,
                            "Não foi possível carregar a imagem.\n"
                                    + "Uma capa padrão será utilizada.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }

            try {

                this.catalogo.atualizarPeriodico(
                        titulo,
                        ano,
                        editora,
                        novaCapaPath,
                        edicao,
                        data,
                        material.getId()
                );

            } catch (OperacaoNaoPermitidaException one) {

                JOptionPane.showMessageDialog(
                        popup,
                        one.getMessage(),
                        "Erro de permissão",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (RegistroNaoEncontradoException e1) {

                JOptionPane.showMessageDialog(
                        popup,
                        e1.getMessage(),
                        "Erro de busca",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (UsuarioNaoAutenticadoException une) {

                JOptionPane.showMessageDialog(
                        popup,
                        une.getMessage(),
                        "Erro de autenticação",
                        JOptionPane.ERROR_MESSAGE
                );

                voltarLogin.run();

            } catch (TipoMaterialInvalidoException e) {

                JOptionPane.showMessageDialog(
                        popup,
                        e.getMessage(),
                        "Erro no tipo de material",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (CredenciaisInvalidasException cie) {

                JOptionPane.showMessageDialog(
                        popup,
                        cie.getMessage(),
                        "Erro de cadastro/atualização",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
