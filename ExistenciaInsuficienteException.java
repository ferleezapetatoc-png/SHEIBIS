public class ExistenciaInsuficienteException extends Exception {

    private static final long serialVersionUID = 1L;

    private final int disponible;

    public ExistenciaInsuficienteException(String mensaje, int disponible) {
        super(mensaje);
        this.disponible = disponible;
    }

    public int getDisponible() {
        return disponible;
    }
}