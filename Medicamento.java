import java.math.BigDecimal;
import java.time.LocalDate;

public class Medicamento {

    private int idMedicamento;
    private String nombre;
    private String descripcion;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private int existencia;
    private LocalDate fechaVencimiento;
    private Integer idProveedor;

    public Medicamento(int idMedicamento, String nombre,
                       String descripcion, BigDecimal precioCompra,
                       BigDecimal precioVenta, int existencia,
                       LocalDate fechaVencimiento, Integer idProveedor) {
        this.idMedicamento = idMedicamento;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.existencia = existencia;
        this.fechaVencimiento = fechaVencimiento;
        this.idProveedor = idProveedor;
    }

    public int getIdMedicamento() {
        return idMedicamento;
    }

    public void setIdMedicamento(int idMedicamento) {
        this.idMedicamento = idMedicamento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public int getExistencia() {
        return existencia;
    }

    public void setExistencia(int existencia) {
        this.existencia = existencia;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public BigDecimal calcularValorInventario() {
        return precioVenta.multiply(
                BigDecimal.valueOf(existencia)
        );
    }
    public void verificarExistencia(int cantidad)
            throws ExistenciaInsuficienteException {
        if (cantidad > existencia) {
            throw new ExistenciaInsuficienteException(
                    "No hay existencia suficiente de " + nombre
                    + ". Existencia: " + existencia
                    + ", solicitado: " + cantidad + ".",
                    existencia
            );
        }
    }
    public boolean hayExistencias(int cantidad) {
        return cantidad > 0 && cantidad <= existencia;
    }

    public void mostrarInformacion() {
        System.out.println("Medicamento: " + nombre);
        System.out.println("Descripción: " + descripcion);
        System.out.println("Precio de venta: Q" + precioVenta);
        System.out.println("Existencia: " + existencia);
    }
}