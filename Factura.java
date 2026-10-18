import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Factura {

    private int idFactura;
    private String numeroFactura;
    private LocalDateTime fecha;
    private Integer idCliente;
    private int idEmpleado;
    private BigDecimal total;
    private List<DetalleFactura> detalles = new ArrayList<>();

    public Factura(int idFactura, String numeroFactura,
                   LocalDateTime fecha, Integer idCliente,
                   int idEmpleado, BigDecimal total) {
        this.idFactura = idFactura;
        this.numeroFactura = numeroFactura;
        this.fecha = fecha;
        this.idCliente = idCliente;
        this.idEmpleado = idEmpleado;
        this.total = total;
    }

    public int getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(int idFactura) {
        this.idFactura = idFactura;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    // Agrega una linea de detalle a la factura
    public void agregarDetalle(DetalleFactura detalle) {
        detalles.add(detalle);
    }

    public List<DetalleFactura> getDetalles() {
        return detalles;
    }

    // Suma los subtotales de todos los detalles y actualiza el total
    public BigDecimal calcularTotal() {
        BigDecimal suma = BigDecimal.ZERO;
        for (DetalleFactura d : detalles) {
            suma = suma.add(d.calcularSubtotal());
        }
        this.total = suma;
        return suma;
    }

    // Cantidad total de unidades vendidas en la factura
    public int contarUnidades() {
        int unidades = 0;
        for (DetalleFactura d : detalles) {
            unidades += d.getCantidad();
        }
        return unidades;
    }

    // Una factura sin cliente se considera "consumidor final"
    public boolean esConsumidorFinal() {
        return idCliente == null;
    }

    public void mostrarInformacion() {
        System.out.println("Factura: " + numeroFactura);
        System.out.println("Fecha: " + fecha);
        System.out.println("Total: Q" + total);
    }
}