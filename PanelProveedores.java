import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

// CRUD de proveedores en pantalla
public class PanelProveedores extends PanelBase {

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "NIT", "Nombre", "Telefono", "Direccion", "Correo"}, 0) {
        @Override
        public boolean isCellEditable(int f, int c) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> filtro = new TableRowSorter<>(modelo);
    private final Tema.Campo buscar = new Tema.Campo("Buscar por nombre, NIT o correo...");
    private final JLabel contador = Tema.texto(" ", Tema.GRIS);
    private List<Proveedor> proveedores = new java.util.ArrayList<>();

    public PanelProveedores() {
        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);
        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(Tema.titulo("Proveedores", 24f));
        textos.add(contador);
        cab.add(textos, BorderLayout.WEST);

        Tema.Boton nuevo = new Tema.Boton("+  Nuevo proveedor", Tema.VERDE);
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

        tabla.setRowSorter(filtro);
        JScrollPane sp = Tema.tabla(tabla);
        tabla.getColumnModel().getColumn(0).setMaxWidth(60);
        Tema.anchos(tabla, 50, 120, 190, 110, 190, 200);
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
        Tarea.ejecutar(ProveedorDAO::listar, this::mostrar, e -> {
            contador.setText("Sin conexion");
            error(this, mensajeBD(e));
        });
    }

    // Llena la tabla (tambien se usa para pruebas)
    void mostrar(List<Proveedor> lista) {
        proveedores = lista;
        modelo.setRowCount(0);
        for (Proveedor p : lista) {
            modelo.addRow(new Object[]{p.getIdProveedor(), p.getNit(),
                p.getNombre(), p.getTelefono(), p.getDireccion(),
                p.tieneCorreo() ? p.getCorreo() : "-"});
        }
        contador.setText(lista.size() + (lista.size() == 1
                ? " proveedor registrado" : " proveedores registrados"));
    }

    private void filtrar() {
        String t = buscar.getText().trim();
        filtro.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter(
                "(?i)" + java.util.regex.Pattern.quote(t), 1, 2, 5));
    }

    private Proveedor seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            aviso(this, "Seleccione un proveedor de la tabla.");
            return null;
        }
        return proveedores.get(tabla.convertRowIndexToModel(fila));
    }

    private void nuevo() {
        Proveedor p = formulario(null);
        if (p == null) {
            return;
        }
        Tarea.ejecutar(() -> { ProveedorDAO.insertar(p); return true; },
                ok -> cargar(), this::manejar);
    }

    private void editar() {
        Proveedor actual = seleccionado();
        if (actual == null) {
            return;
        }
        Proveedor p = formulario(actual);
        if (p == null) {
            return;
        }
        Tarea.ejecutar(() -> { ProveedorDAO.actualizar(p); return true; },
                ok -> cargar(), this::manejar);
    }

    private void eliminar() {
        Proveedor p = seleccionado();
        if (p == null) {
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Eliminar a " + p.getNombre() + "?", "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        Tarea.ejecutar(() -> { ProveedorDAO.eliminar(p.getIdProveedor()); return true; },
                ok -> cargar(),
                e -> {
                    if (e instanceof ValidacionException) {
                        aviso(this, e.getMessage());
                    } else if (e instanceof java.sql.SQLIntegrityConstraintViolationException) {
                        aviso(this, "No se puede eliminar: este proveedor "
                                + "tiene medicamentos relacionados.");
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

    private Proveedor formulario(Proveedor base) {
        Tema.Campo nit = new Tema.Campo("NIT");
        Tema.Campo nombre = new Tema.Campo("Nombre del proveedor");
        Tema.Campo telefono = new Tema.Campo("Telefono");
        Tema.Campo direccion = new Tema.Campo("Direccion");
        Tema.Campo correo = new Tema.Campo("correo@empresa.com (opcional)");

        if (base != null) {
            nit.setText(base.getNit());
            nombre.setText(base.getNombre());
            telefono.setText(base.getTelefono() == null ? "" : base.getTelefono());
            direccion.setText(base.getDireccion() == null ? "" : base.getDireccion());
            correo.setText(base.getCorreo() == null ? "" : base.getCorreo());
        }

        JPanel form = Tema.formulario(380,
                new String[]{"NIT", "Nombre", "Telefono", "Direccion", "Correo"},
                new JComponent[]{nit, nombre, telefono, direccion, correo});

        int r = JOptionPane.showConfirmDialog(this, form,
                base == null ? "Nuevo proveedor" : "Editar proveedor",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return null;
        }
        return new Proveedor(base == null ? 0 : base.getIdProveedor(),
                nit.getText().trim(), nombre.getText().trim(),
                telefono.getText().trim(), direccion.getText().trim(),
                correo.getText().trim());
    }
}
