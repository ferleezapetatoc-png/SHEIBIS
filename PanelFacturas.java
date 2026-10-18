import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

// Facturacion: nueva venta e historial
public class PanelFacturas extends PanelBase {

    // Elemento de lista desplegable con un valor asociado
    private static class Opcion<T> {
        final T valor;
        final String texto;

        Opcion(T valor, String texto) {
            this.valor = valor;
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);
    private final Pestana pestVenta = new Pestana("Nueva venta");
    private final Pestana pestHistorial = new Pestana("Historial");

    // ----- Nueva venta -----
    private final JComboBox<Opcion<Integer>> comboCliente = new JComboBox<>();
    private final JComboBox<Opcion<Medicamento>> comboMedicamento = new JComboBox<>();
    private final Tema.Campo campoCantidad = new Tema.Campo("1");
    private final JLabel infoProducto = Tema.texto(" ", Tema.GRIS);
    private final DefaultTableModel modeloCarrito = new DefaultTableModel(
            new String[]{"Medicamento", "Cant.", "Precio", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int f, int c) {
            return false;
        }
    };
    private final JTable tablaCarrito = new JTable(modeloCarrito);
    private final JLabel etiquetaTotal = Tema.titulo("Q0.00", 30f);

    private final Factura venta = new Factura(0, "", null, null, 0, BigDecimal.ZERO);
    private final List<String> nombresCarrito = new ArrayList<>();
    private List<Medicamento> disponibles = new ArrayList<>();

    // ----- Historial -----
    private final DefaultTableModel modeloHistorial = new DefaultTableModel(
            new String[]{"ID", "Numero", "Fecha", "Cliente", "Atendio", "Total"}, 0) {
        @Override
        public boolean isCellEditable(int f, int c) {
            return false;
        }
    };
    private final JTable tablaHistorial = new JTable(modeloHistorial);
    private final TableRowSorter<DefaultTableModel> filtro =
            new TableRowSorter<>(modeloHistorial);
    private final Tema.Campo buscar = new Tema.Campo("Buscar por numero, cliente o fecha...");

    public PanelFacturas() {
        // ----- Encabezado con pestanas -----
        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);
        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(Tema.titulo("Facturas", 24f));
        textos.add(Tema.texto("Registre ventas y consulte las facturas emitidas", Tema.GRIS));
        cab.add(textos, BorderLayout.WEST);

        JPanel pestanas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pestanas.setOpaque(false);
        pestanas.add(pestVenta);
        pestanas.add(pestHistorial);
        cab.add(pestanas, BorderLayout.EAST);
        add(cab, BorderLayout.NORTH);

        pestVenta.addActionListener(e -> mostrarPestana(true));
        pestHistorial.addActionListener(e -> {
            mostrarPestana(false);
            cargarHistorial();
        });

        contenido.setOpaque(false);
        contenido.add(construirVenta(), "venta");
        contenido.add(construirHistorial(), "historial");
        add(contenido, BorderLayout.CENTER);

        mostrarPestana(true);
        actualizarCarrito();
    }

    private void mostrarPestana(boolean ventaActiva) {
        pestVenta.setActiva(ventaActiva);
        pestHistorial.setActiva(!ventaActiva);
        cartas.show(contenido, ventaActiva ? "venta" : "historial");
    }

    @Override
    public void alMostrar() {
        cargarDatosVenta();
        if (pestHistorial.activa) {
            cargarHistorial();
        }
    }

