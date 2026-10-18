import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FacturaCRUD {

    // SUBMENU DE FACTURAS
    public static void menu(Scanner teclado) {

        int opcion = 0;

        while (opcion != 5) {

            System.out.println("\n--- FACTURAS ---");
            System.out.println("1. Nueva factura (venta)");
            System.out.println("2. Mostrar facturas");
            System.out.println("3. Ver detalle de una factura");
            System.out.println("4. Anular factura");
            System.out.println("5. Volver");
            System.out.print("Seleccione una opcion: ");

            try {
                opcion = Integer.parseInt(teclado.nextLine());

                switch (opcion) {
                    case 1:
                        crear(teclado);
                        break;
                    case 2:
                        mostrar();
                        break;
                    case 3:
                        verDetalle(teclado);
                        break;
                    case 4:
                        anular(teclado);
                        break;
                    case 5:
                        break;
                    default:
                        System.out.println("Opcion incorrecta.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un numero.");
            }
        }
    }

    // CREATE: NUEVA FACTURA
    public static void crear(Scanner teclado) {

        System.out.println("\n--- NUEVA FACTURA ---");

        try (Connection conexion = Conexion.conectar()) {

            // EMPLEADO QUE ATIENDE: el que inicio sesion
            int idEmpleado = Sesion.idEmpleado;
            String nombreEmpleado = Sesion.nombre;

            // CLIENTE (OPCIONAL)
            System.out.print("ID del cliente (vacio = consumidor final): ");
            String textoCliente = teclado.nextLine().trim();

            Integer idCliente = null;
            String nombreCliente = "Consumidor final";

            if (!textoCliente.isEmpty()) {

                idCliente = leerEntero(textoCliente);

                if (idCliente == null) {
                    System.out.println("ID incorrecto.");
                    return;
                }

                nombreCliente = nombrePersona(
                        conexion, "cliente", "id_cliente", idCliente);

                if (nombreCliente == null) {
                    System.out.println("No existe ese cliente.");
                    return;
                }
            }

            // PRODUCTOS
            List<DetalleFactura> detalles = new ArrayList<>();
            List<String> nombres = new ArrayList<>();

            while (true) {

                System.out.print("\nID del medicamento (vacio para terminar): ");
                String texto = teclado.nextLine().trim();

                if (texto.isEmpty()) {
                    break;
                }

                Integer idMed = leerEntero(texto);

                if (idMed == null) {
                    System.out.println("ID incorrecto.");
                    continue;
                }

                Medicamento m = buscarMedicamento(conexion, idMed);

                if (m == null) {
                    System.out.println("No existe ese medicamento.");
                    continue;
                }

                if (m.getFechaVencimiento() != null
                        && m.getFechaVencimiento().isBefore(LocalDate.now())) {
                    System.out.println("Ese medicamento esta vencido.");
                    continue;
                }

                // SI YA ESTA EN LA FACTURA, SE SUMA LA CANTIDAD
                int yaAgregado = 0;
                int posicion = -1;

                for (int i = 0; i < detalles.size(); i++) {
                    if (detalles.get(i).getIdMedicamento() == idMed.intValue()) {
                        yaAgregado = detalles.get(i).getCantidad();
                        posicion = i;
                    }
                }

                System.out.println(m.getNombre() + " - Q" + m.getPrecioVenta()
                        + " - disponible: " + (m.getExistencia() - yaAgregado));

                System.out.print("Cantidad: ");
                Integer cantidad = leerEntero(teclado.nextLine());

                if (cantidad == null || cantidad <= 0) {
                    System.out.println("Cantidad invalida.");
                    continue;
                }

                try {
                    m.verificarExistencia(yaAgregado + cantidad);
                } catch (ExistenciaInsuficienteException e) {
                    System.out.println(e.getMessage());
                    continue;
                }
                if (posicion >= 0) {
                    detalles.get(posicion).setCantidad(yaAgregado + cantidad);
                } else {
                    detalles.add(new DetalleFactura(0, 0, idMed, cantidad,
                            m.getPrecioVenta()));
                    nombres.add(m.getNombre());
                }

                System.out.println("Producto agregado.");
            }

            if (detalles.isEmpty()) {
                System.out.println("La factura no tiene productos. Cancelada.");
                return;
            }

            // RESUMEN
            BigDecimal total = BigDecimal.ZERO;

            System.out.println("\n===== RESUMEN DE LA FACTURA =====");
            System.out.println("Cliente: " + nombreCliente);
            System.out.println("Atiende: " + nombreEmpleado);
            System.out.println("-------------------------------");

            for (int i = 0; i < detalles.size(); i++) {
                DetalleFactura d = detalles.get(i);
                System.out.println(nombres.get(i) + "  x" + d.getCantidad()
                        + "  = Q" + d.calcularSubtotal());
                total = total.add(d.calcularSubtotal());
            }

            System.out.println("-------------------------------");
            System.out.println("TOTAL: Q" + total);

            System.out.print("\n¿Confirmar la venta? Escriba S para confirmar: ");

            if (!teclado.nextLine().equalsIgnoreCase("S")) {
                System.out.println("Venta cancelada. No se guardo nada.");
                return;
            }

            String numero = guardar(conexion, idCliente, idEmpleado,
                    detalles, total);

            System.out.println("Factura " + numero + " guardada correctamente.");

        } catch (SQLException e) {
            System.out.println("No se pudo guardar la factura: " + e.getMessage());
            System.out.println("No se hizo ningun cambio en la base de datos.");
        }
    }

    // READ: LISTA DE FACTURAS
    public static void mostrar() {

        String sql = "SELECT f.id_factura, f.numero_factura, f.fecha, "
                + "f.id_cliente, f.id_empleado, f.total, "
                + "c.nombre AS cnombre, c.apellido AS capellido, "
                + "e.nombre AS enombre, e.apellido AS eapellido "
                + "FROM factura f "
                + "LEFT JOIN cliente c ON f.id_cliente = c.id_cliente "
                + "JOIN empleado e ON f.id_empleado = e.id_empleado "
                + "ORDER BY f.id_factura DESC";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet rs = sentencia.executeQuery()) {

            System.out.println("\n--- LISTA DE FACTURAS ---");

            boolean hay = false;

            while (rs.next()) {

                hay = true;

                int cli = rs.getInt("id_cliente");
                Integer idCliente = rs.wasNull() ? null : cli;

                Timestamp ts = rs.getTimestamp("fecha");

                Factura f = new Factura(
                        rs.getInt("id_factura"),
                        rs.getString("numero_factura"),
                        ts == null ? null : ts.toLocalDateTime(),
                        idCliente,
                        rs.getInt("id_empleado"),
                        rs.getBigDecimal("total")
                );

                String cliente = idCliente == null
                        ? "Consumidor final"
                        : rs.getString("cnombre") + " " + rs.getString("capellido");

                System.out.println("ID: " + f.getIdFactura());
                f.mostrarInformacion();
                System.out.println("Cliente: " + cliente);
                System.out.println("Atendio: " + rs.getString("enombre")
                        + " " + rs.getString("eapellido"));
                System.out.println("------------------------");
            }

            if (!hay) {
                System.out.println("No hay facturas registradas.");
            }

        } catch (SQLException e) {
            System.out.println("Error al mostrar: " + e.getMessage());
        }
    }

    // READ: DETALLE DE UNA FACTURA
    public static void verDetalle(Scanner teclado) {

        System.out.println("\n--- DETALLE DE FACTURA ---");
        System.out.print("ID de la factura: ");

        Integer id = leerEntero(teclado.nextLine());

        if (id == null) {
            System.out.println("ID incorrecto.");
            return;
        }

        String sqlFactura = "SELECT numero_factura, fecha, total "
                + "FROM factura WHERE id_factura = ?";

        String sqlDetalle = "SELECT d.id_detalle, d.id_factura, "
                + "d.id_medicamento, d.cantidad, d.precio_unitario, "
                + "m.nombre "
                + "FROM detalle_factura d "
                + "JOIN medicamento m ON d.id_medicamento = m.id_medicamento "
                + "WHERE d.id_factura = ? ORDER BY d.id_detalle";

        try (Connection conexion = Conexion.conectar()) {

            try (PreparedStatement ps = conexion.prepareStatement(sqlFactura)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        System.out.println("No se encontro esa factura.");
                        return;
                    }

                    System.out.println("\nFactura: " + rs.getString("numero_factura"));
                    System.out.println("Fecha: " + rs.getTimestamp("fecha"));
                }
            }

            BigDecimal total = BigDecimal.ZERO;

            try (PreparedStatement ps = conexion.prepareStatement(sqlDetalle)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {

                    System.out.println("-------------------------------");

                    while (rs.next()) {

                        DetalleFactura d = new DetalleFactura(
                                rs.getInt("id_detalle"),
                                rs.getInt("id_factura"),
                                rs.getInt("id_medicamento"),
                                rs.getInt("cantidad"),
                                rs.getBigDecimal("precio_unitario")
                        );

                        System.out.println(rs.getString("nombre")
                                + "  x" + d.getCantidad()
                                + "  a Q" + d.getPrecioUnitario()
                                + "  = Q" + d.calcularSubtotal());

                        total = total.add(d.calcularSubtotal());
                    }
                }
            }

            System.out.println("-------------------------------");
            System.out.println("TOTAL: Q" + total);

        } catch (SQLException e) {
            System.out.println("Error al mostrar el detalle: " + e.getMessage());
        }
    }

    // DELETE: ANULAR FACTURA (devuelve la existencia a los medicamentos)
    public static void anular(Scanner teclado) {

        System.out.println("\n--- ANULAR FACTURA ---");

        if (!Sesion.esAdministrador()) {
            System.out.println("Acceso denegado: solo un administrador "
                    + "puede anular facturas.");
            return;
        }

        System.out.print("ID de la factura: ");

        Integer id = leerEntero(teclado.nextLine());

        if (id == null) {
            System.out.println("ID incorrecto.");
            return;
        }

        System.out.print("¿Desea anularla? Se devolvera la existencia. "
                + "Escriba S para confirmar: ");

        if (!teclado.nextLine().equalsIgnoreCase("S")) {
            System.out.println("Operacion cancelada.");
            return;
        }

        try (Connection conexion = Conexion.conectar()) {

            if (anularFactura(conexion, id)) {
                System.out.println("Factura anulada y existencias devueltas.");
            } else {
                System.out.println("No se encontro esa factura.");
            }

        } catch (SQLException e) {
            System.out.println("No se pudo anular: " + e.getMessage());
            System.out.println("No se hizo ningun cambio en la base de datos.");
        }
    }

    // GUARDA FACTURA + DETALLES + DESCUENTA EXISTENCIA, TODO EN UNA TRANSACCION
    public static String guardar(Connection conexion, Integer idCliente,
                                  int idEmpleado, List<DetalleFactura> detalles,
                                  BigDecimal total) throws SQLException {

        String numero = "FAC-" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        String sqlFactura = "INSERT INTO factura "
                + "(numero_factura, id_cliente, id_empleado, total) "
                + "VALUES (?, ?, ?, ?)";

        String sqlStock = "UPDATE medicamento "
                + "SET existencia = existencia - ? "
                + "WHERE id_medicamento = ? AND existencia >= ?";

        String sqlDetalle = "INSERT INTO detalle_factura "
                + "(id_factura, id_medicamento, cantidad, precio_unitario) "
                + "VALUES (?, ?, ?, ?)";

        conexion.setAutoCommit(false);

        try {

            int idFactura = 0;

            try (PreparedStatement ps = conexion.prepareStatement(
                    sqlFactura, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, numero);

                if (idCliente == null) {
                    ps.setNull(2, Types.INTEGER);
                } else {
                    ps.setInt(2, idCliente);
                }

                ps.setInt(3, idEmpleado);
                ps.setBigDecimal(4, total);
                ps.executeUpdate();

                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (!claves.next()) {
                        throw new SQLException("No se obtuvo el ID de la factura.");
                    }
                    idFactura = claves.getInt(1);
                }
            }

            try (PreparedStatement psStock = conexion.prepareStatement(sqlStock);
                 PreparedStatement psDetalle =
                         conexion.prepareStatement(sqlDetalle)) {

                for (DetalleFactura d : detalles) {

                    psStock.setInt(1, d.getCantidad());
                    psStock.setInt(2, d.getIdMedicamento());
                    psStock.setInt(3, d.getCantidad());

                    if (psStock.executeUpdate() == 0) {
                        throw new SQLException(
                                "No hay existencia suficiente del medicamento "
                                + "con ID " + d.getIdMedicamento() + ".");
                    }

                    psDetalle.setInt(1, idFactura);
                    psDetalle.setInt(2, d.getIdMedicamento());
                    psDetalle.setInt(3, d.getCantidad());
                    psDetalle.setBigDecimal(4, d.getPrecioUnitario());
                    psDetalle.executeUpdate();
                }
            }

            conexion.commit();
            return numero;

        } catch (SQLException e) {
            conexion.rollback();
            throw e;
        } finally {
            conexion.setAutoCommit(true);
        }
    }

    // ANULA LA FACTURA EN UNA TRANSACCION. Devuelve false si no existe.
    private static boolean anularFactura(Connection conexion, int id)
            throws SQLException {

        conexion.setAutoCommit(false);

        try {

            List<int[]> items = new ArrayList<>();

            try (PreparedStatement ps = conexion.prepareStatement(
                    "SELECT id_medicamento, cantidad FROM detalle_factura "
                    + "WHERE id_factura = ?")) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(new int[]{rs.getInt(1), rs.getInt(2)});
                    }
                }
            }

            try (PreparedStatement ps = conexion.prepareStatement(
                    "UPDATE medicamento SET existencia = existencia + ? "
                    + "WHERE id_medicamento = ?")) {

                for (int[] item : items) {
                    ps.setInt(1, item[1]);
                    ps.setInt(2, item[0]);
                    ps.executeUpdate();
                }
            }

            try (PreparedStatement ps = conexion.prepareStatement(
                    "DELETE FROM detalle_factura WHERE id_factura = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            int filas;

            try (PreparedStatement ps = conexion.prepareStatement(
                    "DELETE FROM factura WHERE id_factura = ?")) {
                ps.setInt(1, id);
                filas = ps.executeUpdate();
            }

            if (filas == 0) {
                conexion.rollback();
                return false;
            }

            conexion.commit();
            return true;

        } catch (SQLException e) {
            conexion.rollback();
            throw e;
        } finally {
            conexion.setAutoCommit(true);
        }
    }

    // BUSCA UN MEDICAMENTO POR ID (null si no existe)
    private static Medicamento buscarMedicamento(Connection conexion, int id)
            throws SQLException {

        String sql = "SELECT * FROM medicamento WHERE id_medicamento = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                java.sql.Date fecha = rs.getDate("fecha_vencimiento");
                int prov = rs.getInt("id_proveedor");
                Integer idProveedor = rs.wasNull() ? null : prov;

                return new Medicamento(
                        rs.getInt("id_medicamento"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getBigDecimal("precio_compra"),
                        rs.getBigDecimal("precio_venta"),
                        rs.getInt("existencia"),
                        fecha == null ? null : fecha.toLocalDate(),
                        idProveedor
                );
            }
        }
    }

    // DEVUELVE "Nombre Apellido" de un cliente o empleado (null si no existe)
    private static String nombrePersona(Connection conexion, String tabla,
                                        String columnaId, int id)
            throws SQLException {

        String sql = "SELECT nombre, apellido FROM " + tabla
                + " WHERE " + columnaId + " = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getString("nombre") + " " + rs.getString("apellido");
                }

                return null;
            }
        }
    }

    // CONVIERTE TEXTO A ENTERO (null si no es un numero)
    private static Integer leerEntero(String texto) {

        try {
            return Integer.valueOf(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}