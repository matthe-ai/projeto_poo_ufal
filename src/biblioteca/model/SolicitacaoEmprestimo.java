package biblioteca.model;

import biblioteca.model.StatusEnums.StatusSolicitacao;
import java.time.LocalDate;
import java.util.Random;

public class SolicitacaoEmprestimo {

    private final int id;
    private final int solicitanteId;
    private final int materialSolicitadoId;
    private final LocalDate dataSolicitacao;
    private StatusSolicitacao status;

    public SolicitacaoEmprestimo(int solicitanteId, int materialSolicitadoId) {
        this.id = new Random().nextInt();
        this.solicitanteId = solicitanteId;
        this.materialSolicitadoId = materialSolicitadoId;
        this.dataSolicitacao = LocalDate.now();
        this.status = StatusSolicitacao.PENDENTE;
    }

    public SolicitacaoEmprestimo(int solicitanteId, int materialSolicitadoId, StatusSolicitacao status, int solicitacaoId) {
        this.id = solicitacaoId;
        this.solicitanteId = solicitanteId;
        this.materialSolicitadoId = materialSolicitadoId;
        this.dataSolicitacao = LocalDate.now();
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDataSolicitacao() {
        return dataSolicitacao;
    }

    public int getSolicitanteId() {
        return solicitanteId;
    }

    public int getMaterialSolicitadoId() {
        return materialSolicitadoId;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacao status) {
        this.status = status;
    }
}
