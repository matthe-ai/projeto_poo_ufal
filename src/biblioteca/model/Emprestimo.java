package biblioteca.model;

import java.time.LocalDate;
import java.util.Random;

import biblioteca.exception.MaterialDevolvidoException;
import biblioteca.model.StatusEnums.StatusEmprestimo;
import biblioteca.model.material.MaterialBibliografico;
import biblioteca.model.usuario.Usuario;

public class Emprestimo {

	// atributos
	
	private final int id;
	private final LocalDate dataEmprestimo;
	private LocalDate dataPrevistaDevolucao;
	private LocalDate dataDevolucao;
	private StatusEmprestimo status;
	private final int materialId;
	private final int usuarioId;
	
	// construtor

	public Emprestimo(LocalDate dataPrevistaDevolucao, int materialId, int usuarioId) {
		this.id = new Random().nextInt();
		this.dataEmprestimo = LocalDate.now();
		this.setStatus(StatusEmprestimo.ATIVO);

		this.dataPrevistaDevolucao = dataPrevistaDevolucao;
		this.materialId = materialId;
		this.usuarioId = usuarioId;
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

	public StatusEmprestimo getStatus() {
		return status;
	}

	private void setStatus(StatusEmprestimo status) {
		this.status = status;
	}

	public int getMaterialId() {
		return this.materialId;
	}

	public int getUsuarioId() {
		return this.usuarioId;
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
