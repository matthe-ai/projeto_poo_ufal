package biblioteca.model.material;

import java.nio.file.Path;

public class Livro extends MaterialBibliografico{

	// atributos
	
	private String autor;
	private String isbn;
	private int anoPublicacao;
	// construtor
	
	public Livro(String titulo, int anoPublicacao, String autor, String isbn, String editora, String nomeCapa) {
		super(titulo, editora, nomeCapa);
		this.setAnoPublicacao(anoPublicacao);
		this.setAutor(autor.trim());
		this.setIsbn(isbn.trim());
		this.setEditora(editora.trim());
	}

	public Livro(String titulo, int anoPublicacao, String autor, String isbn, String editora, int id, String nomeCapa) {
		super(titulo, editora, id, nomeCapa);
		this.setAnoPublicacao(anoPublicacao);
		this.setAutor(autor.trim());
		this.setIsbn(isbn.trim());
		this.setEditora(editora.trim());
	}
	
	// getters e setters

	public String getAutor() {
		return autor;
	}

	public void setAutor(String autor) {
		this.autor = autor;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public int getAnoPublicacao() {
		return anoPublicacao;
	}

	public void setAnoPublicacao(int anoPublicacao) {
		this.anoPublicacao = anoPublicacao;
	}

	// metodos
	
	@Override
	public String exibirDetalhes() {
		return "Titulo do livro: %s - Autor: %s - Ano de publicação: %s - ISBN: %s - Editora: %s".formatted(this.getTitulo(), this.getAutor(), this.getAnoPublicacao(), this.getIsbn(), this.getEditora());
	}
}
