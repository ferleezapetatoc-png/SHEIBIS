import java.awt.*;
import javax.swing.*;

// Marcador para los modulos que aun no tienen pantalla grafica
public class PanelProximamente extends PanelBase {

    public PanelProximamente(String modulo) {
        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);

        Tema.Tarjeta t = new Tema.Tarjeta(new BorderLayout(0, 8));
        t.setPreferredSize(new Dimension(460, 170));

        JLabel titulo = Tema.titulo(modulo, 22f);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel texto = new JLabel("<html><div style='text-align:center'>"
                + "Esta pantalla se esta preparando.<br>"
                + "Mientras tanto puede usar este modulo desde la consola."
                + "</div></html>", SwingConstants.CENTER);
        texto.setFont(Tema.fuente(Font.PLAIN, 14f));
        texto.setForeground(Tema.GRIS);

        t.add(titulo, BorderLayout.NORTH);
        t.add(texto, BorderLayout.CENTER);
        centro.add(t);
        add(centro, BorderLayout.CENTER);
    }
}
