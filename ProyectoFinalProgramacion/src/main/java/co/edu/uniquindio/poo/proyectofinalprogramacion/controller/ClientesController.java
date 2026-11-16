package co.edu.uniquindio.poo.proyectofinalprogramacion.controller;

import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Cliente;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.MonederoVirtual;

import java.util.Comparator;
import java.util.List;

public class ClientesController {

    private MonederoVirtual monederoVirtual;
    private int contador;

    public ClientesController(MonederoVirtual monederoVirtual) {
        this.monederoVirtual = monederoVirtual;
    }

    public List<Cliente> obtenerClientes() {
        return monederoVirtual.getListaClientes().values().stream()
                .sorted(Comparator.comparing(Cliente::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<Cliente> buscarClientes(String texto) {
        if (texto == null || texto.isBlank()) {
            return obtenerClientes();
        }
        String filtro = texto.trim().toLowerCase();
        return obtenerClientes().stream()
                .filter(c -> c.getDocumento().toLowerCase().contains(filtro)
                        || c.getNombre().toLowerCase().contains(filtro))
                .toList();
    }

    public Cliente buscarCliente(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("Ingrese el documento del cliente");
        }
        return obtenerClientes().stream()
                .filter(c -> c.getDocumento().equals(documento.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un cliente con el documento " + documento.trim()));
    }

    public void agregarCliente(String documento, String nombre, String telefono, String correo) {
        validarDocumento(documento);
        String documentoLimpio = documento.trim();
        boolean repetido = obtenerClientes().stream().anyMatch(c -> c.getDocumento().equals(documentoLimpio));
        if (repetido) {
            throw new IllegalArgumentException("Ya existe un cliente con el documento " + documentoLimpio);
        }
        monederoVirtual.registrarCliente(
                new Cliente(generarId(), limpiar(nombre), documentoLimpio, limpiar(telefono), limpiar(correo)));
    }

    public void actualizarCliente(String documento, String nombre, String telefono, String correo) {
        Cliente cliente = buscarCliente(documento);
        // Validar antes de modificar
        new Cliente("validacion", limpiar(nombre), cliente.getDocumento(), limpiar(telefono), limpiar(correo));
        cliente.setNombre(limpiar(nombre));
        cliente.setTelefono(limpiar(telefono));
        cliente.setCorreo(limpiar(correo));
    }

    public void eliminarCliente(String documento) {
        Cliente cliente = buscarCliente(documento);
        monederoVirtual.eliminarCliente(cliente.getId());
    }

    private void validarDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento no puede estar vacío.");
        }
        if (!documento.trim().matches("\\d+")) {
            throw new IllegalArgumentException("El documento debe contener solo números.");
        }
    }

    private String generarId() {
        String id;
        do {
            id = String.format("C%03d", ++contador);
        } while (monederoVirtual.buscarCliente(id).isPresent());
        return id;
    }

    private String limpiar(String texto) {
        return texto == null ? null : texto.trim();
    }
}
