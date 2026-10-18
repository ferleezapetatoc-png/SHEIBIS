import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

// Los 5 reportes del sistema en pantalla
public class PanelReportes extends PanelBase {

    private static final String[] NOMBRES = {
        "Poca existencia",
        "Vencidos o por vencer",
        "Ventas del dia",
        "Mas vendidos",
        "Valor del inventario"
    };
    private static final String[] DESCRIPCIONES = {
        "Medicamentos con pocas unidades en existencia",
        "Medicamentos vencidos o que vencen pronto",
        "Facturas emitidas y total vendido hoy",
        "Los 5 medicamentos con mas unidades vendidas",
        "Valor del inventario a precio de compra y de venta"
    };
    // Los reportes de dinero (3, 4 y 5) solo los ve un administrador
    private static final boolean[] SOLO_ADMIN = {false, false, true, true, true};

    private final JButton[] botones = new JButton[NOMBRES.length];
    private int actual = -1;

    private final JLabel titulo = Tema.titulo(" ", 20f);
    private final JLabel descripcion = Tema.texto(" ", Tema.GRIS);
    private final JLabel etiquetaParametro = Tema.texto(" ", Tema.GRIS);
    private final Tema.Campo campoParametro = new Tema.Campo("");
    private final JPanel filaParametro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

    private final CardLayout cartas = new CardLayout();
    private final JPanel resultado = new JPanel(cartas);
    private final JTable tabla = new JTable(new DefaultTableModel());
    private final JPanel resumen = new JPanel(new GridLayout(1, 3, 16, 0));
    private final JLabel mensajeVacio = new JLabel(" ", SwingConstants.CENTER);

    public PanelReportes() {
        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(Tema.titulo("Reportes", 24f));
        textos.add(Tema.texto("Consulte el estado del inventario y las ventas", Tema.GRIS));
        add(textos, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(18, 0));
        centro.setOpaque(false);
        centro.add(construirLista(), BorderLayout.WEST);
        centro.add(construirResultado(), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        seleccionar(0);
    }

    // ----- Lista de reportes (izquierda) -----
    private JPanel construirLista() {
        Tema.Tarjeta t = new Tema.Tarjeta(new BorderLayout());
        t.setPreferredSize(new Dimension(290, 100));
        t.setBorder(new EmptyBorder(14, 12, 14, 12));

        JPanel lista = new JPanel();
        lista.setOpaque(false);
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));

