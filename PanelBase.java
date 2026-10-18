import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// Base de todos los paneles del menu: se vuelve a cargar al mostrarse
public abstract class PanelBase extends JPanel {

    public PanelBase() {
        super(new BorderLayout(0, 18));
        setBackground(Tema.FONDO);
        setBorder(new EmptyBorder(26, 30, 26, 30));
    }

    // Se llama cada vez que el usuario abre este panel
    public void alMostrar() {
    }

    protected static void error(Component padre, String texto) {
        JOptionPane.showMessageDialog(padre, texto, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    protected static void aviso(Component padre, String texto) {
        JOptionPane.showMessageDialog(padre, texto, "Atencion",
                JOptionPane.WARNING_MESSAGE);
    }

    protected static String mensajeBD(Exception e) {
        return "No se pudo completar la operacion con la base de datos.\n"
                + e.getMessage();
    }
}
