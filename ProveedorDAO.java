import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Acceso a datos de proveedores para la interfaz grafica
public class ProveedorDAO {

    public static List<Proveedor> listar() throws SQLException {

        String sql = "SELECT * FROM proveedor ORDER BY nombre";
        List<Proveedor> lista = new ArrayList<>();

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Proveedor(
                        rs.getInt("id_proveedor"),
                        rs.getString("nit"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getString("direccion"),
                        rs.getString("correo")));
            }
        }
        return lista;
    }

    public static void insertar(Proveedor p)
            throws SQLException, ValidacionException {

        validar(p);

        String sql = "INSERT INTO proveedor "
                + "(nit, nombre, telefono, direccion, correo) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.conectar()) {

            verificarNit(conexion, p.getNit(), 0);

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                llenar(ps, p);
                ps.executeUpdate();
            }
        }
    }

    public static void actualizar(Proveedor p)
            throws SQLException, ValidacionException {

        validar(p);

        String sql = "UPDATE proveedor SET nit = ?, nombre = ?, telefono = ?, "
                + "direccion = ?, correo = ? WHERE id_proveedor = ?";

        try (Connection conexion = Conexion.conectar()) {

            verificarNit(conexion, p.getNit(), p.getIdProveedor());

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                llenar(ps, p);
                ps.setInt(6, p.getIdProveedor());

                if (ps.executeUpdate() == 0) {
                    throw new ValidacionException("No se encontro ese proveedor.");
                }
            }
        }
    }

    public static void eliminar(int id)
            throws SQLException, ValidacionException {

        String sql = "DELETE FROM proveedor WHERE id_proveedor = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            if (ps.executeUpdate() == 0) {
                throw new ValidacionException("No se encontro ese proveedor.");
            }
        }
    }

    private static void llenar(PreparedStatement ps, Proveedor p)
            throws SQLException {
        ps.setString(1, p.getNit());
        ps.setString(2, p.getNombre());
        ps.setString(3, p.getTelefono());
        ps.setString(4, p.getDireccion());
        ps.setString(5, p.getCorreo());
    }

    // Reglas de validacion (lanzan la excepcion personalizada)
    private static void validar(Proveedor p) throws ValidacionException {

        if (p.getNit() == null || p.getNit().trim().isEmpty()) {
            throw new ValidacionException("El NIT es obligatorio.");
        }
        if (p.getNombre() == null || p.getNombre().trim().isEmpty()) {
            throw new ValidacionException("El nombre es obligatorio.");
        }
        // Usa el metodo de negocio de la clase Proveedor
        if (p.tieneCorreo() && !p.correoValido()) {
            throw new ValidacionException(
                    "El correo no es valido. Ejemplo: ventas@empresa.com");
        }
    }

    private static void verificarNit(Connection conexion, String nit,
                                     int idActual)
            throws SQLException, ValidacionException {

        String sql = "SELECT id_proveedor FROM proveedor "
                + "WHERE nit = ? AND id_proveedor <> ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nit);
            ps.setInt(2, idActual);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new ValidacionException(
                            "Ese NIT ya pertenece a otro proveedor.");
                }
            }
        }
    }
}
