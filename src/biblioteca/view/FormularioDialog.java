
package biblioteca.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class FormularioDialog extends JDialog {

    private final Map<String, JTextField> campos = new LinkedHashMap<>();

    private boolean confirmado = false;
    private File imagemSelecionada;

    private final boolean permitirImagem;

    private final Color fundo = new Color(246, 237, 222);
    private final Color marrom = new Color(55, 38, 28);
    private final Color amarelo = new Color(225, 165, 40);

    public FormularioDialog(
        Window owner,
        String titulo,
        LinkedHashMap<String, String> dados,
        boolean permitirImagem
    ) {
        super(owner, titulo, Dialog.ModalityType.APPLICATION_MODAL);

        this.permitirImagem = permitirImagem;

        configurarJanela(titulo);
        criarComponentes(dados);

        setLocationRelativeTo(owner);
    }

    private void configurarJanela(String titulo) {
        setLayout(new BorderLayout());
        setSize(631, permitirImagem ? 436 : 350);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void criarComponentes(LinkedHashMap<String, String> dados) {

        // Cabeçalho
        JPanel cabecalho = new JPanel(new GridBagLayout());
        cabecalho.setBackground(marrom);
        cabecalho.setPreferredSize(new Dimension(631, 53));

        JLabel labelTitulo = new JLabel(
            getTitle().startsWith("Atualizar")
                ? getTitle()
                : getTitle()
        );

        labelTitulo.setFont(new Font("Arial", Font.BOLD, 30));
        labelTitulo.setForeground(Color.WHITE);

        cabecalho.add(labelTitulo);
        add(cabecalho, BorderLayout.NORTH);

        // Conteúdo
        JPanel conteudo = new JPanel(new BorderLayout(20, 0));
        conteudo.setBackground(fundo);
        conteudo.setBorder(new EmptyBorder(25, 15, 15, 17));

        // Seleção de imagem (opcional)
        if (permitirImagem) {

            JPanel painelImagem = new JPanel();
            painelImagem.setLayout(
                new BoxLayout(painelImagem, BoxLayout.Y_AXIS)
            );
            painelImagem.setBackground(fundo);

            JLabel imagem = new JLabel("CAPA", SwingConstants.CENTER);
            imagem.setOpaque(true);
            imagem.setBackground(new Color(215, 215, 215));
            imagem.setPreferredSize(new Dimension(109, 143));
            imagem.setMaximumSize(new Dimension(109, 143));
            imagem.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton botaoImagem = new JButton("Editar");
            botaoImagem.setBackground(amarelo);
            botaoImagem.setForeground(Color.WHITE);
            botaoImagem.setFocusPainted(false);
            botaoImagem.setBorderPainted(false);
            botaoImagem.setAlignmentX(Component.CENTER_ALIGNMENT);

            botaoImagem.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();

                chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imagens (JPG, PNG)", "jpg", "png", "jpeg"));

                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    imagemSelecionada = chooser.getSelectedFile();
                    imagem.setText("Imagem selecionada");
                }
            });

            painelImagem.add(imagem);
            painelImagem.add(Box.createVerticalStrut(12));
            painelImagem.add(botaoImagem);

            conteudo.add(painelImagem, BorderLayout.WEST);
        }

        // Campos
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(fundo);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 0, 7, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int linha = 0;

        for (Map.Entry<String, String> entrada : dados.entrySet()) {

            JLabel label = new JLabel(entrada.getKey());
            label.setFont(new Font("Arial", Font.BOLD, 14));

            JTextField campo = new JTextField(entrada.getValue() == null ? "" : entrada.getValue());

            campo.setPreferredSize(new Dimension(325, 34));

            gbc.gridy = linha;
            gbc.gridx = 0;
            gbc.weightx = 0;
            gbc.insets = new Insets(7, 0, 7, 12);

            formulario.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.insets = new Insets(7, 0, 7, 0);

            formulario.add(campo, gbc);

            campos.put(entrada.getKey(), campo);

            linha++;
        }

        conteudo.add(formulario, BorderLayout.CENTER);
        add(conteudo, BorderLayout.CENTER);

        // Botões inferiores
        JPanel painelBotoes = new JPanel(new BorderLayout());
        painelBotoes.setBackground(fundo);
        painelBotoes.setBorder(new EmptyBorder(0, 80, 25, 50));

        JButton botaoSair = new JButton("Sair");
        botaoSair.setBackground(new Color(215, 200, 195));
        botaoSair.setForeground(Color.WHITE);
        botaoSair.setFont(new Font("Arial", Font.PLAIN, 32));
        botaoSair.setFocusPainted(false);
        botaoSair.setBorderPainted(false);
        botaoSair.setPreferredSize(new Dimension(150, 67));

        JButton botaoConfirmar = new JButton("Confirmar");
        botaoConfirmar.setBackground(amarelo);
        botaoConfirmar.setForeground(Color.WHITE);
        botaoConfirmar.setFont(new Font("Arial", Font.PLAIN, 32));
        botaoConfirmar.setFocusPainted(false);
        botaoConfirmar.setBorderPainted(false);
        botaoConfirmar.setPreferredSize(new Dimension(200, 67));

        botaoSair.addActionListener(e -> dispose());

        botaoConfirmar.addActionListener(e -> {
            confirmado = true;
            dispose();
        });

        painelBotoes.add(botaoSair, BorderLayout.WEST);
        painelBotoes.add(botaoConfirmar, BorderLayout.EAST);

        add(painelBotoes, BorderLayout.SOUTH);
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public String getValor(String nomeCampo) {
        JTextField campo = campos.get(nomeCampo);

        return campo != null ? campo.getText() : null;
    }

    public Map<String, String> getValores() {
        Map<String, String> valores = new LinkedHashMap<>();

        for (Map.Entry<String, JTextField> entrada : campos.entrySet()) {
            valores.put(entrada.getKey(), entrada.getValue().getText());
        }

        return valores;
    }

    public File getImagemSelecionada() {
        return imagemSelecionada;
    }
}
