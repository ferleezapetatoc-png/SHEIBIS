import java.awt.*;
import java.awt.geom.Ellipse2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// Contenido de la pantalla de inicio de sesion
public class LoginPanel extends JPanel {

    private final Tema.Campo campoUsuario = new Tema.Campo("Ingrese su usuario");
    private final Tema.Clave campoClave = new Tema.Clave("Ingrese su contrasena");
    private final JLabel mensaje = new JLabel(" ");
    private final Tema.Boton botonEntrar =
            new Tema.Boton("Iniciar sesion", Tema.VERDE);
    private int intentos = 0;

    public LoginPanel() {
        super(new GridLayout(1, 2));
        setPreferredSize(new Dimension(900, 540));
        add(panelMarca());
        add(panelFormulario());
        botonEntrar.addActionListener(e -> entrar());
    }

    public JButton getBotonEntrar() {
        return botonEntrar;
    }

    // ----- Lado izquierdo: marca con degradado -----
    private JPanel panelMarca() {
        JPanel p = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Tema.suave(g);
                g2.setPaint(new GradientPaint(0, 0, new Color(0x064E3B),
                        getWidth(), getHeight(), new Color(0x10B981)));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(new Color(255, 255, 255, 22));
                g2.fill(new Ellipse2D.Float(-90, getHeight() - 220, 330, 330));
                g2.setColor(new Color(255, 255, 255, 16));
                g2.fill(new Ellipse2D.Float(getWidth() - 170, -110, 300, 300));

                // Cruz de farmacia
                g2.setColor(new Color(255, 255, 255, 235));
                int cx = 60;
                int cy = 70;
                g2.fillRoundRect(cx + 21, cy, 18, 60, 8, 8);
                g2.fillRoundRect(cx, cy + 21, 60, 18, 8, 8);
                g2.dispose();
            }
        };

        JLabel marca = new JLabel("SHEIBIS");
        marca.setFont(Tema.fuente(Font.BOLD, 40f));
        marca.setForeground(Color.WHITE);
        marca.setBounds(60, 160, 360, 52);
        p.add(marca);

        JLabel sub = new JLabel("Sistema de gestion de farmacia");
        sub.setFont(Tema.fuente(Font.PLAIN, 17f));
        sub.setForeground(new Color(0xD1FAE5));
        sub.setBounds(60, 212, 380, 26);
        p.add(sub);

        String[] ventajas = {
            "Control de medicamentos y existencias",
            "Facturacion rapida y segura",
            "Reportes de ventas e inventario"
        };
        int y = 300;
        for (String v : ventajas) {
            JLabel l = new JLabel(v, iconoCheck(), SwingConstants.LEFT);
            l.setIconTextGap(12);
            l.setFont(Tema.fuente(Font.PLAIN, 14f));
            l.setForeground(Color.WHITE);
            l.setBounds(60, y, 380, 24);
            p.add(l);
            y += 34;
        }
        return p;
    }

    // Icono de "check" dibujado (no depende de la fuente del sistema)
    private static Icon iconoCheck() {
        return new Icon() {
            public int getIconWidth() { return 20; }
            public int getIconHeight() { return 20; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = Tema.suave(g);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.fillOval(x, y, 20, 20);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
                g2.drawPolyline(new int[]{x + 5, x + 9, x + 15},
                        new int[]{y + 10, y + 14, y + 6}, 3);
                g2.dispose();
            }
        };
    }

    // ----- Lado derecho: formulario -----
    private JPanel panelFormulario() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(70, 60, 40, 60));

        JLabel t = Tema.titulo("Bienvenido", 30f);
        t.setAlignmentX(LEFT_ALIGNMENT);
        JLabel s = Tema.texto("Ingrese sus credenciales para continuar", Tema.GRIS);
        s.setAlignmentX(LEFT_ALIGNMENT);

        p.add(t);
        p.add(Box.createVerticalStrut(6));
        p.add(s);
        p.add(Box.createVerticalStrut(34));

        p.add(etiqueta("Usuario"));
        p.add(Box.createVerticalStrut(6));
        campoUsuario.setAlignmentX(LEFT_ALIGNMENT);
        campoUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        p.add(campoUsuario);

        p.add(Box.createVerticalStrut(18));
        p.add(etiqueta("Contrasena"));
        p.add(Box.createVerticalStrut(6));
        campoClave.setAlignmentX(LEFT_ALIGNMENT);
        campoClave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        p.add(campoClave);

        mensaje.setFont(Tema.fuente(Font.PLAIN, 13f));
        mensaje.setForeground(Tema.ROJO);
        mensaje.setAlignmentX(LEFT_ALIGNMENT);
        p.add(Box.createVerticalStrut(14));
        p.add(mensaje);

        p.add(Box.createVerticalStrut(14));
        botonEntrar.setAlignmentX(LEFT_ALIGNMENT);
        botonEntrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        p.add(botonEntrar);

        p.add(Box.createVerticalGlue());
        JLabel pie = Tema.texto("Farmacia SHEIBIS", new Color(0x94A3B8));
        pie.setAlignmentX(LEFT_ALIGNMENT);
        p.add(pie);
        return p;
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.fuente(Font.BOLD, 13f));
        l.setForeground(Tema.TEXTO);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    // ----- Logica de inicio de sesion -----
    private void entrar() {
        String usuario = campoUsuario.getText().trim();
        String clave = new String(campoClave.getPassword());

        if (usuario.isEmpty() || clave.isEmpty()) {
            mensaje.setText("Escriba su usuario y contrasena.");
            return;
        }

        botonEntrar.setEnabled(false);
        mensaje.setForeground(Tema.GRIS);
        mensaje.setText("Verificando...");

        Tarea.ejecutar(
                () -> Login.validar(usuario, clave),
                correcto -> {
                    botonEntrar.setEnabled(true);
                    if (correcto) {
                        Window ventana = SwingUtilities.getWindowAncestor(this);
                        new MainFrame().setVisible(true);
                        if (ventana != null) {
                            ventana.dispose();
                        }
                        return;
                    }
                    intentos++;
                    mensaje.setForeground(Tema.ROJO);
                    campoClave.setText("");
                    if (intentos >= 3) {
                        JOptionPane.showMessageDialog(this,
                                "Demasiados intentos fallidos. El programa se cerrara.",
                                "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                    }
                    mensaje.setText("Usuario o contrasena incorrectos. Intento "
                            + intentos + " de 3.");
                },
                error -> {
                    botonEntrar.setEnabled(true);
                    mensaje.setForeground(Tema.ROJO);
                    mensaje.setText("No se pudo conectar con la base de datos.");
                });
    }
}
