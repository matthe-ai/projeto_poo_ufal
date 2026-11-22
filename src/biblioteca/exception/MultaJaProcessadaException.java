package biblioteca.exception;

public class MultaJaProcessadaException extends RuntimeException {
    public MultaJaProcessadaException() {
        super("Essa multa já foi processada.");
    }
}
