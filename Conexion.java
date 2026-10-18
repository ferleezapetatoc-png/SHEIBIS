
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL =
            "jdbc:mysql://localhost:3306/farmacia_sheibis";

    private static final String USUARIO = "root";

    // Si tu usuario root tiene contraseña, escríbela entre las comillas.
    private static final String CONTRASENA = "";

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(
                URL,
                USUARIO,
                CONTRASENA
        );
    }

    public static void main(String[] args) {
       try (Connection conexion = conectar()) {
    if (conexion != null && !conexion.isClosed()) {
        System.out.println(
            "¡Conexión a la base de datos exitosa!"
        );
        System.out.println(
            "Base de datos: " +
            conexion.getCatalog()
        );
    }

        } catch (SQLException e) {
            System.out.println(
                    "Error al conectar con MySQL:"
            );  
            System.out.println(e.getMessage());
        }
    }
}
