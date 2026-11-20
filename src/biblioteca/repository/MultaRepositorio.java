package biblioteca.repository;

import biblioteca.exception.ItemDuplicadoException;
import biblioteca.exception.RegistroNaoEncontradoException;
import biblioteca.model.Multa;
import biblioteca.model.StatusMulta;


import java.util.ArrayList;
import java.util.List;

public class MultaRepositorio implements Repositorio<Multa> {

    private final ArrayList<Multa> multas;

    public MultaRepositorio() {
        this.multas = new ArrayList<Multa>();
    }

    @Override
    public void adicionar(Multa multa) throws ItemDuplicadoException {

        for (Multa m: multas) {
            if (m.getId() == multa.getId()) {
                throw new ItemDuplicadoException("multa");
            }

            // Impede multas duplicadas para um mesmo EMPRÉSTIMO
            if (m.getEmprestimo().getId() == multa.getEmprestimo().getId()) {
                throw new ItemDuplicadoException("multa", "empréstimo");
            }
        }
        multas.add(multa);
    }

    @Override
    public Multa buscarPorId(int multaId) throws RegistroNaoEncontradoException {
        for (Multa m: multas) {
            if (m.getId() == multaId) {
                return m;
            }
        }
        throw new RegistroNaoEncontradoException();
    }

    @Override
    public List<Multa> listarTodos() {
        return new ArrayList<Multa>(multas);
    }

    public List<Multa> listarPorStatus(StatusMulta status) {
        List<Multa> multasFiltradas = new ArrayList<>();

        for (Multa multa : multas) {
            if (multa.getStatus() == status) {
                multasFiltradas.add(multa);
            }
        }

        return multasFiltradas;
    }

    public List<Multa> listarPorUsuario(int usuarioId) {
        List<Multa> multasFiltradas = new ArrayList<>();

        for (Multa multa : multas) {
            if (multa.getEmprestimo().getUsuario().getID() == usuarioId) {
                multasFiltradas.add(multa);
            }
        }

        return multasFiltradas;
    }

    public List<Multa> listarPorUsuarioEStatus(int usuarioId, StatusMulta status) {
        List<Multa> multasFiltradas = new ArrayList<>();
        List<Multa> multasDoUsuario = this.listarPorUsuario(usuarioId);

        for (Multa multa : multasDoUsuario) {
            if (multa.getStatus() == status) {
                multasFiltradas.add(multa);
            }
        }

        return multasFiltradas;
    }

    @Override
    public void atualizar(Multa multa) throws RegistroNaoEncontradoException {

        for (int i = 0; i < multas.size(); i++) {
            if (multas.get(i).getId() == multa.getId()) {
                multas.set(i, multa);
                return;
            }
        }
        throw new RegistroNaoEncontradoException();
    }
}
