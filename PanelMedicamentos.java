import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

// CRUD de medicamentos en pantalla
public class PanelMedicamentos extends PanelBase {

    private static final int COL_EXISTENCIA = 5;
    private static final int COL_VENCE = 6;

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Nombre", "Descripcion", "Compra",
                "Venta", "Existencia", "Vence", "Proveedor"}, 0) {
        @Override
        public boolean isCellEditable(int f, int c) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> filtro = new TableRowSorter<>(modelo);
    private final Tema.Campo buscar = new Tema.Campo("Buscar por nombre o descripcion...");
    private final JLabel contador = Tema.texto(" ", Tema.GRIS);

    private List<Medicamento> medicamentos = new java.util.ArrayList<>();
    private Map<Integer, String> proveedores = new LinkedHashMap<>();

    public PanelMedicamentos() {
        // ----- Encabezado -----
        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);
        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(Tema.titulo("Medicamentos", 24f));
        textos.add(contador);
        cab.add(textos, BorderLayout.WEST);

        Tema.Boton nuevo = new Tema.Boton("+  Nuevo medicamento", Tema.VERDE);
        nuevo.addActionListener(e -> nuevo());
        JPanel derecha = new JPanel(new GridBagLayout());
        derecha.setOpaque(false);
        derecha.add(nuevo);
        cab.add(derecha, BorderLayout.EAST);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.setBorder(new EmptyBorder(14, 0, 0, 0));
        barra.add(buscar, BorderLayout.CENTER);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(cab, BorderLayout.NORTH);
        norte.add(barra, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        // ----- Tabla -----
        tabla.setRowSorter(filtro);
        filtro.setSortable(3, false);
        filtro.setSortable(4, false);
        JScrollPane sp = Tema.tabla(tabla);
        tabla.getColumnModel().getColumn(0).setMaxWidth(60);
        Tema.anchos(tabla, 50, 170, 170, 90, 90, 120, 120, 150);
        tabla.getColumnModel().getColumn(COL_EXISTENCIA)
                .setCellRenderer(new CeldaAlerta());
        tabla.getColumnModel().getColumn(COL_VENCE)
                .setCellRenderer(new CeldaAlerta());
        add(sp, BorderLayout.CENTER);

        buscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editar();
                }
            }
        });

        // ----- Leyenda y acciones -----
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.add(Tema.texto("Existencia en ambar: 10 o menos  |  en rojo: agotado.  "
                + "Fecha en ambar: vence en 30 dias  |  en rojo: vencido.",
                Tema.GRIS), BorderLayout.WEST);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);
        Tema.Boton editar = Tema.Boton.contorno("Editar");
        editar.addActionListener(e -> editar());
        Tema.Boton eliminar = new Tema.Boton("Eliminar", Tema.ROJO);
        eliminar.addActionListener(e -> eliminar());
        acciones.add(editar);
        acciones.add(eliminar);
        pie.add(acciones, BorderLayout.EAST);
        add(pie, BorderLayout.SOUTH);
    }

    @Override
    public void alMostrar() {
        cargar();
    }

    private static class Datos {
        List<Medicamento> medicamentos;
        Map<Integer, String> proveedores;
    }

    private void cargar() {
        contador.setText("Cargando...");
        Tarea.ejecutar(() -> {
            Datos d = new Datos();
            d.medicamentos = MedicamentoDAO.listar();
            d.proveedores = MedicamentoDAO.proveedores();
            return d;
        }, d -> mostrar(d.medicamentos, d.proveedores),
                e -> {
                    contador.setText("Sin conexion");
                    error(this, mensajeBD(e));
                });
    }

    // Llena la tabla (tambien se usa para pruebas sin base de datos)
    void mostrar(List<Medicamento> lista, Map<Integer, String> provs) {
        medicamentos = lista;
        proveedores = provs;
        modelo.setRowCount(0);

        BigDecimal valor = BigDecimal.ZERO;
        for (Medicamento m : lista) {
            valor = valor.add(m.calcularValorInventario());
            modelo.addRow(new Object[]{
                m.getIdMedicamento(), m.getNombre(),
                m.getDescripcion() == null ? "" : m.getDescripcion(),
                "Q" + m.getPrecioCompra().setScale(2),
                "Q" + m.getPrecioVenta().setScale(2),
                m.getExistencia(),
                m.getFechaVencimiento() == null ? "-" : m.getFechaVencimiento().toString(),
                m.getIdProveedor() == null ? "-"
                        : provs.getOrDefault(m.getIdProveedor(), "-")});
        }
        contador.setText(lista.size() + " medicamentos  |  valor del inventario: Q"
                + valor.setScale(2));
    }

    private void filtrar() {
        String t = buscar.getText().trim();
        if (t.isEmpty()) {
            filtro.setRowFilter(null);
        } else {
            filtro.setRowFilter(RowFilter.regexFilter(
                    "(?i)" + java.util.regex.Pattern.quote(t), 1, 2));
        }
    }

    private Medicamento seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            aviso(this, "Seleccione un medicamento de la tabla.");
            return null;
        }
        return medicamentos.get(tabla.convertRowIndexToModel(fila));
    }

    private void nuevo() {
        Medicamento m = formulario(null);
        if (m == null) {
            return;
        }
        Tarea.ejecutar(() -> { MedicamentoDAO.insertar(m); return true; },
                ok -> cargar(), this::manejar);
    }

    private void editar() {
        Medicamento actual = seleccionado();
        if (actual == null) {
            return;
        }
        Medicamento m = formulario(actual);
        if (m == null) {
            return;
        }
        Tarea.ejecutar(() -> { MedicamentoDAO.actualizar(m); return true; },
                ok -> cargar(), this::manejar);
    }

    private void eliminar() {
        Medicamento m = seleccionado();
        if (m == null) {
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Eliminar " + m.getNombre() + "?", "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        Tarea.ejecutar(() -> { MedicamentoDAO.eliminar(m.getIdMedicamento()); return true; },
                ok -> cargar(),
                e -> {
                    if (e instanceof ValidacionException) {
                        aviso(this, e.getMessage());
                    } else if (e instanceof java.sql.SQLIntegrityConstraintViolationException) {
                        aviso(this, "No se puede eliminar: este medicamento "
                                + "tiene ventas registradas.");
                    } else {
                        error(this, mensajeBD(e));
                    }
                });
    }

    private void manejar(Exception e) {
        if (e instanceof ValidacionException) {
            aviso(this, e.getMessage());
        } else {
            error(this, mensajeBD(e));
        }
    }

    // Convierte texto a numero; lanza la excepcion personalizada si es invalido
    private static BigDecimal numero(String texto, String campo)
            throws ValidacionException {
        try {
            return new BigDecimal(texto.trim());
        } catch (NumberFormatException e) {
            throw new ValidacionException(campo + " debe ser un numero valido.");
        }
    }

    // Ventana de captura. Devuelve null si se cancela.
    private Medicamento formulario(Medicamento base) {
        Tema.Campo nombre = new Tema.Campo("Nombre del medicamento");
        Tema.Campo descripcion = new Tema.Campo("Descripcion (opcional)");
        Tema.Campo compra = new Tema.Campo("0.00");
        Tema.Campo venta = new Tema.Campo("0.00");
        Tema.Campo existencia = new Tema.Campo("0");
        Tema.Campo vence = new Tema.Campo("AAAA-MM-DD (opcional)");

        JComboBox<String> prov = new JComboBox<>();
        Tema.estilizarCombo(prov);
        prov.addItem("Sin proveedor");
        List<Integer> ids = new java.util.ArrayList<>();
        ids.add(null);
        for (Map.Entry<Integer, String> e : proveedores.entrySet()) {
            prov.addItem(e.getValue());
            ids.add(e.getKey());
        }

        if (base != null) {
            nombre.setText(base.getNombre());
            descripcion.setText(base.getDescripcion() == null ? "" : base.getDescripcion());
            compra.setText(base.getPrecioCompra().toPlainString());
            venta.setText(base.getPrecioVenta().toPlainString());
            existencia.setText(String.valueOf(base.getExistencia()));
            vence.setText(base.getFechaVencimiento() == null ? ""
                    : base.getFechaVencimiento().toString());
            prov.setSelectedIndex(Math.max(0, ids.indexOf(base.getIdProveedor())));
        }

        JPanel form = Tema.formulario(380,
                new String[]{"Nombre", "Descripcion", "Precio de compra (Q)",
                    "Precio de venta (Q)", "Existencia", "Fecha de vencimiento",
                    "Proveedor"},
                new JComponent[]{nombre, descripcion, compra, venta,
                    existencia, vence, prov});

        while (true) {
            int r = JOptionPane.showConfirmDialog(this, form,
                    base == null ? "Nuevo medicamento" : "Editar medicamento",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) {
                return null;
            }

            try {
                int exist;
                try {
                    exist = Integer.parseInt(existencia.getText().trim());
                } catch (NumberFormatException e) {
                    throw new ValidacionException(
                            "La existencia debe ser un numero entero.");
                }

                LocalDate fecha = null;
                if (!vence.getText().trim().isEmpty()) {
                    try {
                        fecha = LocalDate.parse(vence.getText().trim());
                    } catch (java.time.format.DateTimeParseException e) {
                        throw new ValidacionException(
                                "La fecha debe tener el formato AAAA-MM-DD, "
                                + "por ejemplo 2027-05-30.");
                    }
                }

                return new Medicamento(
                        base == null ? 0 : base.getIdMedicamento(),
                        nombre.getText().trim(), descripcion.getText().trim(),
                        numero(compra.getText(), "El precio de compra"),
                        numero(venta.getText(), "El precio de venta"),
                        exist, fecha, ids.get(prov.getSelectedIndex()));
            } catch (ValidacionException e) {
                aviso(this, e.getMessage());
            }
        }
    }

    // Celdas de existencia y vencimiento con colores de alerta
    private class CeldaAlerta extends DefaultTableCellRenderer {
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

            int idx = t.convertRowIndexToModel(fila);
            if (idx >= 0 && idx < medicamentos.size()) {
                Medicamento m = medicamentos.get(idx);
                if (col == COL_EXISTENCIA) {
                    if (m.getExistencia() == 0) {
                        alerta(Tema.ROJO);
                    } else if (m.getExistencia() <= 10) {
                        alerta(new Color(0xD97706));
                    }
                } else if (col == COL_VENCE && m.getFechaVencimiento() != null) {
                    LocalDate hoy = LocalDate.now();
                    if (m.getFechaVencimiento().isBefore(hoy)) {
                        alerta(Tema.ROJO);
                    } else if (!m.getFechaVencimiento().isAfter(hoy.plusDays(30))) {
                        alerta(new Color(0xD97706));
                    }
                }
            }
            return this;
        }

        private void alerta(Color c) {
            setForeground(c);
            setFont(Tema.fuente(Font.BOLD, 13f));
        }
    }
}
