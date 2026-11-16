package co.edu.uniquindio.poo.proyectofinalprogramacion.controller;

import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Cliente;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Monedero;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.MonederoAhorros;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.MonederoGastos;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.MonederoVirtual;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MonederosController {

    public static final String AHORROS = "Ahorros";
    public static final String GASTOS = "Gastos";

    private MonederoVirtual monederoVirtual;
    private int contador;

    public MonederosController(MonederoVirtual monederoVirtual) {
        this.monederoVirtual = monederoVirtual;
    }

    public List<String> obtenerTipos() {
        return List.of(AHORROS, GASTOS);
    }

    public List<Monedero> obtenerMonederos(Cliente cliente) {
        return cliente.getListaMonederos().values().stream()
                .sorted(Comparator.comparing(Monedero::getNumeroCuenta))
                .toList();
    }

    public List<Monedero> obtenerTodosLosMonederos() {
        return monederoVirtual.getListaClientes().values().stream()
                .flatMap(c -> c.getListaMonederos().values().stream())
                .sorted(Comparator.comparing(Monedero::getNumeroCuenta))
                .toList();
    }

    public void agregarMonedero(Cliente cliente, String tipo, String nombre, double saldoInicial,
                                double metaAhorro, double limiteRetiro, double umbralSaldoBajo) {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("Seleccione el tipo de monedero");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del monedero es obligatorio");
        }
        String cuenta = generarCuenta();
        Monedero monedero;
        switch (tipo) {
            case AHORROS -> monedero = new MonederoAhorros(cuenta, saldoInicial, limiteRetiro, metaAhorro);
            case GASTOS -> monedero = new MonederoGastos(cuenta, saldoInicial, limiteRetiro, umbralSaldoBajo);
            default -> throw new IllegalArgumentException("Tipo de monedero no válido");
        }
        cliente.agregarMonedero(nombre.trim(), monedero);
    }

    public void actualizarMonedero(Cliente cliente, String numeroCuenta, String nombre,
                                   double metaAhorro, double umbralSaldoBajo) {
        Monedero monedero = monederoVirtual.obtenerMonederoDelCliente(cliente, numeroCuenta);
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del monedero es obligatorio");
        }
        if (monedero instanceof MonederoAhorros && metaAhorro <= 0) {
            throw new IllegalArgumentException("La meta de ahorro debe ser mayor a cero.");
        }
        if (monedero instanceof MonederoGastos && umbralSaldoBajo < 0) {
            throw new IllegalArgumentException("El umbral de saldo bajo no puede ser negativo.");
        }
        cliente.renombrarMonedero(obtenerNombre(monedero), nombre.trim());
        if (monedero instanceof MonederoAhorros ahorros) {
            ahorros.setMetaAhorro(metaAhorro);
        } else if (monedero instanceof MonederoGastos gastos) {
            gastos.setUmbralSaldoBajo(umbralSaldoBajo);
        }
    }

    public void eliminarMonedero(Cliente cliente, String numeroCuenta) {
        Monedero monedero = monederoVirtual.obtenerMonederoDelCliente(cliente, numeroCuenta);
        if (monedero.getSaldo() > 0) {
            throw new IllegalStateException("No se puede eliminar un monedero con saldo disponible");
        }
        cliente.eliminarMonedero(obtenerNombre(monedero));
    }

    public String obtenerTipo(Monedero monedero) {
        return monedero.getTipoMonedero();
    }

    public double obtenerMeta(Monedero monedero) {
        return monedero instanceof MonederoAhorros ahorros ? ahorros.getMetaAhorro() : 0;
    }

    public double obtenerLimite(Monedero monedero) {
        if (monedero instanceof MonederoAhorros ahorros) {
            return ahorros.getLimiteRetiro();
        }
        if (monedero instanceof MonederoGastos gastos) {
            return gastos.getLimiteRetiro();
        }
        return 0;
    }

    public double obtenerUmbral(Monedero monedero) {
        return monedero instanceof MonederoGastos gastos ? gastos.getUmbralSaldoBajo() : 0;
    }

    // El nombre del monedero es la clave en el mapa del cliente
    public String obtenerNombre(Monedero monedero) {
        return monederoVirtual.getListaClientes().values().stream()
                .flatMap(c -> c.getListaMonederos().entrySet().stream())
                .filter(e -> e.getValue() == monedero)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(monedero.getNumeroCuenta());
    }

    public String describir(Monedero monedero) {
        return obtenerNombre(monedero) + " (" + monedero.getNumeroCuenta() + ")";
    }

    public String describirConCliente(Monedero monedero) {
        Optional<Cliente> dueno = monederoVirtual.getListaClientes().values().stream()
                .filter(c -> c.getListaMonederos().containsValue(monedero))
                .findFirst();
        return dueno.map(c -> describir(monedero) + " - " + c.getNombre()).orElse(describir(monedero));
    }

    private String generarCuenta() {
        String cuenta;
        do {
            cuenta = String.format("CTA-%04d", ++contador);
        } while (monederoVirtual.buscarMonedero(cuenta).isPresent());
        return cuenta;
    }
}
