import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;

// Colores, fuentes y componentes con estilo propio (sin librerias externas)
public class Tema {

    // ----- Paleta -----
    public static final Color VERDE = new Color(0x10B981);
    public static final Color VERDE_OSCURO = new Color(0x059669);
    public static final Color VERDE_CLARO = new Color(0xD1FAE5);
    public static final Color NOCHE = new Color(0x0F172A);
    public static final Color NOCHE_2 = new Color(0x1E293B);
    public static final Color FONDO = new Color(0xF1F5F9);
    public static final Color BLANCO = Color.WHITE;
    public static final Color TEXTO = new Color(0x0F172A);
    public static final Color GRIS = new Color(0x64748B);
    public static final Color BORDE = new Color(0xE2E8F0);
    public static final Color ROJO = new Color(0xEF4444);
    public static final Color AZUL = new Color(0x3B82F6);
    public static final Color AMBAR = new Color(0xF59E0B);

    // ----- Fuentes -----
    public static final String FAMILIA = elegirFamilia();

    private static String elegirFamilia() {
        String[] preferidas = {"Segoe UI", "Helvetica Neue", "Inter", "Poppins"};
        java.util.Set<String> disponibles = new java.util.HashSet<>(
                java.util.Arrays.asList(GraphicsEnvironment
                        .getLocalGraphicsEnvironment()
                        .getAvailableFontFamilyNames()));
        for (String f : preferidas) {
            if (disponibles.contains(f)) {
                return f;
            }
        }
        return Font.SANS_SERIF;
    }

    public static Font fuente(int estilo, float tam) {
        return new Font(FAMILIA, estilo, 1).deriveFont(estilo, tam);
    }

    // Ponga en false para usar solo el estilo propio (sin FlatLaf)
    public static final boolean USAR_FLATLAF = true;

