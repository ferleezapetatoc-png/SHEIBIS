import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Acceso a datos de facturas para la interfaz grafica
public class FacturaDAO {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Historial: id, numero, fecha, cliente, atendio, total
    public static List<Object[]> historial() throws SQLException {

        String sql = "SELECT f.id_factura, f.numero_factura, f.fecha, "
                + "f.id_cliente, f.total, "
                + "c.nombre AS cnombre, c.apellido AS capellido, "
                + "e.nombre AS enombre, e.apellido AS eapellido "
                + "FROM factura f "
                + "LEFT JOIN cliente c ON f.id_cliente = c.id_cliente "
                + "JOIN empleado e ON f.id_empleado = e.id_empleado "
                + "ORDER BY f.id_factura DESC";

        List<Object[]> filas = new ArrayList<>();

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rs.getInt("id_cliente");
                boolean sinCliente = rs.wasNull();
                Timestamp ts = rs.getTimestamp("fecha");

                filas.add(new Object[]{
                    rs.getInt("id_factura"),
                    rs.getString("numero_factura"),
                    ts == null ? "-" : ts.toLocalDateTime().format(FORMATO),
                    sinCliente ? "Consumidor final"
                            : rs.getString("cnombre") + " " + rs.getString("capellido"),
                    rs.getString("enombre") + " " + rs.getString("eapellido"),
                    "Q" + rs.getBigDecimal("total").setScale(2)
                });
            }
        }
        return filas;
    }

    // Detalle: medicamento, cantidad, precio, subtotal
    public static List<Object[]> detalle(int idFactura) throws SQLException {

        String sql = "SELECT d.id_detalle, d.id_factura, d.id_medicamento, "
                + "d.cantidad, d.precio_unitario, m.nombre "
                + "FROM detalle_factura d "
                + "JOIN medicamento m ON d.id_medicamento = m.id_medicamento "
                + "WHERE d.id_factura = ? ORDER BY d.id_detalle";

        List<Object[]> filas = new ArrayList<>();

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idFactura);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleFactura d = new DetalleFactura(
                            rs.getInt("id_detalle"),
                            rs.getInt("id_factura"),
                            rs.getInt("id_medicamento"),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario"));

                    filas.add(new Object[]{
                        rs.getString("nombre"),
                        d.getCantidad(),
                        "Q" + d.getPrecioUnitario().setScale(2),
                        "Q" + d.calcularSubtotal().setScale(2)
                    });
                }
            }
        }
        return filas;
    }

    // Guarda la venta con la misma transaccion que usa la consola
    public static String guardar(Integer idCliente,
                                 List<DetalleFactura> detalles,
                                 BigDecimal total) throws SQLException {

        try (Connection conexion = Conexion.conectar()) {
            return FacturaCRUD.guardar(conexion, idCliente,
                    Sesion.idEmpleado, detalles, total);
        }
    }
}
