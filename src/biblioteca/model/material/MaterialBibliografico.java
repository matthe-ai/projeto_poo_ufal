package biblioteca.model.material;

import java.nio.file.Path;
import java.util.Random;

public abstract class MaterialBibliografico {

	// atributos
	
	private final int id;
	private String titulo;

	private String editora;
	private boolean disponivel;
	private Path caminhoCapa;
	
	public final static Path capaDefault = Path.of("src", "biblioteca", "upload", "default.jpg");
	
	// construtor
	
	public MaterialBibliografico(String titulo, String editora, Path caminhoCapa) {
		this.id = new Random().nextInt();
		this.setTitulo(titulo.trim());
		this.setEditora(editora);
		this.setDisponivel(true);
		this.setCaminhoCapa(caminhoCapa);
	}

	public MaterialBibliografico(String titulo, String editora, int id, Path caminhoCapa) {
		this.id = id;
		this.setTitulo(titulo.trim());
		this.setDisponivel(true);
		this.setEditora(editora);
		this.setCaminhoCapa(caminhoCapa);
	}
	
	// getters e setters

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public boolean estaDisponivel() {
		return this.disponivel;
	}

	public void setDisponivel(boolean disponivel) {
		this.disponivel = disponivel;
	}

	public int getId() {
		return id;
	}
	
	public String getEditora() {
		return editora;
	}

	public void setEditora(String editora) {
		this.editora = editora;
	}
	
	public Path getCaminhoCapa() {
		return caminhoCapa;
	}

	public void setCaminhoCapa(Path caminhoCapa) {
		this.caminhoCapa = caminhoCapa;
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