    // ================= NUEVA VENTA =================
    private JPanel construirVenta() {
        JPanel p = new JPanel(new BorderLayout(18, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18, 0, 0, 0));

        // ----- Izquierda: datos de la venta -----
        Tema.Tarjeta izq = new Tema.Tarjeta(new BorderLayout(0, 12));
        izq.setPreferredSize(new Dimension(400, 100));
        izq.add(Tema.titulo("Datos de la venta", 16f), BorderLayout.NORTH);

        Tema.estilizarCombo(comboCliente);
        Tema.estilizarCombo(comboMedicamento);
        comboMedicamento.addActionListener(e -> actualizarInfo());

        JPanel form = Tema.formulario(340,
                new String[]{"Cliente", "Medicamento", "Cantidad"},
                new JComponent[]{comboCliente, comboMedicamento, campoCantidad});
        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(form, BorderLayout.NORTH);

        infoProducto.setBorder(new EmptyBorder(0, 0, 10, 0));
        JPanel bajo = new JPanel(new BorderLayout(0, 10));
        bajo.setOpaque(false);
        bajo.add(infoProducto, BorderLayout.NORTH);
        Tema.Boton agregar = new Tema.Boton("Agregar a la factura", Tema.VERDE);
        agregar.addActionListener(e -> agregar());
        bajo.add(agregar, BorderLayout.SOUTH);
        centro.add(bajo, BorderLayout.CENTER);
        izq.add(centro, BorderLayout.CENTER);
        p.add(izq, BorderLayout.WEST);

        // ----- Derecha: carrito -----
        Tema.Tarjeta der = new Tema.Tarjeta(new BorderLayout(0, 12));
        der.add(Tema.titulo("Detalle de la factura", 16f), BorderLayout.NORTH);

        JScrollPane sp = Tema.tabla(tablaCarrito);
        tablaCarrito.getColumnModel().getColumn(1).setMaxWidth(80);
        Tema.anchos(tablaCarrito, 230, 60, 90, 100);
        der.add(sp, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        JPanel total = new JPanel(new GridLayout(2, 1));
        total.setOpaque(false);
        total.add(Tema.texto("TOTAL A PAGAR", Tema.GRIS));
        total.add(etiquetaTotal);
        pie.add(total, BorderLayout.WEST);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        Tema.Boton quitar = Tema.Boton.contorno("Quitar");
        quitar.addActionListener(e -> quitar());
        Tema.Boton vaciar = Tema.Boton.contorno("Vaciar");
        vaciar.addActionListener(e -> vaciar());
        Tema.Boton confirmar = new Tema.Boton("Confirmar venta", Tema.VERDE);
        confirmar.addActionListener(e -> confirmar());
        botones.add(quitar);
        botones.add(vaciar);
        botones.add(confirmar);
        JPanel botonesCentrados = new JPanel(new GridBagLayout());
        botonesCentrados.setOpaque(false);
        botonesCentrados.add(botones);
        pie.add(botonesCentrados, BorderLayout.EAST);
        der.add(pie, BorderLayout.SOUTH);

        p.add(der, BorderLayout.CENTER);
        return p;
    }

    private static class DatosVenta {
        List<Cliente> clientes;
        List<Medicamento> medicamentos;
    }

    private void cargarDatosVenta() {
        Tarea.ejecutar(() -> {
            DatosVenta d = new DatosVenta();
            d.clientes = ClienteDAO.listar();
            d.medicamentos = MedicamentoDAO.listar();
            return d;
        }, d -> cargarDatos(d.clientes, d.medicamentos),
                e -> error(this, mensajeBD(e)));
    }

    // Llena las listas desplegables (tambien se usa para pruebas)
    void cargarDatos(List<Cliente> clientes, List<Medicamento> medicamentos) {
        Integer clienteActual = valorCliente();
        comboCliente.removeAllItems();
        comboCliente.addItem(new Opcion<>(null, "Consumidor final"));
        int seleccion = 0;
        for (Cliente c : clientes) {
            comboCliente.addItem(new Opcion<>(c.getIdCliente(),
                    c.obtenerNombreCompleto() + "  (NIT " + c.getNit() + ")"));
            if (clienteActual != null && clienteActual == c.getIdCliente()) {
                seleccion = comboCliente.getItemCount() - 1;
            }
        }
        comboCliente.setSelectedIndex(seleccion);

        // Solo se pueden vender medicamentos con existencia y no vencidos
        disponibles = new ArrayList<>();
        comboMedicamento.removeAllItems();
        for (Medicamento m : medicamentos) {
            boolean vencido = m.getFechaVencimiento() != null
                    && m.getFechaVencimiento().isBefore(LocalDate.now());
            if (m.getExistencia() > 0 && !vencido) {
                disponibles.add(m);
                comboMedicamento.addItem(new Opcion<>(m, m.getNombre()
                        + "  -  Q" + m.getPrecioVenta().setScale(2)));
            }
        }
        actualizarInfo();
    }

    private Integer valorCliente() {
        Object o = comboCliente.getSelectedItem();
        return o == null ? null : ((Opcion<Integer>) o).valor;
    }

    private Medicamento medicamentoElegido() {
        Object o = comboMedicamento.getSelectedItem();
        return o == null ? null : ((Opcion<Medicamento>) o).valor;
    }

    private int enCarrito(int idMedicamento) {
        for (DetalleFactura d : venta.getDetalles()) {
            if (d.getIdMedicamento() == idMedicamento) {
                return d.getCantidad();
            }
        }
        return 0;
    }

    private void actualizarInfo() {
        Medicamento m = medicamentoElegido();
        if (m == null) {
            infoProducto.setText("No hay medicamentos disponibles para vender.");
            return;
        }
        infoProducto.setText("Disponible: " + (m.getExistencia() - enCarrito(m.getIdMedicamento()))
                + " unidades  |  Precio: Q" + m.getPrecioVenta().setScale(2));
    }

    private void agregar() {
        Medicamento m = medicamentoElegido();
        if (m == null) {
            aviso(this, "Seleccione un medicamento.");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(campoCantidad.getText().trim());
        } catch (NumberFormatException e) {
            cantidad = 0;
        }
        if (cantidad <= 0) {
            aviso(this, "La cantidad debe ser un numero entero mayor que 0.");
            return;
        }

        int yaAgregado = enCarrito(m.getIdMedicamento());

        try {
            m.verificarExistencia(yaAgregado + cantidad);
        } catch (ExistenciaInsuficienteException e) {
            aviso(this, e.getMessage());
            return;
        }

        boolean encontrado = false;
        for (DetalleFactura d : venta.getDetalles()) {
            if (d.getIdMedicamento() == m.getIdMedicamento()) {
                d.setCantidad(yaAgregado + cantidad);
                encontrado = true;
            }
        }
        if (!encontrado) {
            venta.agregarDetalle(new DetalleFactura(0, 0, m.getIdMedicamento(),
                    cantidad, m.getPrecioVenta()));
            nombresCarrito.add(m.getNombre());
        }

        campoCantidad.setText("1");
        actualizarCarrito();
    }

    private void quitar() {
        int fila = tablaCarrito.getSelectedRow();
        if (fila < 0) {
            aviso(this, "Seleccione un producto del detalle.");
            return;
        }
        venta.getDetalles().remove(fila);
        nombresCarrito.remove(fila);
        actualizarCarrito();
    }

    private void vaciar() {
        venta.getDetalles().clear();
        nombresCarrito.clear();
        actualizarCarrito();
    }

    private void actualizarCarrito() {
        modeloCarrito.setRowCount(0);
        List<DetalleFactura> detalles = venta.getDetalles();
        for (int i = 0; i < detalles.size(); i++) {
            DetalleFactura d = detalles.get(i);
            modeloCarrito.addRow(new Object[]{nombresCarrito.get(i),
                d.getCantidad(), "Q" + d.getPrecioUnitario().setScale(2),
                "Q" + d.calcularSubtotal().setScale(2)});
        }
        etiquetaTotal.setText("Q" + venta.calcularTotal().setScale(2));
        actualizarInfo();
    }

    private void confirmar() {
        if (venta.getDetalles().isEmpty()) {
            aviso(this, "Agregue al menos un producto a la factura.");
            return;
        }

        BigDecimal total = venta.calcularTotal();
        int r = JOptionPane.showConfirmDialog(this,
                "Cliente: " + comboCliente.getSelectedItem()
                + "\nProductos: " + venta.contarUnidades() + " unidades"
                + "\nTotal: Q" + total.setScale(2) + "\n\nConfirmar la venta?",
                "Confirmar venta", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }

        Integer idCliente = valorCliente();
        List<DetalleFactura> copia = new ArrayList<>(venta.getDetalles());

        Tarea.ejecutar(() -> FacturaDAO.guardar(idCliente, copia, total),
                numero -> {
                    JOptionPane.showMessageDialog(this,
                            "Factura " + numero + " guardada correctamente.",
                            "Venta registrada", JOptionPane.INFORMATION_MESSAGE);
                    vaciar();
                    cargarDatosVenta();
                },
                e -> error(this, "No se pudo guardar la factura. "
                        + "No se hizo ningun cambio.\n" + e.getMessage()));
    }

    // ================= HISTORIAL =================
    private JPanel construirHistorial() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18, 0, 0, 0));
        p.add(buscar, BorderLayout.NORTH);

        tablaHistorial.setRowSorter(filtro);
        JScrollPane sp = Tema.tabla(tablaHistorial);
        tablaHistorial.getColumnModel().getColumn(0).setMaxWidth(60);
        Tema.anchos(tablaHistorial, 50, 260, 150, 180, 170, 90);
        p.add(sp, BorderLayout.CENTER);

        buscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        tablaHistorial.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    verDetalle();
                }
            }
        });

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);
        Tema.Boton ver = new Tema.Boton("Ver detalle", Tema.VERDE);
        ver.addActionListener(e -> verDetalle());
        acciones.add(ver);
        p.add(acciones, BorderLayout.SOUTH);
        return p;
    }

    private void cargarHistorial() {
        Tarea.ejecutar(FacturaDAO::historial, this::mostrarHistorial,
                e -> error(this, mensajeBD(e)));
    }

    // Llena el historial (tambien se usa para pruebas)
    void mostrarHistorial(List<Object[]> filas) {
        modeloHistorial.setRowCount(0);
        for (Object[] f : filas) {
            modeloHistorial.addRow(f);
        }
    }

    private void filtrar() {
        String t = buscar.getText().trim();
        filtro.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter(
                "(?i)" + java.util.regex.Pattern.quote(t), 1, 2, 3));
    }

    private void verDetalle() {
        int fila = tablaHistorial.getSelectedRow();
        if (fila < 0) {
            aviso(this, "Seleccione una factura de la tabla.");
            return;
        }
        int modelFila = tablaHistorial.convertRowIndexToModel(fila);
        int id = (int) modeloHistorial.getValueAt(modelFila, 0);
        String numero = String.valueOf(modeloHistorial.getValueAt(modelFila, 1));
        String cliente = String.valueOf(modeloHistorial.getValueAt(modelFila, 3));
        String total = String.valueOf(modeloHistorial.getValueAt(modelFila, 5));

        Tarea.ejecutar(() -> FacturaDAO.detalle(id),
                filas -> mostrarDetalle(numero, cliente, total, filas),
                e -> error(this, mensajeBD(e)));
    }

    void mostrarDetalle(String numero, String cliente, String total,
                        List<Object[]> filas) {
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"Medicamento", "Cant.", "Precio", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int f, int c) {
                return false;
            }
        };
        for (Object[] f : filas) {
            m.addRow(f);
        }
        JTable t = new JTable(m);
        JScrollPane sp = Tema.tabla(t);
        t.getColumnModel().getColumn(1).setMaxWidth(70);
        Tema.anchos(t, 230, 60, 90, 100);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setPreferredSize(new Dimension(520, 320));
        JPanel datos = new JPanel(new GridLayout(2, 1));
        datos.add(Tema.titulo(numero, 16f));
        datos.add(Tema.texto("Cliente: " + cliente, Tema.GRIS));
        panel.add(datos, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        JLabel tot = Tema.titulo("Total: " + total, 18f);
        tot.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(tot, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "Detalle de la factura",
                JOptionPane.PLAIN_MESSAGE);
    }

    // ----- Pestana con linea inferior -----
    private static class Pestana extends JButton {

        boolean activa = false;

        Pestana(String texto) {
            super(texto);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(8, 18, 10, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setActiva(false);
        }

        void setActiva(boolean a) {
            activa = a;
            setForeground(a ? Tema.VERDE_OSCURO : Tema.GRIS);
            setFont(Tema.fuente(a ? Font.BOLD : Font.PLAIN, 14f));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (activa) {
                Graphics2D g2 = Tema.suave(g);
                g2.setColor(Tema.VERDE);
                g2.fillRoundRect(10, getHeight() - 4, getWidth() - 20, 3, 3, 3);
                g2.dispose();
            }
        }
    }
}
