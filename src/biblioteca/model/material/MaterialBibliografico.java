package biblioteca.model.material;

import java.util.Random;

public abstract class MaterialBibliografico {

	// atributos
	
	private final int id;
	private String titulo;

	private String editora;
	private boolean disponivel;
	private String nomeCapa;
	
	public static final String capaDefault = "default.jpg";
	
	// construtor
	
	public MaterialBibliografico(String titulo, String editora, String nomeCapa) {
		this.id = new Random().nextInt();
		this.setTitulo(titulo.trim());
		this.setDisponivel(true);
		this.setNomeCapa(nomeCapa);
	}

	public MaterialBibliografico(String titulo, String editora, int id, String nomeCapa) {
		this.id = id;
		this.setTitulo(titulo.trim());
		this.setDisponivel(true);
		this.setEditora(editora.trim());
		this.setNomeCapa(nomeCapa);
	}
	
	// getters e setters

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo.trim();
	}

	public boolean estaDisponivel() {
		return this.disponivel;
	}

	private void setDisponivel(boolean disponivel) {
		this.disponivel = disponivel;
	}

	public int getId() {
		return id;
	}
	
	public String getEditora() {
		return editora;
	}

	public void setEditora(String editora) {
		this.editora = editora.trim();
	}
	
	public String getNomeCapa() {
		return nomeCapa;
	}

	public void setNomeCapa(String nomeCapa) {
		this.nomeCapa = nomeCapa;
	}
	
	// metodos

	public abstract String exibirDetalhes();

	public void marcarComoDisponivel() {
		this.setDisponivel(true);
	}

	public void marcarComoIndisponivel() {
		this.setDisponivel(false);
	}
}
