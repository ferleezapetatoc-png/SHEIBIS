import java.awt.*;
import javax.swing.*;

// Ventana principal de la aplicacion
public class MainFrame extends JFrame {

    private final MainPanel panel = new MainPanel();

    public MainFrame() {
        super("Farmacia SHEIBIS");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(panel);
        setSize(1240, 760);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
        panel.ir("Inicio");
    }
}
