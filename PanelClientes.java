import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

// CRUD de clientes en pantalla
public class PanelClientes extends PanelBase {

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "NIT", "Nombre", "Apellido", "Telefono", "Direccion"}, 0) {
        @Override
        public boolean isCellEditable(int f, int c) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> filtro = new TableRowSorter<>(modelo);
    private final Tema.Campo buscar = new Tema.Campo("Buscar por nombre, NIT o telefono...");
    private final JLabel contador = Tema.texto(" ", Tema.GRIS);
    private List<Cliente> clientes = new java.util.ArrayList<>();

    public PanelClientes() {
        // ----- Encabezado -----
        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);
        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(Tema.titulo("Clientes", 24f));
        textos.add(contador);
        cab.add(textos, BorderLayout.WEST);

        Tema.Boton nuevo = new Tema.Boton("+  Nuevo cliente", Tema.VERDE);
        nuevo.addActionListener(e -> nuevo());
        JPanel derecha = new JPanel(new GridBagLayout());
        derecha.setOpaque(false);
        derecha.add(nuevo);
        cab.add(derecha, BorderLayout.EAST);

        // ----- Buscador -----
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(buscar, BorderLayout.CENTER);
        barra.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(cab, BorderLayout.NORTH);
        norte.add(barra, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        // ----- Tabla -----
        tabla.setRowSorter(filtro);
        JScrollPane sp = Tema.tabla(tabla);
        tabla.getColumnModel().getColumn(0).setMaxWidth(70);
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

        // ----- Acciones -----
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);
        Tema.Boton editar = Tema.Boton.contorno("Editar");
        editar.addActionListener(e -> editar());
        Tema.Boton eliminar = new Tema.Boton("Eliminar", Tema.ROJO);
        eliminar.addActionListener(e -> eliminar());
        acciones.add(editar);
        acciones.add(eliminar);
        add(acciones, BorderLayout.SOUTH);
    }

    @Override
    public void alMostrar() {
        cargar();
    }

    private void cargar() {
        contador.setText("Cargando...");
        Tarea.ejecutar(ClienteDAO::listar,
                this::mostrar,
                e -> {
                    contador.setText("Sin conexion");
                    error(this, mensajeBD(e));
                });
    }

    // Llena la tabla (tambien se usa para pruebas sin base de datos)
    void mostrar(List<Cliente> lista) {
        clientes = lista;
        modelo.setRowCount(0);
        for (Cliente c : lista) {
            modelo.addRow(new Object[]{c.getIdCliente(), c.getNit(),
                c.getNombre(), c.getApellido(), c.getTelefono(), c.getDireccion()});
        }
        contador.setText(lista.size() + (lista.size() == 1
                ? " cliente registrado" : " clientes registrados"));
    }

    private void filtrar() {
        String t = buscar.getText().trim();
        if (t.isEmpty()) {
            filtro.setRowFilter(null);
        } else {
            filtro.setRowFilter(RowFilter.regexFilter(
                    "(?i)" + java.util.regex.Pattern.quote(t)));
        }
    }

    private Cliente seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            aviso(this, "Seleccione un cliente de la tabla.");
            return null;
        }
        int id = (int) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        for (Cliente c : clientes) {
            if (c.getIdCliente() == id) {
                return c;
            }
        }
        return null;
    }

    private void nuevo() {
        Cliente c = formulario(null);
        if (c == null) {
            return;
        }
        Tarea.ejecutar(() -> { ClienteDAO.insertar(c); return true; },
                ok -> cargar(), this::manejar);
    }

    private void editar() {
        Cliente actual = seleccionado();
        if (actual == null) {
            return;
        }
        Cliente c = formulario(actual);
        if (c == null) {
            return;
        }
        Tarea.ejecutar(() -> { ClienteDAO.actualizar(c); return true; },
                ok -> cargar(), this::manejar);
    }

    private void eliminar() {
        Cliente c = seleccionado();
        if (c == null) {
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Eliminar a " + c.obtenerNombreCompleto() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        Tarea.ejecutar(() -> { ClienteDAO.eliminar(c.getIdCliente()); return true; },
                ok -> cargar(),
                e -> {
                    if (e instanceof ValidacionException) {
                        aviso(this, e.getMessage());
                    } else {
                        error(this, "No se pudo eliminar. Verifique si el cliente "
                                + "tiene facturas relacionadas.\n" + e.getMessage());
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

    // Ventana de captura de datos. Devuelve null si se cancela.
    private Cliente formulario(Cliente base) {
        Tema.Campo nit = new Tema.Campo("NIT");
        Tema.Campo nombre = new Tema.Campo("Nombre");
        Tema.Campo apellido = new Tema.Campo("Apellido");
        Tema.Campo telefono = new Tema.Campo("Telefono");
        Tema.Campo direccion = new Tema.Campo("Direccion");

        if (base != null) {
            nit.setText(base.getNit());
            nombre.setText(base.getNombre());
            apellido.setText(base.getApellido());
            telefono.setText(base.getTelefono());
            direccion.setText(base.getDireccion());
        }

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 10));
        form.setPreferredSize(new Dimension(360, 290));
        form.add(nit);
        form.add(nombre);
        form.add(apellido);
        form.add(telefono);
        form.add(direccion);

        int r = JOptionPane.showConfirmDialog(this, form,
                base == null ? "Nuevo cliente" : "Editar cliente",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return null;
        }
        return new Cliente(base == null ? 0 : base.getIdCliente(),
                nit.getText().trim(), nombre.getText().trim(),
                apellido.getText().trim(), telefono.getText().trim(),
                direccion.getText().trim());
    }
}
