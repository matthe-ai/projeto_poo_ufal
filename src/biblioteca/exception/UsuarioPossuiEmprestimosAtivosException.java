package biblioteca.exception;

public class UsuarioPossuiEmprestimosAtivosException extends Exception {

    public UsuarioPossuiEmprestimosAtivosException() {
        super("Não é possível remover um usuário que possui empréstimos ativos.");
    }
}
