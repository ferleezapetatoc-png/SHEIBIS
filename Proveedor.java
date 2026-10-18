public class Proveedor {

    private int idProveedor;
    private String nit;
    private String nombre;
    private String telefono;
    private String direccion;
    private String correo;

    public Proveedor(int idProveedor, String nit, String nombre,
                     String telefono, String direccion,
                     String correo) {
        this.idProveedor = idProveedor;
        this.nit = nit;
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.correo = correo;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public boolean tieneCorreo() {
        return correo != null && !correo.trim().isEmpty();
    }

    // Validacion basica del formato del correo
    public boolean correoValido() {
        return tieneCorreo()
                && correo.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    }

    // Texto corto con los datos de contacto del proveedor
    public String obtenerContacto() {
        String contacto = nombre + " - Tel: " + telefono;
        if (tieneCorreo()) {
            contacto += " - " + correo;
        }
        return contacto;
    }

    public void mostrarInformacion() {
        System.out.println("Proveedor: " + nombre);
        System.out.println("NIT: " + nit);
        System.out.println("Teléfono: " + telefono);
        System.out.println("Correo: " + correo);
    }
}