import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Acceso a datos de medicamentos para la interfaz grafica
public class MedicamentoDAO {

    public static List<Medicamento> listar() throws SQLException {

        String sql = "SELECT * FROM medicamento ORDER BY nombre";
        List<Medicamento> lista = new ArrayList<>();

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Date fecha = rs.getDate("fecha_vencimiento");
                int prov = rs.getInt("id_proveedor");
                Integer idProveedor = rs.wasNull() ? null : prov;

                lista.add(new Medicamento(
                        rs.getInt("id_medicamento"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getBigDecimal("precio_compra"),
                        rs.getBigDecimal("precio_venta"),
                        rs.getInt("existencia"),
                        fecha == null ? null : fecha.toLocalDate(),
                        idProveedor));
            }
        }
        return lista;
    }

    // id -> nombre de los proveedores (para la lista desplegable)
    public static Map<Integer, String> proveedores() throws SQLException {

        String sql = "SELECT id_proveedor, nombre FROM proveedor ORDER BY nombre";
        Map<Integer, String> mapa = new LinkedHashMap<>();

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                mapa.put(rs.getInt(1), rs.getString(2));
            }
        }
        return mapa;
    }

    public static void insertar(Medicamento m)
            throws SQLException, ValidacionException {

        validar(m);

        String sql = "INSERT INTO medicamento (nombre, descripcion, "
                + "precio_compra, precio_venta, existencia, "
                + "fecha_vencimiento, id_proveedor) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            llenar(ps, m);
            ps.executeUpdate();
        }
    }

    public static void actualizar(Medicamento m)
            throws SQLException, ValidacionException {

        validar(m);

        String sql = "UPDATE medicamento SET nombre = ?, descripcion = ?, "
                + "precio_compra = ?, precio_venta = ?, existencia = ?, "
                + "fecha_vencimiento = ?, id_proveedor = ? "
                + "WHERE id_medicamento = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            llenar(ps, m);
            ps.setInt(8, m.getIdMedicamento());

            if (ps.executeUpdate() == 0) {
                throw new ValidacionException("No se encontro ese medicamento.");
            }
        }
    }

    public static void eliminar(int id)
            throws SQLException, ValidacionException {

        String sql = "DELETE FROM medicamento WHERE id_medicamento = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            if (ps.executeUpdate() == 0) {
                throw new ValidacionException("No se encontro ese medicamento.");
            }
        }
    }

    private static void llenar(PreparedStatement ps, Medicamento m)
            throws SQLException {

        ps.setString(1, m.getNombre());
        ps.setString(2, m.getDescripcion());
        ps.setBigDecimal(3, m.getPrecioCompra());
        ps.setBigDecimal(4, m.getPrecioVenta());
        ps.setInt(5, m.getExistencia());

        if (m.getFechaVencimiento() == null) {
            ps.setNull(6, Types.DATE);
        } else {
            ps.setDate(6, Date.valueOf(m.getFechaVencimiento()));
        }

        if (m.getIdProveedor() == null) {
            ps.setNull(7, Types.INTEGER);
        } else {
            ps.setInt(7, m.getIdProveedor());
        }
    }

    // Reglas de validacion (lanzan la excepcion personalizada)
    private static void validar(Medicamento m) throws ValidacionException {

        if (m.getNombre() == null || m.getNombre().trim().isEmpty()) {
            throw new ValidacionException("El nombre es obligatorio.");
        }
        if (m.getPrecioCompra() == null
                || m.getPrecioCompra().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionException(
                    "El precio de compra debe ser un numero mayor o igual a 0.");
        }
        if (m.getPrecioVenta() == null
                || m.getPrecioVenta().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionException(
                    "El precio de venta debe ser un numero mayor o igual a 0.");
        }
        if (m.getExistencia() < 0) {
            throw new ValidacionException(
                    "La existencia no puede ser negativa.");
        }
    }
}
