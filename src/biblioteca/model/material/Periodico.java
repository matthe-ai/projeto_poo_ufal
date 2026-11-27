package biblioteca.model.material;

public class Periodico extends MaterialBibliografico {

	// atributos

    private int numeroEdicao;
    private String dataPublicacao;
    private final TipoPeriodico tipoPeriodico;

    // construtor

    public Periodico(String titulo, String editora, int numeroEdicao, String dataPublicacao, TipoPeriodico tipoPeriodico, String nomeCapa) {
        super(titulo, editora, nomeCapa);
        this.tipoPeriodico = tipoPeriodico;
        this.setNumeroEdicao(numeroEdicao);
        this.setDataPublicacao(dataPublicacao.trim());
    }

    public Periodico(String titulo, String editora, int numeroEdicao, String dataPublicacao, TipoPeriodico tipoPeriodico, int id, String nomeCapa) {
        super(titulo, editora, id, nomeCapa);
        this.tipoPeriodico = tipoPeriodico;
        this.setNumeroEdicao(numeroEdicao);
        this.setDataPublicacao(dataPublicacao.trim());
    }

    // getters e setters

    public TipoPeriodico getTipoPeriodico() {
        return tipoPeriodico;
    }

    public int getNumeroEdicao() {
        return numeroEdicao;
    }

    public void setNumeroEdicao(int numeroEdicao) {
        this.numeroEdicao = numeroEdicao;
    }

    public String getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(String dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    // metodos

    @Override
    public String exibirDetalhes() {
        if (getTipoPeriodico() == TipoPeriodico.JORNAL) {
            return "Titulo do jornal: %s - Ano de publicação: %s - Numero de edição: %s - Editora: %s".formatted(this.getTitulo(), this.getDataPublicacao(), this.getNumeroEdicao(), this.getEditora());
        }

        return "Titulo da revista: %s - Ano de publicação: %s - Numero de edição: %s - Editora: %s".formatted(this.getTitulo(), this.getDataPublicacao(), this.getNumeroEdicao(), this.getEditora());
    }

}
