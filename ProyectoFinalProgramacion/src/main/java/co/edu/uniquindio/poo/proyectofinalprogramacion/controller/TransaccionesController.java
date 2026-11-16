package co.edu.uniquindio.poo.proyectofinalprogramacion.controller;

import co.edu.uniquindio.poo.proyectofinalprogramacion.exceptions.LimiteRetiroExcedidoException;
import co.edu.uniquindio.poo.proyectofinalprogramacion.exceptions.MonederoInactivoException;
import co.edu.uniquindio.poo.proyectofinalprogramacion.exceptions.SaldoInsuficienteException;
import co.edu.uniquindio.poo.proyectofinalprogramacion.exceptions.TransaccionNoReversibleException;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.*;

import java.time.LocalDate;
import java.util.List;

public class TransaccionesController {

    public static final String DEPOSITO = "Depósito";
    public static final String RETIRO = "Retiro";
    public static final String TRANSFERENCIA = "Transferencia";

    private MonederoVirtual monederoVirtual;

    public TransaccionesController(MonederoVirtual monederoVirtual) {
        this.monederoVirtual = monederoVirtual;
    }

    public List<String> obtenerTiposOperacion() {
        return List.of(DEPOSITO, RETIRO, TRANSFERENCIA);
    }

    // Cargo o comisión que se cobraría
    public double calcularCargo(Cliente cliente, String tipo, double monto) {
        if (tipo == null) {
            throw new IllegalArgumentException("Seleccione el tipo de operación");
        }
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero.");
        }
        return switch (tipo) {
            case RETIRO -> cliente.tieneBeneficioVigente(TipoBeneficio.SIN_CARGOS_RETIRO)
                    ? 0 : monto * Monedero.TASA_CARGO_RETIRO;
            case TRANSFERENCIA -> monederoVirtual.calcularComisionTransferencia(cliente, monto);
            default -> 0;
        };
    }

    // Puntos según la regla de cada tipo de transacción
    public int calcularPuntos(Cliente cliente, String tipo, double monto) {
        if (tipo == null) {
            throw new IllegalArgumentException("Seleccione el tipo de operación");
        }
        Transaccion vistaPrevia = switch (tipo) {
            case DEPOSITO -> new Deposito("previa", monto, "Vista previa", EstadoTransaccion.COMPLETADA);
            case RETIRO -> new Retiro("previa", monto, "Vista previa", EstadoTransaccion.COMPLETADA, 0);
            case TRANSFERENCIA -> new Transferencia("previa", monto, "Vista previa", "",
                    EstadoTransaccion.COMPLETADA, 0);
            default -> throw new IllegalArgumentException("Tipo de operación no válido");
        };
        return vistaPrevia.calcularPuntosGenerados();
    }

    public void ejecutarOperacion(Cliente cliente, String tipo, Monedero origen, Monedero destino, double monto) {
        if (tipo == null) {
            throw new IllegalArgumentException("Seleccione el tipo de operación");
        }
        if (origen == null) {
            throw new IllegalArgumentException("Seleccione el monedero origen");
        }
        try {
            switch (tipo) {
                case DEPOSITO -> monederoVirtual.depositar(cliente.getId(), origen.getNumeroCuenta(), monto);
                case RETIRO -> monederoVirtual.retirar(cliente.getId(), origen.getNumeroCuenta(), monto);
                case TRANSFERENCIA -> {
                    if (destino == null) {
                        throw new IllegalArgumentException("Seleccione el monedero destino");
                    }
                    monederoVirtual.transferir(cliente.getId(), origen.getNumeroCuenta(),
                            destino.getNumeroCuenta(), monto);
                }
                default -> throw new IllegalArgumentException("Tipo de operación no válido");
            }
        } catch (MonederoInactivoException | SaldoInsuficienteException | LimiteRetiroExcedidoException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    // Filtros opcionales: nulo significa sin filtro
    public List<Transaccion> obtenerTransacciones(Cliente cliente, Monedero monedero, String tipo,
                                                  LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final");
        }
        List<Transaccion> base = monedero == null
                ? monederoVirtual.consultarHistorial(cliente.getId())
                : monederoVirtual.consultarHistorial(cliente.getId(), monedero.getNumeroCuenta());
        return base.stream()
                .filter(t -> tipo == null || t.getTipo().equals(tipo))
                .filter(t -> desde == null || !t.getFecha().toLocalDate().isBefore(desde))
                .filter(t -> hasta == null || !t.getFecha().toLocalDate().isAfter(hasta))
                .toList();
    }

    public void revertirTransaccion(Cliente cliente, Transaccion transaccion) {
        try {
            monederoVirtual.revertirTransaccion(cliente.getId(), transaccion.getId());
        } catch (MonederoInactivoException | SaldoInsuficienteException | TransaccionNoReversibleException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public String obtenerTipo(Transaccion transaccion) {
        return transaccion.getTipo();
    }
}
