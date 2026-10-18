import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ProveedorCRUD {

    // SUBMENU DE PROVEEDORES
    public static void menu(Scanner teclado) {

        int opcion = 0;

        while (opcion != 5) {

            System.out.println("\n--- PROVEEDORES ---");
            System.out.println("1. Registrar proveedor");
            System.out.println("2. Mostrar proveedores");
            System.out.println("3. Actualizar proveedor");
            System.out.println("4. Eliminar proveedor");
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

        System.out.println("\n--- REGISTRAR PROVEEDOR ---");

        Proveedor p = pedirDatos(teclado, "");
        if (p == null) {
            return;
        }

        String sql = "INSERT INTO proveedor "
                + "(nit, nombre, telefono, direccion, correo) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.conectar()) {

            if (nitExiste(conexion, p.getNit(), 0)) {
                System.out.println("El NIT ya esta registrado.");
                return;
            }

            try (PreparedStatement sentencia =
                    conexion.prepareStatement(sql)) {

                llenarSentencia(sentencia, p);
                sentencia.executeUpdate();

                System.out.println("Proveedor registrado correctamente.");
            }

        } catch (SQLException e) {
            System.out.println("Error al registrar: " + e.getMessage());
        }
    }

    // READ
    public static void mostrar() {

        String sql = "SELECT * FROM proveedor ORDER BY id_proveedor";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet rs = sentencia.executeQuery()) {

            System.out.println("\n--- LISTA DE PROVEEDORES ---");

            boolean hay = false;

            while (rs.next()) {

                hay = true;

                Proveedor p = new Proveedor(
                        rs.getInt("id_proveedor"),
                        rs.getString("nit"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getString("direccion"),
                        rs.getString("correo")
                );

                System.out.println("ID: " + p.getIdProveedor());
                p.mostrarInformacion();
                System.out.println("Direccion: " + p.getDireccion());
                System.out.println("------------------------");
            }

            if (!hay) {
                System.out.println("No hay proveedores registrados.");
            }

        } catch (SQLException e) {
            System.out.println("Error al mostrar: " + e.getMessage());
        }
    }

    // UPDATE
    public static void actualizar(Scanner teclado) {

        System.out.println("\n--- ACTUALIZAR PROVEEDOR ---");

        System.out.print("ID del proveedor: ");
        int id;

        try {
            id = Integer.parseInt(teclado.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("ID incorrecto.");
            return;
        }

        Proveedor p = pedirDatos(teclado, "Nuevo ");
        if (p == null) {
            return;
        }

        String sql = "UPDATE proveedor SET nit = ?, nombre = ?, "
                + "telefono = ?, direccion = ?, correo = ? "
                + "WHERE id_proveedor = ?";

        try (Connection conexion = Conexion.conectar()) {

            if (nitExiste(conexion, p.getNit(), id)) {
                System.out.println("El NIT pertenece a otro proveedor.");
                return;
            }

            try (PreparedStatement sentencia =
                    conexion.prepareStatement(sql)) {

                llenarSentencia(sentencia, p);
                sentencia.setInt(6, id);

                int filas = sentencia.executeUpdate();

                if (filas > 0) {
                    System.out.println("Proveedor actualizado correctamente.");
                } else {
                    System.out.println("No se encontro ese proveedor.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al actualizar: " + e.getMessage());
        }
    }

    // DELETE
    public static void eliminar(Scanner teclado) {

        System.out.println("\n--- ELIMINAR PROVEEDOR ---");
        System.out.print("ID del proveedor: ");

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

        String sql = "DELETE FROM proveedor WHERE id_proveedor = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);

            int filas = sentencia.executeUpdate();

            if (filas > 0) {
                System.out.println("Proveedor eliminado correctamente.");
            } else {
                System.out.println("No se encontro ese proveedor.");
            }

        } catch (SQLException e) {
            System.out.println("No se pudo eliminar: " + e.getMessage());
            System.out.println("Verifique si tiene medicamentos relacionados.");
        }
    }

    // PIDE LOS DATOS POR TECLADO Y LOS VALIDA (usado por registrar y actualizar)
    private static Proveedor pedirDatos(Scanner teclado, String prefijo) {

        System.out.print(prefijo + "NIT: ");
        String nit = teclado.nextLine().trim();

        System.out.print(prefijo + "nombre: ");
        String nombre = teclado.nextLine().trim();

        System.out.print(prefijo + "telefono: ");
        String telefono = teclado.nextLine().trim();

        System.out.print(prefijo + "direccion: ");
        String direccion = teclado.nextLine().trim();

        System.out.print(prefijo + "correo: ");
        String correo = teclado.nextLine().trim();

        if (nit.isEmpty() || nombre.isEmpty()) {
            System.out.println("NIT y nombre son obligatorios.");
            return null;
        }

        if (!correo.isEmpty() && !correo.contains("@")) {
            System.out.println("El correo no es valido.");
            return null;
        }

        return new Proveedor(0, nit, nombre, telefono, direccion, correo);
    }

    // ASIGNA LOS 5 PRIMEROS PARAMETROS (INSERT y UPDATE usan el mismo orden)
    private static void llenarSentencia(PreparedStatement s, Proveedor p)
            throws SQLException {

        s.setString(1, p.getNit());
        s.setString(2, p.getNombre());
        s.setString(3, p.getTelefono());
        s.setString(4, p.getDireccion());
        s.setString(5, p.getCorreo());
    }

    // VERIFICA SI EL NIT YA LO TIENE OTRO PROVEEDOR (idActual = 0 al registrar)
    private static boolean nitExiste(Connection conexion, String nit,
                                     int idActual) throws SQLException {

        String sql = "SELECT id_proveedor FROM proveedor "
                + "WHERE nit = ? AND id_proveedor <> ?";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setString(1, nit);
            consulta.setInt(2, idActual);

            try (ResultSet rs = consulta.executeQuery()) {
                return rs.next();
            }
        }
    }
}