package biblioteca.repository;

import biblioteca.exception.RegistroNaoEncontradoException;

public interface RepositorioRemovivel {

    void remover(int id) throws RegistroNaoEncontradoException;;
}
