package biblioteca.model;

import java.time.LocalDate;
import java.util.Random;

import biblioteca.exception.MaterialDevolvidoException;
import biblioteca.model.material.MaterialBibliografico;
import biblioteca.model.usuario.Usuario;

public class Emprestimo {

	// atributos
	
	private final int id;
	private final LocalDate dataEmprestimo;
	private LocalDate dataPrevistaDevolucao;
	private LocalDate dataDevolucao;
	private StatusEmprestimo status;
	private MaterialBibliografico material;
	private Usuario usuario;
	
	// construtor
	
	public Emprestimo() {
		this.id = new Random().nextInt();
		this.dataEmprestimo = LocalDate.now();
		this.setStatus(StatusEmprestimo.ATIVO);
	}

	public Emprestimo(LocalDate dataPrevistaDevolucao, MaterialBibliografico material, Usuario usuario) {
		this.id = new Random().nextInt();
		this.dataEmprestimo = LocalDate.now();
		this.setStatus(StatusEmprestimo.ATIVO);

		this.dataPrevistaDevolucao = dataPrevistaDevolucao;
		this.material = material;
		this.usuario = usuario;
	}
	
	// getters e setters

	public LocalDate getDataPrevistaDevolucao() {
		return dataPrevistaDevolucao;
	}

	public void setDataPrevistaDevolucao(LocalDate dataPrevistaDevolucao) {
		this.dataPrevistaDevolucao = dataPrevistaDevolucao;
	}

	public LocalDate getDataDevolucao() {
		return dataDevolucao;
	}

	public void setDataDevolucao(LocalDate dataDevolucao) {
		this.dataDevolucao = dataDevolucao;
	}

	public StatusEmprestimo getStatus() {
		return status;
	}

	private void setStatus(StatusEmprestimo status) {
		this.status = status;
	}

	public void setMaterial(MaterialBibliografico material) {
		this.material = material;
	}

	public MaterialBibliografico getMaterial() {
		return this.material;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Usuario getUsuario() {
		return this.usuario;
	}

	public int getId() {
		return id;
	}

	public LocalDate getDataEmprestimo() {
		return dataEmprestimo;
	}
	
	// metodos
	
	public void registrarDevolucao() throws MaterialDevolvidoException {

		if (dataDevolucao != null) {
			throw new MaterialDevolvidoException();
		}

		this.dataDevolucao = LocalDate.now();
		this.setStatus(StatusEmprestimo.DEVOLVIDO);
	}
	
	public boolean estaAtivo() {
		return this.getStatus() == StatusEmprestimo.ATIVO;
	}
}
