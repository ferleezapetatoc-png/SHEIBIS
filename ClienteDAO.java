import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Acceso a datos de clientes para la interfaz grafica
public class ClienteDAO {

    public static List<Cliente> listar() throws SQLException {

        String sql = "SELECT * FROM cliente ORDER BY id_cliente";
        List<Cliente> lista = new ArrayList<>();

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Cliente(
                        rs.getInt("id_cliente"),
                        rs.getString("nit"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("telefono"),
                        rs.getString("direccion")));
            }
        }
        return lista;
    }

    public static void insertar(Cliente c)
            throws SQLException, ValidacionException {

        validar(c);

        String sql = "INSERT INTO cliente "
                + "(nit, nombre, apellido, telefono, direccion) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.conectar()) {

            verificarNit(conexion, c.getNit(), 0);

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, c.getNit());
                ps.setString(2, c.getNombre());
                ps.setString(3, c.getApellido());
                ps.setString(4, c.getTelefono());
                ps.setString(5, c.getDireccion());
                ps.executeUpdate();
            }
        }
    }

    public static void actualizar(Cliente c)
            throws SQLException, ValidacionException {

        validar(c);

        String sql = "UPDATE cliente SET nit = ?, nombre = ?, apellido = ?, "
                + "telefono = ?, direccion = ? WHERE id_cliente = ?";

        try (Connection conexion = Conexion.conectar()) {

            verificarNit(conexion, c.getNit(), c.getIdCliente());

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, c.getNit());
                ps.setString(2, c.getNombre());
                ps.setString(3, c.getApellido());
                ps.setString(4, c.getTelefono());
                ps.setString(5, c.getDireccion());
                ps.setInt(6, c.getIdCliente());

                if (ps.executeUpdate() == 0) {
                    throw new ValidacionException(
                            "No se encontro ese cliente.");
                }
            }
        }
    }

    public static void eliminar(int id)
            throws SQLException, ValidacionException {

        String sql = "DELETE FROM cliente WHERE id_cliente = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            if (ps.executeUpdate() == 0) {
                throw new ValidacionException("No se encontro ese cliente.");
            }
        }
    }

    // Reglas de validacion (lanzan la excepcion personalizada)
    private static void validar(Cliente c) throws ValidacionException {

        if (c.getNit() == null || c.getNit().trim().isEmpty()) {
            throw new ValidacionException("El NIT es obligatorio.");
        }
        if (c.getNombre() == null || c.getNombre().trim().isEmpty()) {
            throw new ValidacionException("El nombre es obligatorio.");
        }
        if (c.getApellido() == null || c.getApellido().trim().isEmpty()) {
            throw new ValidacionException("El apellido es obligatorio.");
        }
        if (c.getTelefono() != null && !c.getTelefono().trim().isEmpty()
                && !c.getTelefono().trim().matches("[0-9+\\- ]{6,20}")) {
            throw new ValidacionException(
                    "El telefono solo puede tener numeros (6 a 20 digitos).");
        }
    }

    private static void verificarNit(Connection conexion, String nit,
                                     int idActual)
            throws SQLException, ValidacionException {

        String sql = "SELECT id_cliente FROM cliente "
                + "WHERE nit = ? AND id_cliente <> ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nit);
            ps.setInt(2, idActual);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new ValidacionException(
                            "Ese NIT ya pertenece a otro cliente.");
                }
            }
        }
    }
}
