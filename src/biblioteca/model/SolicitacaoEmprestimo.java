package biblioteca.model;

import biblioteca.model.StatusEnums.StatusSolicitacao;
import biblioteca.model.material.MaterialBibliografico;
import biblioteca.model.usuario.Usuario;

import java.time.LocalDate;
import java.util.Random;

public class SolicitacaoEmprestimo {

    private final int id;
    private final Usuario solicitante;
    private final MaterialBibliografico materialSolicitado;
    private final LocalDate dataSolicitacao;
    private StatusSolicitacao status;

    public SolicitacaoEmprestimo(Usuario solicitante, MaterialBibliografico materialSolicitado) {
        this.id = new Random().nextInt();
        this.solicitante = solicitante;
        this.materialSolicitado = materialSolicitado;
        this.dataSolicitacao = LocalDate.now();
        this.status = StatusSolicitacao.PENDENTE;
    }

    public SolicitacaoEmprestimo(Usuario solicitante, MaterialBibliografico materialSolicitado, StatusSolicitacao status, int solicitacaoId) {
        this.id = solicitacaoId;
        this.solicitante = solicitante;
        this.materialSolicitado = materialSolicitado;
        this.dataSolicitacao = LocalDate.now();
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDataSolicitacao() {
        return dataSolicitacao;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public MaterialBibliografico getMaterialSolicitado() {
        return materialSolicitado;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacao status) {
        this.status = status;
    }
}
