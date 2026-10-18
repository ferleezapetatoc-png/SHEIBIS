import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// Demostracion visual de los 4 pilares de la POO
public class PanelPOO extends PanelBase {

    private final JTextArea salida = new JTextArea();

    public PanelPOO() {
        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);
        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(Tema.titulo("Pilares de la POO", 24f));
        textos.add(Tema.texto("Los cuatro pilares aplicados en las clases del sistema", Tema.GRIS));
        cab.add(textos, BorderLayout.WEST);

        Tema.Boton ejecutar = new Tema.Boton("Ejecutar demostracion", Tema.VERDE);
        ejecutar.addActionListener(e -> ejecutar());
        JPanel derecha = new JPanel(new GridBagLayout());
        derecha.setOpaque(false);
        derecha.add(ejecutar);
        cab.add(derecha, BorderLayout.EAST);
        add(cab, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(1, 2, 18, 0));
        centro.setOpaque(false);

        JPanel pilares = new JPanel(new GridLayout(4, 1, 0, 12));
        pilares.setOpaque(false);
        pilares.add(pilar("1", "Abstraccion", Tema.AZUL,
                "Persona es una clase abstracta: define lo comun y obliga "
                + "a las hijas a implementar mostrarInformacion()."));
        pilares.add(pilar("2", "Herencia", Tema.VERDE,
                "Cliente y Empleado extienden de Persona y reutilizan "
                + "sus atributos y metodos."));
        pilares.add(pilar("3", "Polimorfismo", Tema.AMBAR,
                "Un mismo metodo, mostrarInformacion(), responde distinto "
                + "segun la clase del objeto."));
        pilares.add(pilar("4", "Encapsulamiento", new Color(0x8B5CF6),
                "Los atributos son privados y solo se usan mediante "
                + "getters y setters."));
        centro.add(pilares);

        Tema.Tarjeta consola = new Tema.Tarjeta(new BorderLayout(0, 10));
        consola.add(Tema.titulo("Resultado", 15f), BorderLayout.NORTH);
        salida.setEditable(false);
        salida.setLineWrap(true);
        salida.setWrapStyleWord(true);
        salida.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        salida.setBackground(Tema.NOCHE);
        salida.setForeground(new Color(0xA7F3D0));
        salida.setCaretColor(Tema.NOCHE);
        salida.setBorder(new EmptyBorder(12, 14, 12, 14));
        salida.setText("Presione \"Ejecutar demostracion\" para ver\n"
                + "los cuatro pilares funcionando.");
        JScrollPane sp = new JScrollPane(salida);
        sp.setBorder(BorderFactory.createEmptyBorder());
        consola.add(sp, BorderLayout.CENTER);
        centro.add(consola);

        add(centro, BorderLayout.CENTER);
    }

    private JPanel pilar(String numero, String nombre, Color color, String desc) {
        Tema.Tarjeta t = new Tema.Tarjeta(new BorderLayout(14, 0));

        JLabel num = new JLabel(numero, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Tema.suave(g);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        num.setFont(Tema.fuente(Font.BOLD, 20f));
        num.setForeground(Color.WHITE);
        num.setPreferredSize(new Dimension(46, 46));

        JPanel izq = new JPanel(new BorderLayout());
        izq.setOpaque(false);
        izq.add(num, BorderLayout.NORTH);

        JPanel txt = new JPanel(new BorderLayout(0, 4));
        txt.setOpaque(false);
        txt.add(Tema.titulo(nombre, 16f), BorderLayout.NORTH);
        JLabel d = new JLabel("<html>" + desc + "</html>");
        d.setFont(Tema.fuente(Font.PLAIN, 13f));
        d.setForeground(Tema.GRIS);
        txt.add(d, BorderLayout.CENTER);

        t.add(izq, BorderLayout.WEST);
        t.add(txt, BorderLayout.CENTER);
        return t;
    }

    // Ejecuta Main.demostrarPOO() y muestra su salida en pantalla
    private void ejecutar() {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            Main.demostrarPOO();
        } finally {
            System.setOut(original);
        }
        salida.setText(buffer.toString(StandardCharsets.UTF_8));
        salida.setCaretPosition(0);
    }
}
