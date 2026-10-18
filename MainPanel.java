import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// Ventana principal: menu lateral + contenido
public class MainPanel extends JPanel {

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);
    private final Map<String, PanelBase> paneles = new LinkedHashMap<>();
    private final Map<String, NavBoton> botones = new LinkedHashMap<>();
    private final JLabel tituloSeccion = Tema.titulo(" ", 14f);

    public MainPanel() {
        super(new BorderLayout());
        add(barraLateral(), BorderLayout.WEST);

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.setBackground(Tema.FONDO);
        derecha.add(barraSuperior(), BorderLayout.NORTH);

        agregar("Inicio", new PanelInicio());
        agregar("Clientes", new PanelClientes());
        agregar("Medicamentos", new PanelMedicamentos());
        agregar("Facturas", new PanelFacturas());
        agregar("Reportes", new PanelReportes());
        if (Sesion.esAdministrador()) {
            agregar("Proveedores", new PanelProveedores());
            agregar("Empleados", new PanelEmpleados());
        }
        agregar("Pilares POO", new PanelPOO());

        derecha.add(contenido, BorderLayout.CENTER);
        add(derecha, BorderLayout.CENTER);
    }

    private void agregar(String nombre, PanelBase panel) {
        paneles.put(nombre, panel);
        contenido.add(panel, nombre);
    }

    void ir(String nombre) {
        for (Map.Entry<String, NavBoton> e : botones.entrySet()) {
            e.getValue().setActivo(e.getKey().equals(nombre));
        }
        tituloSeccion.setText(nombre);
        cartas.show(contenido, nombre);
        paneles.get(nombre).alMostrar();
    }

    // ----- Barra lateral -----
    private JPanel barraLateral() {
        JPanel p = new JPanel();
        p.setBackground(Tema.NOCHE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(250, 100));
        p.setBorder(new EmptyBorder(26, 16, 20, 16));

        JLabel marca = new JLabel("SHEIBIS");
        marca.setFont(Tema.fuente(Font.BOLD, 24f));
        marca.setForeground(Color.WHITE);
        marca.setAlignmentX(LEFT_ALIGNMENT);
        marca.setBorder(new EmptyBorder(0, 10, 0, 0));
        JLabel sub = new JLabel("Farmacia");
        sub.setFont(Tema.fuente(Font.PLAIN, 13f));
        sub.setForeground(Tema.VERDE);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        sub.setBorder(new EmptyBorder(0, 10, 0, 0));
        p.add(marca);
        p.add(sub);
        p.add(Box.createVerticalStrut(30));

        String[] orden = {"Inicio", "Clientes", "Medicamentos", "Facturas",
            "Reportes", "Proveedores", "Empleados", "Pilares POO"};
        for (String nombre : orden) {
            boolean soloAdmin = nombre.equals("Proveedores")
                    || nombre.equals("Empleados");
            if (soloAdmin && !Sesion.esAdministrador()) {
                continue;
            }
            NavBoton b = new NavBoton(nombre);
            b.setAlignmentX(LEFT_ALIGNMENT);
            b.setPreferredSize(new Dimension(218, 44));
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            b.addActionListener(e -> ir(nombre));
            botones.put(nombre, b);
            p.add(b);
            p.add(Box.createVerticalStrut(4));
        }

        p.add(Box.createVerticalGlue());
        p.add(tarjetaUsuario());
        return p;
    }

    private JPanel tarjetaUsuario() {
        JPanel t = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Tema.suave(g);
                g2.setColor(Tema.NOCHE_2);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(),
                        getHeight(), 16, 16));
                g2.dispose();
            }
        };
        t.setOpaque(false);
        t.setBorder(new EmptyBorder(14, 14, 14, 14));
        t.setAlignmentX(LEFT_ALIGNMENT);
        t.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel nombre = new JLabel(Sesion.nombre.isEmpty() ? "Usuario" : Sesion.nombre);
        nombre.setFont(Tema.fuente(Font.BOLD, 14f));
        nombre.setForeground(Color.WHITE);
        JLabel rol = new JLabel(Sesion.rol.isEmpty() ? "-" : Sesion.rol);
        rol.setFont(Tema.fuente(Font.PLAIN, 12f));
        rol.setForeground(new Color(0x94A3B8));
        JPanel datos = new JPanel(new GridLayout(2, 1));
        datos.setOpaque(false);
        datos.add(nombre);
        datos.add(rol);
        t.add(datos, BorderLayout.NORTH);

        Tema.Boton salir = new Tema.Boton("Cerrar sesion", Tema.ROJO);
        salir.addActionListener(e -> cerrarSesion());
        t.add(salir, BorderLayout.SOUTH);
        return t;
    }

    private void cerrarSesion() {
        int r = JOptionPane.showConfirmDialog(this, "Desea cerrar la sesion?",
                "Cerrar sesion", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            Sesion.idEmpleado = 0;
            Sesion.nombre = "";
            Sesion.rol = "";
            Window ventana = SwingUtilities.getWindowAncestor(this);
            new LoginFrame().setVisible(true);
            if (ventana != null) {
                ventana.dispose();
            }
        }
    }

    // ----- Barra superior -----
    private JPanel barraSuperior() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE),
                new EmptyBorder(16, 30, 16, 30)));
        tituloSeccion.setForeground(Tema.GRIS);
        p.add(tituloSeccion, BorderLayout.WEST);
        p.add(Tema.texto(LocalDate.now().toString(), Tema.GRIS), BorderLayout.EAST);
        return p;
    }

    // ----- Boton del menu lateral -----
    static class NavBoton extends JButton {

        private boolean activo = false;
        private boolean encima = false;

        NavBoton(String texto) {
            super(texto);
            setFont(Tema.fuente(Font.PLAIN, 14f));
            setForeground(new Color(0xCBD5E1));
            setHorizontalAlignment(LEFT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(0, 24, 0, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }
            });
        }

        void setActivo(boolean a) {
            activo = a;
            setForeground(a ? Color.WHITE : new Color(0xCBD5E1));
            setFont(Tema.fuente(a ? Font.BOLD : Font.PLAIN, 14f));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suave(g);
            if (activo) {
                g2.setColor(new Color(0x10B981, false));
                g2.setColor(new Color(16, 185, 129, 50));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(),
                        getHeight(), 12, 12));
                g2.setColor(Tema.VERDE);
                g2.fill(new RoundRectangle2D.Float(0, 10, 4,
                        getHeight() - 20, 4, 4));
            } else if (encima) {
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(),
                        getHeight(), 12, 12));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
