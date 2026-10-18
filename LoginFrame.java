import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

// Ventana de inicio de sesion
public class LoginFrame extends JFrame {

    public LoginFrame() {
        super("Farmacia SHEIBIS - Inicio de sesion");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        LoginPanel panel = new LoginPanel();
        setContentPane(panel);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(panel.getBotonEntrar());
    }

    // ----- Primer uso: crear administrador inicial -----
    public static void pedirAdministradorInicial(Component padre) {

        Tema.Campo nombre = new Tema.Campo("Nombre");
        Tema.Campo apellido = new Tema.Campo("Apellido");
        Tema.Campo usuario = new Tema.Campo("Usuario");
        Tema.Clave clave = new Tema.Clave("Contrasena (minimo 4 caracteres)");

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 10));
        form.setPreferredSize(new Dimension(340, 260));
        JLabel info = new JLabel("<html><b>Primer uso:</b> no hay ningun "
                + "administrador.<br>Cree el administrador inicial.</html>");
        info.setFont(Tema.fuente(Font.PLAIN, 13f));
        form.add(info);
        form.add(nombre);
        form.add(apellido);
        form.add(usuario);
        form.add(clave);

        while (true) {
            int r = JOptionPane.showConfirmDialog(padre, form,
                    "Crear administrador", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (r != JOptionPane.OK_OPTION) {
                System.exit(0);
            }

            try {
                Login.crearAdministrador(nombre.getText().trim(),
                        apellido.getText().trim(), usuario.getText().trim(),
                        new String(clave.getPassword()));
                JOptionPane.showMessageDialog(padre,
                        "Administrador creado correctamente.");
                return;
            } catch (ValidacionException e) {
                JOptionPane.showMessageDialog(padre, e.getMessage(),
                        "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(padre,
                        "Error con la base de datos: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }
    }
}
