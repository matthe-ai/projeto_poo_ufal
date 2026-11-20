package biblioteca.exception;

public class MaterialDevolvidoException extends Exception {

    public MaterialDevolvidoException() {
        super("Não é possível devolver um material novamente para o mesmo empréstimo.");
    }
}
