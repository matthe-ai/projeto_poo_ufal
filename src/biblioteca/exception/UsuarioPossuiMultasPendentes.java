package biblioteca.exception;

public class UsuarioPossuiMultasPendentes extends RuntimeException {
    public UsuarioPossuiMultasPendentes() {
        super("Usuários com multas pendentes não podem realizar empréstimos.");
    }
}
