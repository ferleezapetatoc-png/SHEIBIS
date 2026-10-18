import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class EmpleadoCRUD {

    // SUBMENU DE EMPLEADOS
    public static void menu(Scanner teclado) {

        int opcion = 0;

        while (opcion != 5) {

            System.out.println("\n--- EMPLEADOS ---");
            System.out.println("1. Registrar empleado");
            System.out.println("2. Mostrar empleados");
            System.out.println("3. Actualizar empleado");
            System.out.println("4. Eliminar empleado");
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

        System.out.println("\n--- REGISTRAR EMPLEADO ---");

        String[] d = pedirDatos(teclado, "", true);
        if (d == null) {
            return;
        }

        String sql = "INSERT INTO empleado (nit, nombre, apellido, telefono, "
                + "direccion, puesto, rol, usuario, contrasena) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.conectar()) {

            if (usuarioExiste(conexion, d[7], 0)) {
                System.out.println("Ese usuario ya existe.");
                return;
            }

            try (PreparedStatement sentencia =
                    conexion.prepareStatement(sql)) {

                for (int i = 0; i < 8; i++) {
                    sentencia.setString(i + 1, d[i]);
                }
                sentencia.setString(9, cifrar(d[8]));

                sentencia.executeUpdate();

                System.out.println("Empleado registrado correctamente.");
            }

        } catch (SQLException e) {
            System.out.println("Error al registrar: " + e.getMessage());
        }
    }

    // READ (nunca se muestra la contrasena)
    public static void mostrar() {

        String sql = "SELECT * FROM empleado ORDER BY id_empleado";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet rs = sentencia.executeQuery()) {

            System.out.println("\n--- LISTA DE EMPLEADOS ---");

            boolean hay = false;

            while (rs.next()) {

                hay = true;

                Empleado e = new Empleado(
                        rs.getInt("id_empleado"),
                        rs.getString("nit"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("telefono"),
                        rs.getString("direccion"),
                        rs.getString("puesto"),
                        rs.getString("usuario"),
                        ""
                );

                e.mostrarInformacion();
                System.out.println("Rol: " + rs.getString("rol"));
                System.out.println("------------------------");
            }

            if (!hay) {
                System.out.println("No hay empleados registrados.");
            }

        } catch (SQLException e) {
            System.out.println("Error al mostrar: " + e.getMessage());
        }
    }

    // UPDATE (si la contrasena se deja vacia, se conserva la actual)
    public static void actualizar(Scanner teclado) {

        System.out.println("\n--- ACTUALIZAR EMPLEADO ---");

        System.out.print("ID del empleado: ");
        int id;

        try {
            id = Integer.parseInt(teclado.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("ID incorrecto.");
            return;
        }

        String[] d = pedirDatos(teclado, "Nuevo ", false);
        if (d == null) {
            return;
        }

        boolean cambiaClave = !d[8].isEmpty();

        String sql = "UPDATE empleado SET nit = ?, nombre = ?, apellido = ?, "
                + "telefono = ?, direccion = ?, puesto = ?, rol = ?, "
                + "usuario = ?"
                + (cambiaClave ? ", contrasena = ?" : "")
                + " WHERE id_empleado = ?";

        try (Connection conexion = Conexion.conectar()) {

            if (usuarioExiste(conexion, d[7], id)) {
                System.out.println("Ese usuario pertenece a otro empleado.");
                return;
            }

            try (PreparedStatement sentencia =
                    conexion.prepareStatement(sql)) {

                for (int i = 0; i < 8; i++) {
                    sentencia.setString(i + 1, d[i]);
                }

                if (cambiaClave) {
                    sentencia.setString(9, cifrar(d[8]));
                    sentencia.setInt(10, id);
                } else {
                    sentencia.setInt(9, id);
                }

                int filas = sentencia.executeUpdate();

                if (filas > 0) {
                    System.out.println("Empleado actualizado correctamente.");
                } else {
                    System.out.println("No se encontro ese empleado.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al actualizar: " + e.getMessage());
        }
    }

    // DELETE
    public static void eliminar(Scanner teclado) {

        System.out.println("\n--- ELIMINAR EMPLEADO ---");
        System.out.print("ID del empleado: ");

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

        String sql = "DELETE FROM empleado WHERE id_empleado = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);

            int filas = sentencia.executeUpdate();

            if (filas > 0) {
                System.out.println("Empleado eliminado correctamente.");
            } else {
                System.out.println("No se encontro ese empleado.");
            }

        } catch (SQLException e) {
            System.out.println("No se pudo eliminar: " + e.getMessage());
            System.out.println("Verifique si tiene facturas relacionadas.");
        }
    }

    // PIDE LOS DATOS Y LOS VALIDA. Devuelve un arreglo con este orden:
    // 0 nit, 1 nombre, 2 apellido, 3 telefono, 4 direccion, 5 puesto,
    // 6 rol, 7 usuario, 8 contrasena (en texto, se cifra al guardar)
    private static String[] pedirDatos(Scanner teclado, String prefijo,
                                       boolean claveObligatoria) {

        System.out.print(prefijo + "NIT: ");
        String nit = teclado.nextLine().trim();

        System.out.print(prefijo + "nombre: ");
        String nombre = teclado.nextLine().trim();

        System.out.print(prefijo + "apellido: ");
        String apellido = teclado.nextLine().trim();

        System.out.print(prefijo + "telefono: ");
        String telefono = teclado.nextLine().trim();

        System.out.print(prefijo + "direccion: ");
        String direccion = teclado.nextLine().trim();

        System.out.print(prefijo + "puesto: ");
        String puesto = teclado.nextLine().trim();

        System.out.print(prefijo
                + "rol (1 = ADMINISTRADOR, 2 = EMPLEADO): ");
        String opcionRol = teclado.nextLine().trim();

        String rol;
        if (opcionRol.equals("1")) {
            rol = "ADMINISTRADOR";
        } else if (opcionRol.equals("2")) {
            rol = "EMPLEADO";
        } else {
            System.out.println("Rol invalido. Escriba 1 o 2.");
            return null;
        }

        System.out.print(prefijo + "usuario: ");
        String usuario = teclado.nextLine().trim();

        System.out.print(prefijo + "contrasena"
                + (claveObligatoria ? ": " : " (vacio para no cambiarla): "));
        String contrasena = teclado.nextLine();

        if (nombre.isEmpty() || apellido.isEmpty() || usuario.isEmpty()) {
            System.out.println("Nombre, apellido y usuario son obligatorios.");
            return null;
        }

        if (claveObligatoria && contrasena.isEmpty()) {
            System.out.println("La contrasena es obligatoria.");
            return null;
        }

        if (!contrasena.isEmpty() && contrasena.length() < 4) {
            System.out.println("La contrasena debe tener al menos 4 caracteres.");
            return null;
        }

        return new String[]{nit, nombre, apellido, telefono, direccion,
                puesto, rol, usuario, contrasena};
    }

    // VERIFICA SI EL USUARIO YA LO TIENE OTRO EMPLEADO (idActual = 0 al registrar)
    private static boolean usuarioExiste(Connection conexion, String usuario,
                                         int idActual) throws SQLException {

        String sql = "SELECT id_empleado FROM empleado "
                + "WHERE usuario = ? AND id_empleado <> ?";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setString(1, usuario);
            consulta.setInt(2, idActual);

            try (ResultSet rs = consulta.executeQuery()) {
                return rs.next();
            }
        }
    }

    // CIFRA LA CONTRASENA CON SHA-256 (se guarda como texto de 64 caracteres)
    public static String cifrar(String texto) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No se pudo cifrar la contrasena.", e);
        }
    }
}