    public static void iniciar() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        if (USAR_FLATLAF) {
            activarFlatLaf();
        }
        UIManager.put("OptionPane.messageFont", fuente(Font.PLAIN, 14f));
        UIManager.put("OptionPane.buttonFont", fuente(Font.BOLD, 13f));
        UIManager.put("ToolTip.font", fuente(Font.PLAIN, 12f));
    }

    // Activa FlatLaf (lib/flatlaf-3.7.2.jar) con el verde de la farmacia.
    // Si el .jar no esta, el programa sigue funcionando con el estilo propio.
    private static void activarFlatLaf() {
        try {
            Class<?> flat = Class.forName("com.formdev.flatlaf.FlatLaf");
            flat.getMethod("setGlobalExtraDefaults", java.util.Map.class)
                    .invoke(null, java.util.Map.of("@accentColor", "#10B981"));

            Class<?> claro = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            claro.getMethod("setup").invoke(null);

            UIManager.put("defaultFont", fuente(Font.PLAIN, 13f));
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("ScrollBar.width", 12);
        } catch (Throwable e) {
            System.out.println("FlatLaf no disponible; se usa el estilo propio.");
        }
    }

    static Graphics2D suave(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    // ----- Etiquetas -----
    public static JLabel titulo(String texto, float tam) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(Font.BOLD, tam));
        l.setForeground(TEXTO);
        return l;
    }

    public static JLabel texto(String texto, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(Font.PLAIN, 13f));
        l.setForeground(color);
        return l;
    }

    // ----- Boton redondeado con efecto al pasar el mouse -----
    public static class Boton extends JButton {

        private final Color base;
        private final boolean contorno;
        private boolean encima = false;

        public Boton(String texto, Color base) {
            this(texto, base, false);
        }

        public static Boton contorno(String texto) {
            return new Boton(texto, GRIS, true);
        }

        private Boton(String texto, Color base, boolean contorno) {
            super(texto);
            this.base = base;
            this.contorno = contorno;
            setFont(fuente(Font.BOLD, 13f));
            setForeground(contorno ? TEXTO : BLANCO);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 20, 10, 20));
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

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            int w = getWidth();
            int h = getHeight();

            if (contorno) {
                g2.setColor(encima ? FONDO : BLANCO);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 12, 12));
                g2.setColor(BORDE);
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 12, 12));
            } else {
                Color c = base;
                if (!isEnabled()) {
                    c = new Color(0xCBD5E1);
                } else if (getModel().isPressed()) {
                    c = base.darker().darker();
                } else if (encima) {
                    c = base.darker();
                }
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ----- Campos de texto -----
    private static void pintarCampo(Graphics g, JTextComponent c, String pista) {
        Graphics2D g2 = suave(g);
        int w = c.getWidth();
        int h = c.getHeight();

        g2.setColor(BLANCO);
        g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 12, 12));
        g2.setColor(c.hasFocus() ? VERDE : BORDE);
        g2.setStroke(new BasicStroke(c.hasFocus() ? 2f : 1f));
        g2.draw(new RoundRectangle2D.Float(1, 1, w - 3, h - 3, 12, 12));

        if (pista != null && c.getText().isEmpty() && !c.hasFocus()) {
            g2.setFont(c.getFont());
            g2.setColor(new Color(0x94A3B8));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(pista, c.getInsets().left,
                    (h - fm.getHeight()) / 2 + fm.getAscent());
        }
        g2.dispose();
    }

    private static void estiloCampo(JTextComponent c) {
        c.setOpaque(false);
        c.setFont(fuente(Font.PLAIN, 14f));
        c.setForeground(TEXTO);
        c.setCaretColor(VERDE_OSCURO);
        c.setBorder(new EmptyBorder(10, 14, 10, 14));
        c.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                c.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                c.repaint();
            }
        });
    }

    public static class Campo extends JTextField {

        private final String pista;

        public Campo(String pista) {
            this.pista = pista;
            estiloCampo(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            pintarCampo(g, this, pista);
            super.paintComponent(g);
        }
    }

    public static class Clave extends JPasswordField {

        private final String pista;

        public Clave(String pista) {
            this.pista = pista;
            estiloCampo(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            pintarCampo(g, this, pista);
            super.paintComponent(g);
        }
    }

    // ----- Tarjeta blanca con borde suave -----
    public static class Tarjeta extends JPanel {

        public Tarjeta(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBorder(new EmptyBorder(18, 20, 18, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            g2.setColor(BLANCO);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1,
                    getHeight() - 1, 18, 18));
            g2.setColor(BORDE);
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1,
                    getHeight() - 1, 18, 18));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ----- Lista desplegable -----
    public static void estilizarCombo(JComboBox<?> c) {
        c.setFont(fuente(Font.PLAIN, 14f));
        c.setBackground(BLANCO);
        c.setForeground(TEXTO);
        c.setPreferredSize(new Dimension(100, 42));
    }

    // ----- Formulario: etiqueta arriba y campo abajo -----
    public static JPanel formulario(int ancho, String[] etiquetas,
                                    JComponent[] campos) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        for (int i = 0; i < campos.length; i++) {
            JLabel l = new JLabel(etiquetas[i]);
            l.setFont(fuente(Font.BOLD, 12f));
            l.setForeground(GRIS);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            campos[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            campos[i].setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            p.add(l);
            p.add(Box.createVerticalStrut(4));
            p.add(campos[i]);
            p.add(Box.createVerticalStrut(10));
        }
        p.setPreferredSize(new Dimension(ancho, campos.length * 76));
        return p;
    }

    // Anchos de columna (el primero que sobre se reparte solo)
    public static void anchos(JTable t, int... anchos) {
        for (int i = 0; i < anchos.length && i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    // ----- Tablas -----
    public static JScrollPane tabla(JTable t) {
        t.setRowHeight(40);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFont(fuente(Font.PLAIN, 13f));
        t.setForeground(TEXTO);
        t.setSelectionBackground(VERDE_CLARO);
        t.setSelectionForeground(TEXTO);
        t.setFillsViewportHeight(true);
        t.setFocusable(false);
        t.setDefaultEditor(Object.class, null);

        DefaultTableCellRenderer celda = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tabla,
                    Object valor, boolean sel, boolean foco, int fila, int col) {
                super.getTableCellRendererComponent(tabla, valor, sel, foco,
                        fila, col);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                if (!sel) {
                    setBackground(fila % 2 == 0 ? BLANCO : new Color(0xF8FAFC));
                }
                return this;
            }
        };
        t.setDefaultRenderer(Object.class, celda);
        t.setDefaultRenderer(Number.class, celda);
        t.setDefaultRenderer(Integer.class, celda);

        JTableHeader cab = t.getTableHeader();
        cab.setReorderingAllowed(false);
        cab.setPreferredSize(new Dimension(100, 42));
        cab.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tabla,
                    Object valor, boolean sel, boolean foco, int fila, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tabla,
                        valor, false, false, fila, col);
                l.setText(String.valueOf(valor).toUpperCase());
                l.setFont(fuente(Font.BOLD, 11f));
                l.setForeground(GRIS);
                l.setBackground(new Color(0xF8FAFC));
                l.setBorder(new CompoundBorder(
                        new MatteBorder(0, 0, 1, 0, BORDE),
                        new EmptyBorder(0, 14, 0, 14)));
                return l;
            }
        });

        JScrollPane sp = new JScrollPane(t);
        sp.setColumnHeaderView(t.getTableHeader());
        sp.setBorder(new LineBorder(BORDE, 1, true));
        sp.getViewport().setBackground(BLANCO);
        return sp;
    }
}
