import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Acceso a datos de empleados para la interfaz grafica
public class EmpleadoDAO {

    // Un empleado junto con su rol (la contrasena nunca se carga)
    public static class Fila {
        public final Empleado empleado;
        public final String rol;

        public Fila(Empleado empleado, String rol) {
            this.empleado = empleado;
            this.rol = rol;
        }
    }

    public static List<Fila> listar() throws SQLException {

        String sql = "SELECT id_empleado, nit, nombre, apellido, telefono, "
                + "direccion, puesto, rol, usuario FROM empleado "
                + "ORDER BY id_empleado";
        List<Fila> lista = new ArrayList<>();

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Empleado e = new Empleado(
                        rs.getInt("id_empleado"),
                        rs.getString("nit"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("telefono"),
                        rs.getString("direccion"),
                        rs.getString("puesto"),
                        rs.getString("usuario"),
                        "");
                lista.add(new Fila(e, rs.getString("rol")));
            }
        }
        return lista;
    }

    // clave: contrasena en texto (se cifra al guardar)
    public static void insertar(Empleado e, String rol, String clave)
            throws SQLException, ValidacionException {

        validar(e, rol, clave, true);

        String sql = "INSERT INTO empleado (nit, nombre, apellido, telefono, "
                + "direccion, puesto, rol, usuario, contrasena) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.conectar()) {

            verificarUsuario(conexion, e.getUsuario(), 0);

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                llenar(ps, e, rol);
                ps.setString(9, EmpleadoCRUD.cifrar(clave));
                ps.executeUpdate();
            }
        }
    }

    // Si clave viene vacia, no se cambia la contrasena
    public static void actualizar(Empleado e, String rol, String clave)
            throws SQLException, ValidacionException {

        validar(e, rol, clave, false);

        boolean cambiaClave = !clave.isEmpty();

        String sql = "UPDATE empleado SET nit = ?, nombre = ?, apellido = ?, "
                + "telefono = ?, direccion = ?, puesto = ?, rol = ?, "
                + "usuario = ?" + (cambiaClave ? ", contrasena = ?" : "")
                + " WHERE id_empleado = ?";

        try (Connection conexion = Conexion.conectar()) {

            verificarUsuario(conexion, e.getUsuario(), e.getIdEmpleado());

            // No se puede dejar el sistema sin administradores
            if (!rol.equals("ADMINISTRADOR")
                    && esUnicoAdministrador(conexion, e.getIdEmpleado())) {
                throw new ValidacionException(
                        "No se puede quitar el rol al unico administrador.");
            }

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                llenar(ps, e, rol);

                if (cambiaClave) {
                    ps.setString(9, EmpleadoCRUD.cifrar(clave));
                    ps.setInt(10, e.getIdEmpleado());
                } else {
                    ps.setInt(9, e.getIdEmpleado());
                }

                if (ps.executeUpdate() == 0) {
                    throw new ValidacionException("No se encontro ese empleado.");
                }
            }
        }
    }

    public static void eliminar(int id)
            throws SQLException, ValidacionException {

        if (id == Sesion.idEmpleado) {
            throw new ValidacionException(
                    "No puede eliminar el usuario con el que inicio sesion.");
        }

        String sql = "DELETE FROM empleado WHERE id_empleado = ?";

        try (Connection conexion = Conexion.conectar()) {

            if (esUnicoAdministrador(conexion, id)) {
                throw new ValidacionException(
                        "No se puede eliminar al unico administrador.");
            }

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setInt(1, id);

                if (ps.executeUpdate() == 0) {
                    throw new ValidacionException("No se encontro ese empleado.");
                }
            }
        }
    }

    private static void llenar(PreparedStatement ps, Empleado e, String rol)
            throws SQLException {
        ps.setString(1, e.getNit());
        ps.setString(2, e.getNombre());
        ps.setString(3, e.getApellido());
        ps.setString(4, e.getTelefono());
        ps.setString(5, e.getDireccion());
        ps.setString(6, e.getPuesto());
        ps.setString(7, rol);
        ps.setString(8, e.getUsuario());
    }

    // Reglas de validacion (lanzan la excepcion personalizada)
    private static void validar(Empleado e, String rol, String clave,
                                boolean claveObligatoria)
            throws ValidacionException {

        if (e.getNombre().trim().isEmpty() || e.getApellido().trim().isEmpty()
                || e.getUsuario().trim().isEmpty()) {
            throw new ValidacionException(
                    "Nombre, apellido y usuario son obligatorios.");
        }
        if (!rol.equals("ADMINISTRADOR") && !rol.equals("EMPLEADO")) {
            throw new ValidacionException("El rol no es valido.");
        }
        if (claveObligatoria && clave.isEmpty()) {
            throw new ValidacionException("La contrasena es obligatoria.");
        }
        if (!clave.isEmpty() && clave.length() < 4) {
            throw new ValidacionException(
                    "La contrasena debe tener al menos 4 caracteres.");
        }
    }

    private static void verificarUsuario(Connection conexion, String usuario,
                                         int idActual)
            throws SQLException, ValidacionException {

        String sql = "SELECT id_empleado FROM empleado "
                + "WHERE usuario = ? AND id_empleado <> ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setInt(2, idActual);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new ValidacionException(
                            "Ese usuario pertenece a otro empleado.");
                }
            }
        }
    }

    // true si el empleado es administrador y no hay otro mas
    private static boolean esUnicoAdministrador(Connection conexion, int id)
            throws SQLException {

        String sql = "SELECT "
                + "SUM(rol = 'ADMINISTRADOR') AS admins, "
                + "SUM(rol = 'ADMINISTRADOR' AND id_empleado = ?) AS este "
                + "FROM empleado";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("este") > 0 && rs.getInt("admins") <= 1;
            }
        }
    }
}
