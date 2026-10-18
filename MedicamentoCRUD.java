import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class MedicamentoCRUD {

    // SUBMENU DE MEDICAMENTOS
    public static void menu(Scanner teclado) {

        int opcion = 0;

        while (opcion != 5) {

            System.out.println("\n--- MEDICAMENTOS ---");
            System.out.println("1. Registrar medicamento");
            System.out.println("2. Mostrar medicamentos");
            System.out.println("3. Actualizar medicamento");
            System.out.println("4. Eliminar medicamento");
            System.out.println("5. Volver");
            System.out.print("Seleccione una opcion: ");

            try {
                opcion = Integer.parseInt(teclado.nextLine());

                switch (opcion) {
                    case 1:
                        registrar(teclado);
                        break;
                    case 2:
                        mostrar();
                        break;
                    case 3:
                        actualizar(teclado);
                        break;
                    case 4:
                        eliminar(teclado);
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

    // CREATE
    public static void registrar(Scanner teclado) {

        System.out.println("\n--- REGISTRAR MEDICAMENTO ---");

        Medicamento m = pedirDatos(teclado, "");
        if (m == null) {
            return;
        }

        String sql = "INSERT INTO medicamento "
                + "(nombre, descripcion, precio_compra, precio_venta, "
                + "existencia, fecha_vencimiento, id_proveedor) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            llenarSentencia(sentencia, m);
            sentencia.executeUpdate();

            System.out.println("Medicamento registrado correctamente.");

        } catch (SQLException e) {
            System.out.println("Error al registrar: " + e.getMessage());
        }
    }

    // READ
    public static void mostrar() {

        String sql = "SELECT * FROM medicamento ORDER BY id_medicamento";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet rs = sentencia.executeQuery()) {

            System.out.println("\n--- LISTA DE MEDICAMENTOS ---");

            boolean hay = false;

            while (rs.next()) {

                hay = true;

                Date fecha = rs.getDate("fecha_vencimiento");
                int prov = rs.getInt("id_proveedor");
                Integer idProveedor = rs.wasNull() ? null : prov;

                Medicamento m = new Medicamento(
                        rs.getInt("id_medicamento"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getBigDecimal("precio_compra"),
                        rs.getBigDecimal("precio_venta"),
                        rs.getInt("existencia"),
                        fecha == null ? null : fecha.toLocalDate(),
                        idProveedor
                );

                System.out.println("ID: " + m.getIdMedicamento());
                m.mostrarInformacion();
                System.out.println("Vence: "
                        + (m.getFechaVencimiento() == null
                        ? "sin fecha" : m.getFechaVencimiento()));
                System.out.println("------------------------");
            }

            if (!hay) {
                System.out.println("No hay medicamentos registrados.");
            }

        } catch (SQLException e) {
            System.out.println("Error al mostrar: " + e.getMessage());
        }
    }

    // UPDATE
    public static void actualizar(Scanner teclado) {

        System.out.println("\n--- ACTUALIZAR MEDICAMENTO ---");

        System.out.print("ID del medicamento: ");
        int id;

        try {
            id = Integer.parseInt(teclado.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("ID incorrecto.");
            return;
        }

        Medicamento m = pedirDatos(teclado, "Nuevo ");
        if (m == null) {
            return;
        }

        String sql = "UPDATE medicamento SET nombre = ?, descripcion = ?, "
                + "precio_compra = ?, precio_venta = ?, existencia = ?, "
                + "fecha_vencimiento = ?, id_proveedor = ? "
                + "WHERE id_medicamento = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            llenarSentencia(sentencia, m);
            sentencia.setInt(8, id);

            int filas = sentencia.executeUpdate();

            if (filas > 0) {
                System.out.println("Medicamento actualizado correctamente.");
            } else {
                System.out.println("No se encontro ese medicamento.");
            }

        } catch (SQLException e) {
            System.out.println("Error al actualizar: " + e.getMessage());
        }
    }

    // DELETE
    public static void eliminar(Scanner teclado) {

        System.out.println("\n--- ELIMINAR MEDICAMENTO ---");
        System.out.print("ID del medicamento: ");

        int id;

        try {
            id = Integer.parseInt(teclado.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("ID incorrecto.");
            return;
        }

        System.out.print("¿Desea eliminarlo? Escriba S para confirmar: ");
        String confirmacion = teclado.nextLine();

        if (!confirmacion.equalsIgnoreCase("S")) {
            System.out.println("Operacion cancelada.");
            return;
        }

        String sql = "DELETE FROM medicamento WHERE id_medicamento = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);

            int filas = sentencia.executeUpdate();

            if (filas > 0) {
                System.out.println("Medicamento eliminado correctamente.");
            } else {
                System.out.println("No se encontro ese medicamento.");
            }

        } catch (SQLException e) {
            System.out.println("No se pudo eliminar: " + e.getMessage());
            System.out.println("Verifique si esta en alguna factura.");
        }
    }

    // PIDE LOS DATOS POR TECLADO Y LOS VALIDA (usado por registrar y actualizar)
    private static Medicamento pedirDatos(Scanner teclado, String prefijo) {

        try {
            System.out.print(prefijo + "nombre: ");
            String nombre = teclado.nextLine().trim();

            System.out.print(prefijo + "descripcion: ");
            String descripcion = teclado.nextLine().trim();

            System.out.print(prefijo + "precio de compra: ");
            BigDecimal precioCompra = new BigDecimal(teclado.nextLine().trim());

            System.out.print(prefijo + "precio de venta: ");
            BigDecimal precioVenta = new BigDecimal(teclado.nextLine().trim());

            System.out.print(prefijo + "existencia: ");
            int existencia = Integer.parseInt(teclado.nextLine().trim());

            System.out.print(prefijo
                    + "fecha de vencimiento (AAAA-MM-DD, vacio si no tiene): ");
            String textoFecha = teclado.nextLine().trim();
            LocalDate fecha = textoFecha.isEmpty()
                    ? null : LocalDate.parse(textoFecha);

            System.out.print(prefijo
                    + "ID del proveedor (vacio si no tiene): ");
            String textoProv = teclado.nextLine().trim();
            Integer idProveedor = textoProv.isEmpty()
                    ? null : Integer.valueOf(textoProv);

            if (nombre.isEmpty()) {
                System.out.println("El nombre es obligatorio.");
                return null;
            }

            if (precioCompra.signum() < 0 || precioVenta.signum() < 0
                    || existencia < 0) {
                System.out.println("Precios y existencia no pueden ser negativos.");
                return null;
            }

            return new Medicamento(0, nombre, descripcion, precioCompra,
                    precioVenta, existencia, fecha, idProveedor);

        } catch (NumberFormatException e) {
            System.out.println("Numero invalido. Operacion cancelada.");
        } catch (DateTimeParseException e) {
            System.out.println("Fecha invalida. Use AAAA-MM-DD. Operacion cancelada.");
        }

        return null;
    }

    // ASIGNA LOS 7 PRIMEROS PARAMETROS (INSERT y UPDATE usan el mismo orden)
    private static void llenarSentencia(PreparedStatement s, Medicamento m)
            throws SQLException {

        s.setString(1, m.getNombre());
        s.setString(2, m.getDescripcion());
        s.setBigDecimal(3, m.getPrecioCompra());
        s.setBigDecimal(4, m.getPrecioVenta());
        s.setInt(5, m.getExistencia());

        if (m.getFechaVencimiento() == null) {
            s.setNull(6, Types.DATE);
        } else {
            s.setDate(6, Date.valueOf(m.getFechaVencimiento()));
        }

        if (m.getIdProveedor() == null) {
            s.setNull(7, Types.INTEGER);
        } else {
            s.setInt(7, m.getIdProveedor());
        }
    }
}