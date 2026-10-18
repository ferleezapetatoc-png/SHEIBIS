public class Sesion {

    public static int idEmpleado = 0;
    public static String nombre = "";
    public static String rol = "";

    public static boolean esAdministrador() {
        return "ADMINISTRADOR".equals(rol);
    }
}