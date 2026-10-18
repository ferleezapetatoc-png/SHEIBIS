import java.math.BigDecimal;

public class DetalleFactura {

    private int idDetalle;
    private int idFactura;
    private int idMedicamento;
    private int cantidad;
    private BigDecimal precioUnitario;

    public DetalleFactura(int idDetalle, int idFactura,
                          int idMedicamento, int cantidad,
                          BigDecimal precioUnitario) {
        this.idDetalle = idDetalle;
        this.idFactura = idFactura;
        this.idMedicamento = idMedicamento;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(int idFactura) {
        this.idFactura = idFactura;
    }

    public int getIdMedicamento() {
        return idMedicamento;
    }

    public void setIdMedicamento(int idMedicamento) {
        this.idMedicamento = idMedicamento;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal calcularSubtotal() {
        return precioUnitario.multiply(
                BigDecimal.valueOf(cantidad)
        );
    }

    public void mostrarInformacion() {
        System.out.println("ID factura: " + idFactura);
        System.out.println("ID medicamento: " + idMedicamento);
        System.out.println("Cantidad: " + cantidad);
        System.out.println("Subtotal: Q" + calcularSubtotal());
    }
}