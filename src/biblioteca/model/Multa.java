package biblioteca.model;


import biblioteca.exception.MultaJaProcessada;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

public class Multa {

    private final int id;
    private final long diasAtraso;
    private final BigDecimal valor;
    private final LocalDate dataGeracao;
    private final Emprestimo emprestimo;
    private StatusMulta status;
    private LocalDate dataPagamento;

    public Multa(long diasAtraso, BigDecimal valor, Emprestimo emprestimo) {
        this.id =  new Random().nextInt();
        this.diasAtraso = diasAtraso;
        this.valor = valor;
        this.dataGeracao = LocalDate.now();
        this.status = StatusMulta.PENDENTE;
        this.emprestimo = emprestimo;
    }

    public Multa(long diasAtraso, BigDecimal valor, LocalDate dataGeracao, Emprestimo emprestimo, int id) {
        this.id = id;
        this.diasAtraso = diasAtraso;
        this.valor = valor;
        this.dataGeracao = dataGeracao;
        this.emprestimo = emprestimo;
        this.status = StatusMulta.PENDENTE;
    }

    public int getId() {
        return id;
    }

    public Emprestimo getEmprestimo() {
        return emprestimo;
    }

    public StatusMulta getStatus() {
        return status;
    }

    private void setStatus(StatusMulta status) {
        this.status = status;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    private void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public LocalDate getDataGeracao() {
        return dataGeracao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public long getDiasAtraso() {
        return diasAtraso;
    }

    public void registrarPagamento() {

        if (this.dataPagamento != null || this.status != StatusMulta.PENDENTE) {
            throw new MultaJaProcessada();
        }

        setDataPagamento(LocalDate.now());
        setStatus(StatusMulta.PAGA);
    }

    public void registrarCancelamento() {

        if (this.dataPagamento != null || this.status != StatusMulta.PENDENTE) {
            throw new MultaJaProcessada();
        }

        setStatus(StatusMulta.CANCELADA);
    }
}