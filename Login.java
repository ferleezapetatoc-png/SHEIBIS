import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Login {

    // INICIA SESION. Devuelve true si el usuario entro, false si no.
    public static boolean iniciar(Scanner teclado) {

        try {
            if (contarAdministradores() == 0) {
                crearAdministradorInicial(teclado);
            }
        } catch (SQLException e) {
            System.out.println("Error con la base de datos: " + e.getMessage());
            return false;
        }

        System.out.println("\n===== INICIO DE SESION =====");

        for (int intento = 1; intento <= 3; intento++) {

            System.out.print("Usuario: ");
            String usuario = teclado.nextLine().trim();

            System.out.print("Contrasena: ");
            String contrasena = teclado.nextLine();

            try {
                if (validar(usuario, contrasena)) {
                    return true;
                }
            } catch (SQLException e) {
                System.out.println("Error con la base de datos: " + e.getMessage());
                return false;
            }

            System.out.println("Usuario o contrasena incorrectos. Intento "
                    + intento + " de 3.");
        }

        return false;
    }

    // CUANTOS ADMINISTRADORES EXISTEN
    public static int contarAdministradores() throws SQLException {

        String sql = "SELECT COUNT(*) FROM empleado WHERE rol = 'ADMINISTRADOR'";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getInt(1);
        }
    }

    // SI NO HAY NINGUN ADMINISTRADOR, SE CREA EL PRIMERO
    private static void crearAdministradorInicial(Scanner teclado)
            throws SQLException {

        System.out.println("\n===== PRIMER USO =====");
        System.out.println("No hay ningun administrador registrado.");
        System.out.println("Cree el administrador inicial.");

        String nombre;
        String apellido;
        String usuario;
        String contrasena;

        while (true) {

            System.out.print("Nombre: ");
            nombre = teclado.nextLine().trim();

            System.out.print("Apellido: ");
            apellido = teclado.nextLine().trim();

            System.out.print("Usuario: ");
            usuario = teclado.nextLine().trim();

            System.out.print("Contrasena (minimo 4 caracteres): ");
            contrasena = teclado.nextLine();

            if (nombre.isEmpty() || apellido.isEmpty() || usuario.isEmpty()) {
                System.out.println("Nombre, apellido y usuario son obligatorios.\n");
                continue;
            }

            if (contrasena.length() < 4) {
                System.out.println("La contrasena es muy corta.\n");
                continue;
            }

            if (usuarioExiste(usuario)) {
                System.out.println("Ese usuario ya existe. Elija otro.\n");
                continue;
            }

            break;
        }

        String sql = "INSERT INTO empleado "
                + "(nombre, apellido, puesto, rol, usuario, contrasena) "
                + "VALUES (?, ?, 'Administrador', 'ADMINISTRADOR', ?, ?)";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, usuario);
            ps.setString(4, EmpleadoCRUD.cifrar(contrasena));
            ps.executeUpdate();
        }

        System.out.println("Administrador creado correctamente.");
    }

    public static boolean usuarioExiste(String usuario) throws SQLException {

        String sql = "SELECT id_empleado FROM empleado WHERE usuario = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, usuario);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // COMPARA USUARIO Y CONTRASENA CIFRADA. Si coinciden, llena la Sesion.
    public static boolean validar(String usuario, String contrasena)
            throws SQLException {

        String sql = "SELECT id_empleado, nombre, apellido, rol, contrasena "
                + "FROM empleado WHERE usuario = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, usuario);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return false;
                }

                String guardada = rs.getString("contrasena");

                if (!guardada.equals(EmpleadoCRUD.cifrar(contrasena))) {
                    return false;
                }

                Sesion.idEmpleado = rs.getInt("id_empleado");
                Sesion.nombre = rs.getString("nombre") + " "
                        + rs.getString("apellido");
                Sesion.rol = rs.getString("rol");

                return true;
            }
        }
    }

    // CREA EL ADMINISTRADOR INICIAL (la interfaz grafica usa este metodo)
    public static void crearAdministrador(String nombre, String apellido,
                                          String usuario, String contrasena)
            throws SQLException, ValidacionException {

        if (nombre.isEmpty() || apellido.isEmpty() || usuario.isEmpty()) {
            throw new ValidacionException(
                    "Nombre, apellido y usuario son obligatorios.");
        }

        if (contrasena.length() < 4) {
            throw new ValidacionException(
                    "La contrasena debe tener minimo 4 caracteres.");
        }

        if (usuarioExiste(usuario)) {
            throw new ValidacionException(
                    "Ese usuario ya existe. Elija otro.");
        }

        String sql = "INSERT INTO empleado "
                + "(nombre, apellido, puesto, rol, usuario, contrasena) "
                + "VALUES (?, ?, 'Administrador', 'ADMINISTRADOR', ?, ?)";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, usuario);
            ps.setString(4, EmpleadoCRUD.cifrar(contrasena));
            ps.executeUpdate();
        }
    }
}
