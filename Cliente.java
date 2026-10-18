public class Cliente extends Persona {

    private int idCliente;

    public Cliente(int idCliente, String nit, String nombre,
                   String apellido, String telefono,
                   String direccion) {
        super(nit, nombre, apellido, telefono, direccion);
        this.idCliente = idCliente;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String obtenerNombreCompleto() {
        return getNombre() + " " + getApellido();
    }

    // Indica si el cliente tiene un NIT valido (no vacio y distinto de CF)
    public boolean tieneNit() {
        String nit = getNit();
        return nit != null && !nit.trim().isEmpty()
                && !nit.trim().equalsIgnoreCase("CF");
    }

    @Override
    public void mostrarInformacion() {
        System.out.println("ID del cliente: " + idCliente);
        System.out.println("Nombre: " + getNombre()
                + " " + getApellido());
        System.out.println("NIT: " + getNit());
        System.out.println("Teléfono: " + getTelefono());
        System.out.println("Dirección: " + getDireccion());
    }
}