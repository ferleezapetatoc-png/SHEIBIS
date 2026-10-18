public class Empleado extends Persona {

    private int idEmpleado;
    private String puesto;
    private String usuario;
    private String contrasena;

    public Empleado(int idEmpleado, String nit, String nombre,
                    String apellido, String telefono,
                    String direccion, String puesto,
                    String usuario, String contrasena) {
        super(nit, nombre, apellido, telefono, direccion);
        this.idEmpleado = idEmpleado;
        this.puesto = puesto;
        this.usuario = usuario;
        this.contrasena = contrasena;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    @Override
    public void mostrarInformacion() {
        System.out.println("ID del empleado: " + idEmpleado);
        System.out.println("Nombre: " + getNombre()
                + " " + getApellido());
        System.out.println("Puesto: " + puesto);
        System.out.println("Usuario: " + usuario);
    }
}