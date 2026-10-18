import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    static Scanner teclado = new Scanner(System.in);

    public static void main(String[] args) {

        if (!Login.iniciar(teclado)) {
            System.out.println("No se pudo iniciar sesion. Programa cerrado.");
            teclado.close();
            return;
        }

        System.out.println("\nBienvenido, " + Sesion.nombre
                + " (" + Sesion.rol + ")");

        int opcion = 0;

        while (opcion != 11) {

            System.out.println("\n===== FARMACIA SHEIBIS =====");
            System.out.println("1. Registrar cliente");
            System.out.println("2. Mostrar clientes");
            System.out.println("3. Actualizar cliente");
            System.out.println("4. Eliminar cliente");
            System.out.println("5. Medicamentos");
            System.out.println("6. Proveedores");
            System.out.println("7. Empleados");
            System.out.println("8. Facturas");
            System.out.println("9. Reportes");
            System.out.println("10. Demostracion de los 4 pilares de POO");
            System.out.println("11. Salir");
            System.out.print("Seleccione una opcion: ");

            try {
                opcion = Integer.parseInt(teclado.nextLine());

                switch (opcion) {
                    case 1:
                        registrarCliente();
                        break;

                    case 2:
                        mostrarClientes();
                        break;

                    case 3:
                        actualizarCliente();
                        break;

                    case 4:
                        eliminarCliente();
                        break;

                    case 5:
                        MedicamentoCRUD.menu(teclado);
                        break;

                    case 6:
                        if (Sesion.esAdministrador()) {
                            ProveedorCRUD.menu(teclado);
                        } else {
                            System.out.println("Acceso denegado: solo administradores.");
                        }
                        break;

                    case 7:
                        if (Sesion.esAdministrador()) {
                            EmpleadoCRUD.menu(teclado);
                        } else {
                            System.out.println("Acceso denegado: solo administradores.");
                        }
                        break;

                    case 8:
                        FacturaCRUD.menu(teclado);
                        break;

                    case 9:
                        Reportes.menu(teclado);
                        break;

                    case 10:
                        demostrarPOO();
                        break;

                    case 11:
                        System.out.println("Hasta pronto.");
                        break;

                    default:
                        System.out.println("Opcion incorrecta.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un numero.");
            }
        }

        teclado.close();
    }

    // REGISTRAR CLIENTE
    public static void registrarCliente() {

        System.out.println("\n--- REGISTRAR CLIENTE ---");

        System.out.print("NIT: ");
        String nit = teclado.nextLine().trim();

        System.out.print("Nombre: ");
        String nombre = teclado.nextLine().trim();

        System.out.print("Apellido: ");
        String apellido = teclado.nextLine().trim();

        System.out.print("Telefono: ");
        String telefono = teclado.nextLine().trim();

        System.out.print("Direccion: ");
        String direccion = teclado.nextLine().trim();

        if (nit.isEmpty() || nombre.isEmpty()
                || apellido.isEmpty()) {
            System.out.println("NIT, nombre y apellido son obligatorios.");
            return;
        }

        String sql = "INSERT INTO cliente "
                + "(nit, nombre, apellido, telefono, direccion) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.conectar()) {

            String verificar = "SELECT id_cliente FROM cliente WHERE nit = ?";

            try (PreparedStatement consulta =
                    conexion.prepareStatement(verificar)) {

                consulta.setString(1, nit);

                try (ResultSet resultado = consulta.executeQuery()) {
                    if (resultado.next()) {
                        System.out.println("El NIT ya esta registrado.");
                        return;
                    }
                }
            }

            try (PreparedStatement sentencia =
                    conexion.prepareStatement(sql)) {

                sentencia.setString(1, nit);
                sentencia.setString(2, nombre);
                sentencia.setString(3, apellido);
                sentencia.setString(4, telefono);
                sentencia.setString(5, direccion);

                sentencia.executeUpdate();

                System.out.println("Cliente registrado correctamente.");
            }

        } catch (SQLException e) {
            System.out.println("Error al registrar: " + e.getMessage());
        }
    }

    // MOSTRAR CLIENTES
    public static void mostrarClientes() {

        String sql = "SELECT * FROM cliente ORDER BY id_cliente";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            System.out.println("\n--- LISTA DE CLIENTES ---");

            boolean hayClientes = false;

            while (resultado.next()) {

                hayClientes = true;

                Cliente cliente = new Cliente(
                        resultado.getInt("id_cliente"),
                        resultado.getString("nit"),
                        resultado.getString("nombre"),
                        resultado.getString("apellido"),
                        resultado.getString("telefono"),
                        resultado.getString("direccion")
                );

                cliente.mostrarInformacion();
                System.out.println("------------------------");
            }

            if (!hayClientes) {
                System.out.println("No hay clientes registrados.");
            }

        } catch (SQLException e) {
            System.out.println("Error al mostrar clientes: " + e.getMessage());
        }
    }

    // ACTUALIZAR CLIENTE
    public static void actualizarCliente() {

        System.out.println("\n--- ACTUALIZAR CLIENTE ---");

        System.out.print("ID del cliente: ");
        int id;

        try {
            id = Integer.parseInt(teclado.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("ID incorrecto.");
            return;
        }

        System.out.print("Nuevo NIT: ");
        String nit = teclado.nextLine().trim();

        System.out.print("Nuevo nombre: ");
        String nombre = teclado.nextLine().trim();

        System.out.print("Nuevo apellido: ");
        String apellido = teclado.nextLine().trim();

        System.out.print("Nuevo telefono: ");
        String telefono = teclado.nextLine().trim();

        System.out.print("Nueva direccion: ");
        String direccion = teclado.nextLine().trim();

        if (nit.isEmpty() || nombre.isEmpty()
                || apellido.isEmpty()) {
            System.out.println("NIT, nombre y apellido son obligatorios.");
            return;
        }

        String sql = "UPDATE cliente SET nit = ?, nombre = ?, "
                + "apellido = ?, telefono = ?, direccion = ? "
                + "WHERE id_cliente = ?";

        try (Connection conexion = Conexion.conectar()) {

            String verificar = "SELECT id_cliente FROM cliente "
                    + "WHERE nit = ? AND id_cliente <> ?";

            try (PreparedStatement consulta =
                    conexion.prepareStatement(verificar)) {

                consulta.setString(1, nit);
                consulta.setInt(2, id);

                try (ResultSet resultado = consulta.executeQuery()) {
                    if (resultado.next()) {
                        System.out.println("El NIT pertenece a otro cliente.");
                        return;
                    }
                }
            }

            try (PreparedStatement sentencia =
                    conexion.prepareStatement(sql)) {

                sentencia.setString(1, nit);
                sentencia.setString(2, nombre);
                sentencia.setString(3, apellido);
                sentencia.setString(4, telefono);
                sentencia.setString(5, direccion);
                sentencia.setInt(6, id);

                int filas = sentencia.executeUpdate();

                if (filas > 0) {
                    System.out.println("Cliente actualizado correctamente.");
                } else {
                    System.out.println("No se encontro ese cliente.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al actualizar: " + e.getMessage());
        }
    }

    // ELIMINAR CLIENTE
    public static void eliminarCliente() {

        System.out.println("\n--- ELIMINAR CLIENTE ---");
        System.out.print("ID del cliente: ");

        int id;

        try {
            id = Integer.parseInt(teclado.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("ID incorrecto.");
            return;
        }

        System.out.print("¿Desea eliminarlo? Escriba S para confirmar: ");
        String confirmacion = teclado.nextLine();

        if (!confirmacion.equalsIgnoreCase("S")) {
            System.out.println("Operacion cancelada.");
            return;
        }

        String sql = "DELETE FROM cliente WHERE id_cliente = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);

            int filas = sentencia.executeUpdate();

            if (filas > 0) {
                System.out.println("Cliente eliminado correctamente.");
            } else {
                System.out.println("No se encontro ese cliente.");
            }

        } catch (SQLException e) {
            System.out.println("No se pudo eliminar: " + e.getMessage());
            System.out.println("Verifique si tiene facturas relacionadas.");
        }
    }

    // DEMOSTRAR LOS CUATRO PILARES DE POO
    public static void demostrarPOO() {

        System.out.println("\n===== DEMOSTRACION DE LOS 4 PILARES DE POO =====");

        // ---------- 1. ABSTRACCION ----------
        System.out.println("\n--- 1. ABSTRACCION ---");
        System.out.println("Persona es una clase abstracta: define que toda persona");
        System.out.println("tiene nit, nombre, apellido, telefono y direccion, y obliga");
        System.out.println("a sus hijas a implementar mostrarInformacion().");
        System.out.println("No se puede hacer 'new Persona(...)'; solo se crean");
        System.out.println("objetos de sus clases hijas (Cliente, Empleado).");

        // ---------- 2. HERENCIA ----------
        System.out.println("\n--- 2. HERENCIA ---");
        Cliente cliente = new Cliente(
                1, "1234567-8", "Carlos", "Lopez",
                "55555555", "Quetzaltenango"
        );
        Empleado empleado = new Empleado(
                1, "9876543-2", "Ana", "Perez",
                "44444444", "Quetzaltenango",
                "Administrador", "ana_demo", "clave_demo"
        );
        System.out.println("Cliente y Empleado extienden de Persona.");
        System.out.println("Ambos reutilizan sin reescribir: getNombre() = "
                + cliente.getNombre() + " / " + empleado.getNombre());
        System.out.println("Cliente es una Persona: " + (cliente instanceof Persona));
        System.out.println("Empleado es una Persona: " + (empleado instanceof Persona));

        // ---------- 3. POLIMORFISMO ----------
        System.out.println("\n--- 3. POLIMORFISMO ---");
        Persona[] personas = { cliente, empleado };

        for (Persona persona : personas) {
            System.out.println("\nObjeto real: "
                    + persona.getClass().getSimpleName());
            persona.mostrarInformacion();
        }

        System.out.println("\nSe llama al mismo metodo mostrarInformacion()");
        System.out.println("sobre una referencia Persona, pero cada clase hija");
        System.out.println("responde de forma distinta (sobrescritura).");

        // ---------- 4. ENCAPSULAMIENTO ----------
        System.out.println("\n--- 4. ENCAPSULAMIENTO ---");
        System.out.println("Los atributos son private; no se acceden directamente.");
        System.out.println("Solo se leen y modifican mediante getters y setters.");
        System.out.println("Nombre antes: " + cliente.getNombre());
        cliente.setNombre("Carlos Alberto");
        System.out.println("Nombre despues de setNombre(): " + cliente.getNombre());

        System.out.println("\n===== FIN DE LA DEMOSTRACION =====");
    }
}