        for (int i = 0; i < NOMBRES.length; i++) {
            final int indice = i;
            botones[i] = new ItemReporte(NOMBRES[i], SOLO_ADMIN[i]);
            botones[i].setAlignmentX(LEFT_ALIGNMENT);
            botones[i].setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
            botones[i].setPreferredSize(new Dimension(250, 54));
            botones[i].addActionListener(e -> seleccionar(indice));
            lista.add(botones[i]);
            lista.add(Box.createVerticalStrut(6));
        }
        t.add(lista, BorderLayout.NORTH);
        return t;
    }

    // ----- Resultado (derecha) -----
    private JPanel construirResultado() {
        Tema.Tarjeta t = new Tema.Tarjeta(new BorderLayout(0, 14));

        JPanel cab = new JPanel(new BorderLayout(0, 4));
        cab.setOpaque(false);
        cab.add(titulo, BorderLayout.NORTH);
        cab.add(descripcion, BorderLayout.CENTER);

        filaParametro.setOpaque(false);
        filaParametro.setBorder(new EmptyBorder(8, 0, 0, 0));
        campoParametro.setPreferredSize(new Dimension(90, 40));
        Tema.Boton generar = new Tema.Boton("Generar", Tema.VERDE);
        generar.addActionListener(e -> generar());
        campoParametro.addActionListener(e -> generar());
        filaParametro.add(etiquetaParametro);
        filaParametro.add(campoParametro);
        filaParametro.add(generar);
        cab.add(filaParametro, BorderLayout.SOUTH);
        t.add(cab, BorderLayout.NORTH);

        resultado.setOpaque(false);
        resultado.add(Tema.tabla(tabla), "tabla");
        resumen.setOpaque(false);
        JPanel contenedorResumen = new JPanel(new BorderLayout());
        contenedorResumen.setOpaque(false);
        contenedorResumen.add(resumen, BorderLayout.NORTH);
        resultado.add(contenedorResumen, "resumen");
        mensajeVacio.setFont(Tema.fuente(Font.PLAIN, 15f));
        mensajeVacio.setForeground(Tema.GRIS);
        resultado.add(mensajeVacio, "mensaje");
        t.add(resultado, BorderLayout.CENTER);
        return t;
    }

    @Override
    public void alMostrar() {
        generar();
    }

    private void seleccionar(int indice) {
        actual = indice;
        for (int i = 0; i < botones.length; i++) {
            ((ItemReporte) botones[i]).setActivo(i == indice);
        }
        titulo.setText(NOMBRES[indice]);
        descripcion.setText(DESCRIPCIONES[indice]);

        filaParametro.setVisible(indice == 0 || indice == 1);
        if (indice == 0) {
            etiquetaParametro.setText("Existencia menor o igual a:");
            campoParametro.setText("10");
        } else if (indice == 1) {
            etiquetaParametro.setText("Vencen en los proximos (dias):");
            campoParametro.setText("30");
        }
        generar();
    }

    private void generar() {
        if (actual < 0) {
            return;
        }
        if (SOLO_ADMIN[actual] && !Sesion.esAdministrador()) {
            mostrarMensaje("Acceso denegado: este reporte es solo para administradores.");
            return;
        }

        final int reporte = actual;
        int numero = 0;
        if (reporte == 0 || reporte == 1) {
            try {
                numero = Integer.parseInt(campoParametro.getText().trim());
                if (numero < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                aviso(this, "Escriba un numero entero igual o mayor que 0.");
                return;
            }
        }
        final int n = numero;

        switch (reporte) {
            case 0:
                Tarea.ejecutar(() -> ReportesDAO.pocaExistencia(n),
                        f -> mostrarPocaExistencia(f, n), this::fallo);
                break;
            case 1:
                Tarea.ejecutar(() -> ReportesDAO.porVencer(n),
                        f -> mostrarPorVencer(f, n), this::fallo);
                break;
            case 2:
                Tarea.ejecutar(ReportesDAO::ventasDelDia,
                        this::mostrarVentas, this::fallo);
                break;
            case 3:
                Tarea.ejecutar(ReportesDAO::masVendidos,
                        this::mostrarMasVendidos, this::fallo);
                break;
            default:
                Tarea.ejecutar(ReportesDAO::valorInventario,
                        this::mostrarInventario, this::fallo);
        }
    }

    private void fallo(Exception e) {
        mostrarMensaje("No se pudo generar el reporte.");
        error(this, mensajeBD(e));
    }

    // ================= Presentacion de cada reporte =================
    void mostrarPocaExistencia(List<Object[]> filas, int limite) {
        if (filas.isEmpty()) {
            mostrarMensaje("Ningun medicamento con " + limite + " unidades o menos.");
            return;
        }
        llenarTabla(new String[]{"ID", "Medicamento", "Existencia"}, filas);
        Tema.anchos(tabla, 60, 360, 120);
        tabla.getColumnModel().getColumn(0).setMaxWidth(70);
        tabla.getColumnModel().getColumn(2).setCellRenderer(new CeldaColor(2));
    }

    void mostrarPorVencer(List<Object[]> filas, int dias) {
        if (filas.isEmpty()) {
            mostrarMensaje("No hay medicamentos vencidos ni por vencer en " + dias + " dias.");
            return;
        }
        llenarTabla(new String[]{"ID", "Medicamento", "Existencia", "Vence", "Estado"}, filas);
        Tema.anchos(tabla, 60, 260, 100, 120, 120);
        tabla.getColumnModel().getColumn(0).setMaxWidth(70);
        tabla.getColumnModel().getColumn(4).setCellRenderer(new CeldaColor(4));
    }

    void mostrarVentas(Object[] datos) {
        resumen.removeAll();
        resumen.setLayout(new GridLayout(1, 2, 16, 0));
        resumen.add(tile("Facturas emitidas hoy", String.valueOf(datos[0]), Tema.AZUL));
        resumen.add(tile("Total vendido hoy",
                "Q" + ((BigDecimal) datos[1]).setScale(2), Tema.VERDE));
        cartas.show(resultado, "resumen");
        resumen.revalidate();
        resumen.repaint();
    }

    void mostrarMasVendidos(List<Object[]> filas) {
        if (filas.isEmpty()) {
            mostrarMensaje("Aun no hay ventas registradas.");
            return;
        }
        llenarTabla(new String[]{"#", "Medicamento", "Unidades vendidas"}, filas);
        Tema.anchos(tabla, 50, 240, 300);
        tabla.getColumnModel().getColumn(0).setMaxWidth(60);
        int maximo = 1;
        for (Object[] f : filas) {
            maximo = Math.max(maximo, (Integer) f[2]);
        }
        tabla.getColumnModel().getColumn(2).setCellRenderer(new CeldaBarra(maximo));
    }

    void mostrarInventario(Object[] datos) {
        BigDecimal compra = (BigDecimal) datos[1];
        BigDecimal venta = (BigDecimal) datos[2];
        resumen.removeAll();
        resumen.setLayout(new GridLayout(2, 2, 16, 16));
        resumen.add(tile("Unidades en existencia", String.valueOf(datos[0]), Tema.AZUL));
        resumen.add(tile("Valor a precio de compra", "Q" + compra.setScale(2), Tema.AMBAR));
        resumen.add(tile("Valor a precio de venta", "Q" + venta.setScale(2), Tema.VERDE));
        resumen.add(tile("Ganancia estimada",
                "Q" + venta.subtract(compra).setScale(2), new Color(0x8B5CF6)));
        cartas.show(resultado, "resumen");
        resumen.revalidate();
        resumen.repaint();
    }

    private void llenarTabla(String[] columnas, List<Object[]> filas) {
        DefaultTableModel m = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int f, int c) {
                return false;
            }
        };
        for (Object[] f : filas) {
            m.addRow(f);
        }
        tabla.setModel(m);
        cartas.show(resultado, "tabla");
    }

    private void mostrarMensaje(String texto) {
        mensajeVacio.setText(texto);
        cartas.show(resultado, "mensaje");
    }

    private JPanel tile(String etiqueta, String valor, Color color) {
        JPanel p = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Tema.suave(g);
                g2.setColor(new Color(0xF8FAFC));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(Tema.BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(color);
                g2.fillRoundRect(0, 18, 5, getHeight() - 36, 5, 5);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18, 22, 18, 18));
        p.setPreferredSize(new Dimension(100, 110));
        p.add(Tema.texto(etiqueta, Tema.GRIS), BorderLayout.NORTH);
        p.add(Tema.titulo(valor, 28f), BorderLayout.CENTER);
        return p;
    }

    // ----- Boton de la lista de reportes -----
    private static class ItemReporte extends JButton {

        private boolean activo = false;
        private boolean encima = false;
        private final boolean soloAdmin;

        ItemReporte(String texto, boolean soloAdmin) {
            super(texto);
            this.soloAdmin = soloAdmin;
            setHorizontalAlignment(LEFT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(0, 16, 0, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setActivo(false);
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    encima = false;
                    repaint();
                }
            });
        }

        void setActivo(boolean a) {
            activo = a;
            setForeground(a ? Tema.VERDE_OSCURO : Tema.TEXTO);
            setFont(Tema.fuente(a ? Font.BOLD : Font.PLAIN, 14f));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suave(g);
            if (activo) {
                g2.setColor(Tema.VERDE_CLARO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            } else if (encima) {
                g2.setColor(new Color(0xF1F5F9));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
            if (soloAdmin) {
                g2.setFont(Tema.fuente(Font.PLAIN, 11f));
                g2.setColor(Tema.GRIS);
                String s = "Admin";
                g2.drawString(s, getWidth() - g2.getFontMetrics().stringWidth(s) - 12,
                        getHeight() / 2 + 4);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ----- Colores de alerta en celdas -----
    private static class CeldaColor extends DefaultTableCellRenderer {
        private final int columna;

        CeldaColor(int columna) {
            this.columna = columna;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor,
                boolean sel, boolean foco, int fila, int col) {
            super.getTableCellRendererComponent(t, valor, sel, foco, fila, col);
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setHorizontalAlignment(LEFT);
            if (!sel) {
                setBackground(fila % 2 == 0 ? Tema.BLANCO : new Color(0xF8FAFC));
            }
            setForeground(Tema.TEXTO);
            setFont(Tema.fuente(Font.PLAIN, 13f));

            String texto = String.valueOf(valor);
            if ("VENCIDO".equals(texto)) {
                setForeground(Tema.ROJO);
                setFont(Tema.fuente(Font.BOLD, 13f));
            } else if ("Por vencer".equals(texto)) {
                setForeground(new Color(0xD97706));
                setFont(Tema.fuente(Font.BOLD, 13f));
            } else if (columna == 2 && valor instanceof Integer) {
                int unidades = (Integer) valor;
                if (unidades == 0) {
                    setForeground(Tema.ROJO);
                    setFont(Tema.fuente(Font.BOLD, 13f));
                } else if (unidades <= 10) {
                    setForeground(new Color(0xD97706));
                    setFont(Tema.fuente(Font.BOLD, 13f));
                }
            }
            return this;
        }
    }

    // ----- Barra proporcional para "mas vendidos" -----
    private static class CeldaBarra extends JPanel implements javax.swing.table.TableCellRenderer {
        private final int maximo;
        private int valor;
        private boolean par;

        CeldaBarra(int maximo) {
            this.maximo = maximo;
            setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foco, int fila, int col) {
            valor = (Integer) v;
            par = fila % 2 == 0;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suave(g);
            g2.setColor(par ? Tema.BLANCO : new Color(0xF8FAFC));
            g2.fillRect(0, 0, getWidth(), getHeight());

            int espacioTexto = 60;
            int ancho = getWidth() - 28 - espacioTexto;
            int largo = Math.max(6, (int) ((double) valor / maximo * ancho));
            g2.setColor(Tema.VERDE);
            g2.fillRoundRect(14, getHeight() / 2 - 7, largo, 14, 14, 14);

            g2.setColor(Tema.TEXTO);
            g2.setFont(Tema.fuente(Font.BOLD, 13f));
            g2.drawString(String.valueOf(valor), 14 + largo + 10,
                    getHeight() / 2 + 5);
            g2.dispose();
        }
    }
}
