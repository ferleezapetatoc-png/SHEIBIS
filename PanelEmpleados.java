import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

// CRUD de empleados (modulo de usuarios y roles) en pantalla
public class PanelEmpleados extends PanelBase {

    private static final int COL_ROL = 5;

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "NIT", "Nombre", "Puesto", "Usuario", "Rol"}, 0) {
        @Override
        public boolean isCellEditable(int f, int c) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> filtro = new TableRowSorter<>(modelo);
    private final Tema.Campo buscar = new Tema.Campo("Buscar por nombre, usuario o puesto...");
    private final JLabel contador = Tema.texto(" ", Tema.GRIS);
    private List<EmpleadoDAO.Fila> empleados = new java.util.ArrayList<>();

    public PanelEmpleados() {
        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);
        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(Tema.titulo("Empleados y usuarios", 24f));
        textos.add(contador);
        cab.add(textos, BorderLayout.WEST);

        Tema.Boton nuevo = new Tema.Boton("+  Nuevo empleado", Tema.VERDE);
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
        Tema.anchos(tabla, 50, 120, 230, 160, 150, 140);
        tabla.getColumnModel().getColumn(COL_ROL).setCellRenderer(new CeldaRol());
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

        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.add(Tema.texto("Las contrasenas se guardan cifradas y nunca se muestran.",
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

    private void cargar() {
        contador.setText("Cargando...");
        Tarea.ejecutar(EmpleadoDAO::listar, this::mostrar, e -> {
            contador.setText("Sin conexion");
            error(this, mensajeBD(e));
        });
    }

    // Llena la tabla (tambien se usa para pruebas)
    void mostrar(List<EmpleadoDAO.Fila> lista) {
        empleados = lista;
        modelo.setRowCount(0);
        for (EmpleadoDAO.Fila f : lista) {
            Empleado e = f.empleado;
            modelo.addRow(new Object[]{e.getIdEmpleado(), e.getNit(),
                e.getNombre() + " " + e.getApellido(), e.getPuesto(),
                e.getUsuario(), f.rol});
        }
        contador.setText(lista.size() + (lista.size() == 1
                ? " usuario registrado" : " usuarios registrados"));
    }

    private void filtrar() {
        String t = buscar.getText().trim();
        filtro.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter(
                "(?i)" + java.util.regex.Pattern.quote(t), 2, 3, 4));
    }

    private EmpleadoDAO.Fila seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            aviso(this, "Seleccione un empleado de la tabla.");
            return null;
        }
        return empleados.get(tabla.convertRowIndexToModel(fila));
    }

    private void nuevo() {
        Object[] datos = formulario(null);
        if (datos == null) {
            return;
        }
        Empleado e = (Empleado) datos[0];
        String rol = (String) datos[1];
        String clave = (String) datos[2];
        Tarea.ejecutar(() -> { EmpleadoDAO.insertar(e, rol, clave); return true; },
                ok -> cargar(), this::manejar);
    }

    private void editar() {
        EmpleadoDAO.Fila actual = seleccionado();
        if (actual == null) {
            return;
        }
        Object[] datos = formulario(actual);
        if (datos == null) {
            return;
        }
        Empleado e = (Empleado) datos[0];
        String rol = (String) datos[1];
        String clave = (String) datos[2];
        Tarea.ejecutar(() -> { EmpleadoDAO.actualizar(e, rol, clave); return true; },
                ok -> cargar(), this::manejar);
    }

    private void eliminar() {
        EmpleadoDAO.Fila f = seleccionado();
        if (f == null) {
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Eliminar a " + f.empleado.getNombre() + " "
                + f.empleado.getApellido() + "?", "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        Tarea.ejecutar(() -> { EmpleadoDAO.eliminar(f.empleado.getIdEmpleado()); return true; },
                ok -> cargar(),
                e -> {
                    if (e instanceof ValidacionException) {
                        aviso(this, e.getMessage());
                    } else if (e instanceof java.sql.SQLIntegrityConstraintViolationException) {
                        aviso(this, "No se puede eliminar: este empleado "
                                + "tiene facturas relacionadas.");
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

    // Devuelve {Empleado, rol, clave} o null si se cancela
    private Object[] formulario(EmpleadoDAO.Fila base) {
        Tema.Campo nit = new Tema.Campo("NIT");
        Tema.Campo nombre = new Tema.Campo("Nombre");
        Tema.Campo apellido = new Tema.Campo("Apellido");
        Tema.Campo telefono = new Tema.Campo("Telefono");
        Tema.Campo direccion = new Tema.Campo("Direccion");
        Tema.Campo puesto = new Tema.Campo("Puesto");
        JComboBox<String> rol = new JComboBox<>(new String[]{"EMPLEADO", "ADMINISTRADOR"});
        Tema.estilizarCombo(rol);
        Tema.Campo usuario = new Tema.Campo("Usuario para iniciar sesion");
        Tema.Clave clave = new Tema.Clave(base == null
                ? "Minimo 4 caracteres" : "Dejar vacia para no cambiarla");

        if (base != null) {
            Empleado e = base.empleado;
            nit.setText(e.getNit() == null ? "" : e.getNit());
            nombre.setText(e.getNombre());
            apellido.setText(e.getApellido());
            telefono.setText(e.getTelefono() == null ? "" : e.getTelefono());
            direccion.setText(e.getDireccion() == null ? "" : e.getDireccion());
            puesto.setText(e.getPuesto() == null ? "" : e.getPuesto());
            rol.setSelectedItem(base.rol);
            usuario.setText(e.getUsuario());
        }

        JPanel izq = Tema.formulario(300,
                new String[]{"NIT", "Nombre", "Apellido", "Telefono", "Direccion"},
                new JComponent[]{nit, nombre, apellido, telefono, direccion});
        JPanel der = Tema.formulario(300,
                new String[]{"Puesto", "Rol", "Usuario",
                    base == null ? "Contrasena" : "Nueva contrasena"},
                new JComponent[]{puesto, rol, usuario, clave});

        JPanel form = new JPanel(new GridLayout(1, 2, 18, 0));
        form.add(izq);
        form.add(der);
        form.setPreferredSize(new Dimension(620, 390));

        int r = JOptionPane.showConfirmDialog(this, form,
                base == null ? "Nuevo empleado" : "Editar empleado",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return null;
        }

        Empleado e = new Empleado(base == null ? 0 : base.empleado.getIdEmpleado(),
                nit.getText().trim(), nombre.getText().trim(),
                apellido.getText().trim(), telefono.getText().trim(),
                direccion.getText().trim(), puesto.getText().trim(),
                usuario.getText().trim(), "");
        return new Object[]{e, (String) rol.getSelectedItem(),
            new String(clave.getPassword())};
    }

    // Resalta a los administradores
    private static class CeldaRol extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor,
                boolean sel, boolean foco, int fila, int col) {
            super.getTableCellRendererComponent(t, valor, sel, foco, fila, col);
            setBorder(new EmptyBorder(0, 14, 0, 14));
            if (!sel) {
                setBackground(fila % 2 == 0 ? Tema.BLANCO : new Color(0xF8FAFC));
            }
            boolean admin = "ADMINISTRADOR".equals(valor);
            setForeground(admin ? Tema.VERDE_OSCURO : Tema.TEXTO);
            setFont(Tema.fuente(admin ? Font.BOLD : Font.PLAIN, 13f));
            return this;
        }
    }
}
