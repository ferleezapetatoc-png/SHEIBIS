import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Consultas de los reportes para la interfaz grafica
// (las mismas consultas que usa Reportes en la consola)
public class ReportesDAO {

    // id, nombre, existencia
    public static List<Object[]> pocaExistencia(int limite) throws SQLException {

        String sql = "SELECT id_medicamento, nombre, existencia "
                + "FROM medicamento WHERE existencia <= ? "
                + "ORDER BY existencia, nombre";

        List<Object[]> filas = new ArrayList<>();

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Object[]{rs.getInt(1), rs.getString(2),
                        rs.getInt(3)});
                }
            }
        }
        return filas;
    }

    // id, nombre, existencia, vence, estado
    public static List<Object[]> porVencer(int dias) throws SQLException {

        LocalDate hoy = LocalDate.now();

        String sql = "SELECT id_medicamento, nombre, existencia, "
                + "fecha_vencimiento FROM medicamento "
                + "WHERE fecha_vencimiento IS NOT NULL "
                + "AND fecha_vencimiento <= ? ORDER BY fecha_vencimiento";

        List<Object[]> filas = new ArrayList<>();

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(hoy.plusDays(dias)));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate vence = rs.getDate("fecha_vencimiento").toLocalDate();
                    filas.add(new Object[]{rs.getInt(1), rs.getString(2),
                        rs.getInt(3), vence.toString(),
                        vence.isBefore(hoy) ? "VENCIDO" : "Por vencer"});
                }
            }
        }
        return filas;
    }

    // {cantidad de facturas (Integer), total vendido (BigDecimal)}
    public static Object[] ventasDelDia() throws SQLException {

        String sql = "SELECT COUNT(*) AS facturas, "
                + "COALESCE(SUM(total), 0) AS total "
                + "FROM factura WHERE DATE(fecha) = CURDATE()";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return new Object[]{rs.getInt("facturas"), rs.getBigDecimal("total")};
        }
    }

    // puesto, nombre, unidades
    public static List<Object[]> masVendidos() throws SQLException {

        String sql = "SELECT m.nombre, SUM(d.cantidad) AS vendidas "
                + "FROM detalle_factura d "
                + "JOIN medicamento m ON d.id_medicamento = m.id_medicamento "
                + "GROUP BY m.id_medicamento, m.nombre "
                + "ORDER BY vendidas DESC LIMIT 5";

        List<Object[]> filas = new ArrayList<>();

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int puesto = 0;
            while (rs.next()) {
                puesto++;
                filas.add(new Object[]{puesto, rs.getString(1), rs.getInt(2)});
            }
        }
        return filas;
    }

    // {unidades (Integer), valor de compra, valor de venta}
    public static Object[] valorInventario() throws SQLException {

        String sql = "SELECT "
                + "COALESCE(SUM(precio_venta * existencia), 0) AS venta, "
                + "COALESCE(SUM(precio_compra * existencia), 0) AS compra, "
                + "COALESCE(SUM(existencia), 0) AS unidades "
                + "FROM medicamento";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            BigDecimal venta = rs.getBigDecimal("venta");
            BigDecimal compra = rs.getBigDecimal("compra");
            return new Object[]{rs.getInt("unidades"), compra, venta};
        }
    }
}
