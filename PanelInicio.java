import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

// Resumen general del negocio
public class PanelInicio extends PanelBase {

    private final JLabel saludo = Tema.titulo(" ", 26f);
    private final JLabel[] valores = new JLabel[4];
    private final DefaultTableModel modeloStock = modelo("Medicamento", "Existencia");
    private final DefaultTableModel modeloVence = modelo("Medicamento", "Vence");

    // Datos de una consulta de resumen
    static class Resumen {
        int clientes;
        int medicamentos;
        int pocaExistencia;
        int facturasHoy;
        BigDecimal totalHoy = BigDecimal.ZERO;
        List<Object[]> stock = new ArrayList<>();
        List<Object[]> vence = new ArrayList<>();
    }

    public PanelInicio() {
        JPanel norte = new JPanel(new BorderLayout(0, 4));
        norte.setOpaque(false);
        norte.add(saludo, BorderLayout.NORTH);
        norte.add(Tema.texto("Resumen de la farmacia al dia de hoy", Tema.GRIS),
                BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 18));
        centro.setOpaque(false);

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.setPreferredSize(new Dimension(100, 110));
        stats.add(estadistica(0, "Clientes", Tema.AZUL));
        stats.add(estadistica(1, "Medicamentos", Tema.VERDE));
        stats.add(estadistica(2, "Poca existencia", Tema.AMBAR));
        stats.add(estadistica(3, "Ventas de hoy", new Color(0x8B5CF6)));
        centro.add(stats, BorderLayout.NORTH);

        JPanel listas = new JPanel(new GridLayout(1, 2, 16, 0));
        listas.setOpaque(false);
        listas.add(listado("Poca existencia (10 o menos)", modeloStock));
        listas.add(listado("Por vencer (proximos 30 dias)", modeloVence));
        centro.add(listas, BorderLayout.CENTER);

        add(centro, BorderLayout.CENTER);
    }

    private static DefaultTableModel modelo(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int f, int c) {
                return false;
            }
        };
    }

    private JPanel estadistica(int indice, String titulo, Color color) {
        Tema.Tarjeta t = new Tema.Tarjeta(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = Tema.suave(g);
                g2.setColor(color);
                g2.fillRoundRect(0, 18, 5, getHeight() - 36, 5, 5);
                g2.dispose();
            }
        };
        valores[indice] = Tema.titulo("-", 28f);
        t.add(Tema.texto(titulo, Tema.GRIS), BorderLayout.NORTH);
        t.add(valores[indice], BorderLayout.CENTER);
        return t;
    }

    private JPanel listado(String titulo, DefaultTableModel m) {
        Tema.Tarjeta t = new Tema.Tarjeta(new BorderLayout(0, 12));
        t.add(Tema.titulo(titulo, 15f), BorderLayout.NORTH);
        JTable tabla = new JTable(m);
        t.add(Tema.tabla(tabla), BorderLayout.CENTER);
        return t;
    }

    @Override
    public void alMostrar() {
        saludo.setText("Hola, " + (Sesion.nombre.isEmpty() ? "bienvenido" : Sesion.nombre));
        Tarea.ejecutar(PanelInicio::consultar, this::mostrar,
                e -> {
                    for (JLabel v : valores) {
                        v.setText("-");
                    }
                    error(this, mensajeBD(e));
                });
    }

    void mostrar(Resumen r) {
        valores[0].setText(String.valueOf(r.clientes));
        valores[1].setText(String.valueOf(r.medicamentos));
        valores[2].setText(String.valueOf(r.pocaExistencia));
        valores[3].setText("Q" + r.totalHoy.setScale(2));
        llenar(modeloStock, r.stock);
        llenar(modeloVence, r.vence);
    }

    private static void llenar(DefaultTableModel m, List<Object[]> filas) {
        m.setRowCount(0);
        for (Object[] f : filas) {
            m.addRow(f);
        }
    }

    private static int contar(Connection c, String sql) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    static Resumen consultar() throws Exception {
        Resumen r = new Resumen();

        try (Connection c = Conexion.conectar()) {
            r.clientes = contar(c, "SELECT COUNT(*) FROM cliente");
            r.medicamentos = contar(c, "SELECT COUNT(*) FROM medicamento");
            r.pocaExistencia = contar(c,
                    "SELECT COUNT(*) FROM medicamento WHERE existencia <= 10");

            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT COUNT(*), COALESCE(SUM(total), 0) FROM factura "
                    + "WHERE DATE(fecha) = CURDATE()");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                r.facturasHoy = rs.getInt(1);
                r.totalHoy = rs.getBigDecimal(2);
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT nombre, existencia FROM medicamento "
                    + "WHERE existencia <= 10 ORDER BY existencia, nombre LIMIT 8");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    r.stock.add(new Object[]{rs.getString(1), rs.getInt(2)});
                }
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT nombre, fecha_vencimiento FROM medicamento "
                    + "WHERE fecha_vencimiento IS NOT NULL AND fecha_vencimiento "
                    + "<= DATE_ADD(CURDATE(), INTERVAL 30 DAY) "
                    + "ORDER BY fecha_vencimiento LIMIT 8");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    r.vence.add(new Object[]{rs.getString(1), rs.getDate(2)});
                }
            }
        }
        return r;
    }
}
