package biblioteca.exception;

public class MultaJaProcessada extends RuntimeException {
    public MultaJaProcessada() {
        super("Essa multa já foi processada.");
    }
}
