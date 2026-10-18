import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

public class Reportes {

    // SUBMENU DE REPORTES
    public static void menu(Scanner teclado) {

        int opcion = 0;

        while (opcion != 6) {

            System.out.println("\n--- REPORTES ---");
            System.out.println("1. Medicamentos con poca existencia");
            System.out.println("2. Medicamentos vencidos o por vencer");
            System.out.println("3. Ventas del dia (solo administrador)");
            System.out.println("4. Medicamentos mas vendidos (solo administrador)");
            System.out.println("5. Valor del inventario (solo administrador)");
            System.out.println("6. Volver");
            System.out.print("Seleccione una opcion: ");

            try {
                opcion = Integer.parseInt(teclado.nextLine());

                switch (opcion) {
                    case 1:
                        pocaExistencia(teclado);
                        break;
                    case 2:
                        porVencer(teclado);
                        break;
                    case 3:
                        if (permitido()) {
                            ventasDelDia();
                        }
                        break;
                    case 4:
                        if (permitido()) {
                            masVendidos();
                        }
                        break;
                    case 5:
                        if (permitido()) {
                            valorInventario();
                        }
                        break;
                    case 6:
                        break;
                    default:
                        System.out.println("Opcion incorrecta.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un numero.");
            }
        }
    }

    // LOS REPORTES DE DINERO SOLO LOS VE UN ADMINISTRADOR
    private static boolean permitido() {

        if (Sesion.esAdministrador()) {
            return true;
        }

        System.out.println("Acceso denegado: solo administradores.");
        return false;
    }

    // MEDICAMENTOS CON EXISTENCIA IGUAL O MENOR AL LIMITE
    public static void pocaExistencia(Scanner teclado) {

        Integer limite = leerNumero(teclado,
                "Mostrar medicamentos con existencia menor o igual a "
                + "(vacio = 10): ", 10);

        if (limite == null) {
            return;
        }

        String sql = "SELECT id_medicamento, nombre, existencia "
                + "FROM medicamento WHERE existencia <= ? "
                + "ORDER BY existencia, nombre";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, limite);

            try (ResultSet rs = ps.executeQuery()) {

                System.out.println("\n--- POCA EXISTENCIA (<= " + limite + ") ---");

                boolean hay = false;

                while (rs.next()) {
                    hay = true;
                    System.out.println("ID " + rs.getInt("id_medicamento")
                            + " | " + rs.getString("nombre")
                            + " | existencia: " + rs.getInt("existencia"));
                }

                if (!hay) {
                    System.out.println("Ningun medicamento con poca existencia.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error en el reporte: " + e.getMessage());
        }
    }

    // MEDICAMENTOS VENCIDOS O QUE VENCEN EN LOS PROXIMOS DIAS
    public static void porVencer(Scanner teclado) {

        Integer dias = leerNumero(teclado,
                "Mostrar los que vencen en los proximos N dias "
                + "(vacio = 30): ", 30);

        if (dias == null) {
            return;
        }

        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(dias);

        String sql = "SELECT id_medicamento, nombre, existencia, "
                + "fecha_vencimiento FROM medicamento "
                + "WHERE fecha_vencimiento IS NOT NULL "
                + "AND fecha_vencimiento <= ? "
                + "ORDER BY fecha_vencimiento";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(limite));

            try (ResultSet rs = ps.executeQuery()) {

                System.out.println("\n--- VENCIDOS O QUE VENCEN EN " + dias
                        + " DIAS ---");

                boolean hay = false;

                while (rs.next()) {

                    hay = true;

                    LocalDate vence = rs.getDate("fecha_vencimiento").toLocalDate();
                    String estado = vence.isBefore(hoy) ? "VENCIDO" : "por vencer";

                    System.out.println("ID " + rs.getInt("id_medicamento")
                            + " | " + rs.getString("nombre")
                            + " | existencia: " + rs.getInt("existencia")
                            + " | vence: " + vence
                            + " | " + estado);
                }

                if (!hay) {
                    System.out.println("No hay medicamentos vencidos ni por vencer.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error en el reporte: " + e.getMessage());
        }
    }

    // CANTIDAD DE FACTURAS Y TOTAL VENDIDO HOY
    public static void ventasDelDia() {

        String sql = "SELECT COUNT(*) AS facturas, "
                + "COALESCE(SUM(total), 0) AS total "
                + "FROM factura WHERE DATE(fecha) = CURDATE()";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();

            System.out.println("\n--- VENTAS DE HOY (" + LocalDate.now() + ") ---");
            System.out.println("Facturas emitidas: " + rs.getInt("facturas"));
            System.out.println("Total vendido: Q" + rs.getBigDecimal("total"));

        } catch (SQLException e) {
            System.out.println("Error en el reporte: " + e.getMessage());
        }
    }

    // LOS 5 MEDICAMENTOS CON MAS UNIDADES VENDIDAS
    public static void masVendidos() {

        String sql = "SELECT m.nombre, SUM(d.cantidad) AS vendidas "
                + "FROM detalle_factura d "
                + "JOIN medicamento m ON d.id_medicamento = m.id_medicamento "
                + "GROUP BY m.id_medicamento, m.nombre "
                + "ORDER BY vendidas DESC LIMIT 5";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- MEDICAMENTOS MAS VENDIDOS ---");

            int puesto = 0;

            while (rs.next()) {
                puesto++;
                System.out.println(puesto + ". " + rs.getString("nombre")
                        + " - " + rs.getInt("vendidas") + " unidades");
            }

            if (puesto == 0) {
                System.out.println("Aun no hay ventas registradas.");
            }

        } catch (SQLException e) {
            System.out.println("Error en el reporte: " + e.getMessage());
        }
    }

    // VALOR TOTAL DEL INVENTARIO A PRECIO DE VENTA Y A PRECIO DE COMPRA
    public static void valorInventario() {

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

            System.out.println("\n--- VALOR DEL INVENTARIO ---");
            System.out.println("Unidades en existencia: " + rs.getInt("unidades"));
            System.out.println("Valor a precio de compra: Q" + compra);
            System.out.println("Valor a precio de venta: Q" + venta);
            System.out.println("Ganancia estimada: Q" + venta.subtract(compra));

        } catch (SQLException e) {
            System.out.println("Error en el reporte: " + e.getMessage());
        }
    }

    // LEE UN NUMERO POSITIVO. Si queda vacio usa el valor por defecto.
    // Devuelve null si lo escrito no es valido.
    private static Integer leerNumero(Scanner teclado, String mensaje,
                                      int defecto) {

        System.out.print(mensaje);
        String texto = teclado.nextLine().trim();

        if (texto.isEmpty()) {
            return defecto;
        }

        try {
            int numero = Integer.parseInt(texto);

            if (numero < 0) {
                System.out.println("El numero no puede ser negativo.");
                return null;
            }

            return numero;

        } catch (NumberFormatException e) {
            System.out.println("Numero invalido.");
            return null;
        }
    }
}