import javax.swing.SwingUtilities;

// Punto de entrada de la version con interfaz grafica
public class AppGUI {

    public static void main(String[] args) {
        Tema.iniciar();

        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);

            // Primer uso: si no hay administradores, se crea el inicial
            Tarea.ejecutar(Login::contarAdministradores,
                    cantidad -> {
                        if (cantidad == 0) {
                            LoginFrame.pedirAdministradorInicial(login);
                        }
                    },
                    error -> javax.swing.JOptionPane.showMessageDialog(login,
                            "No se pudo conectar con MySQL.\nVerifique que XAMPP/MySQL "
                            + "este encendido.\n" + error.getMessage(),
                            "Sin conexion", javax.swing.JOptionPane.ERROR_MESSAGE));
        });
    }
}
