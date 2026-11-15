package co.edu.uniquindio.poo.proyectofinalprogramacion.model;

import co.edu.uniquindio.poo.proyectofinal.exceptions.LimiteRetiroExcedidoException;
import co.edu.uniquindio.poo.proyectofinal.exceptions.MonederoInactivoException;
import co.edu.uniquindio.poo.proyectofinal.exceptions.SaldoInsuficienteException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class Monedero {
    private final String numeroCuenta;
    private double saldo;
    private boolean activo;
    private final List<Transaccion> historialTransacciones;
    private static final double TASA_CARGO_RETIRO = 0.01;

    public Monedero(String numeroCuenta, double saldoInicial) {
        if (numeroCuenta == null || numeroCuenta.isBlank()) {
            throw new IllegalArgumentException("El número de cuenta no puede estar vacío.");
        }
        if (saldoInicial < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo.");
        }
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldoInicial;
        this.activo = true;
        this.historialTransacciones = new ArrayList<>();
    }

    public Deposito depositar(double monto) throws MonederoInactivoException {
        if (!activo) {
            throw new MonederoInactivoException("El monedero " + numeroCuenta + " se encuentra inactivo.");
        }
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto a depositar debe ser mayor a cero.");
        }
        this.saldo += monto;

        Deposito deposito = new Deposito(UUID.randomUUID().toString(), monto,
                "Depósito en " + numeroCuenta, EstadoTransaccion.COMPLETADA);
        registrarTransaccion(deposito);
        return deposito;
    }

    public Retiro retirar(double monto)
            throws MonederoInactivoException, SaldoInsuficienteException, LimiteRetiroExcedidoException {
        return retirar(monto, false);
    }

    // exentoCargo = true cuando el cliente tiene el beneficio SIN_CARGOS_RETIRO vigente
    public Retiro retirar(double monto, boolean exentoCargo)
            throws MonederoInactivoException, SaldoInsuficienteException, LimiteRetiroExcedidoException {
        if (!activo) {
            throw new MonederoInactivoException("El monedero " + numeroCuenta + " se encuentra inactivo.");
        }
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto a retirar debe ser mayor a cero.");
        }
        if (!validarLimiteRetiro(monto)) {
            throw new LimiteRetiroExcedidoException("El monto excede el límite máximo permitido para este monedero.");
        }
        double cargoRetiro = exentoCargo ? 0 : monto * TASA_CARGO_RETIRO;
        double totalDescontar = monto + cargoRetiro;
        if (totalDescontar > saldo) {
            throw new SaldoInsuficienteException("Saldo insuficiente en la cuenta " + numeroCuenta);
        }
        this.saldo -= totalDescontar;

        Retiro retiro = new Retiro(UUID.randomUUID().toString(), monto,
                "Retiro de " + numeroCuenta, EstadoTransaccion.COMPLETADA, cargoRetiro);
        registrarTransaccion(retiro);
        return retiro;
    }

    // Mueven saldo sin registrar transacción (los usa la transferencia)
    public void debitar(double monto) throws MonederoInactivoException, SaldoInsuficienteException {
        if (!activo) {
            throw new MonederoInactivoException("El monedero " + numeroCuenta + " se encuentra inactivo.");
        }
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto a debitar debe ser mayor a cero.");
        }
        if (monto > saldo) {
            throw new SaldoInsuficienteException("Saldo insuficiente en la cuenta " + numeroCuenta);
        }
        this.saldo -= monto;
    }

    public void acreditar(double monto) throws MonederoInactivoException {
        if (!activo) {
            throw new MonederoInactivoException("El monedero " + numeroCuenta + " se encuentra inactivo.");
        }
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto a acreditar debe ser mayor a cero.");
        }
        this.saldo += monto;
    }

    public void registrarTransaccion(Transaccion transaccion) {
        if (transaccion == null) {
            throw new IllegalArgumentException("La transacción no puede ser nula.");
        }
        this.historialTransacciones.add(transaccion);
    }

    // Cada tipo de monedero decide si genera una alerta (polimorfismo, sin instanceof)
    public Optional<String> generarAlerta() {
        return Optional.empty();
    }

    // Métodos abstractos obligatorios
    public abstract boolean validarLimiteRetiro(double monto);
    public abstract String getTipoMonedero();

    // Getters
    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public double getSaldo() {
        return saldo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<Transaccion> getHistorialTransacciones() {
        return Collections.unmodifiableList(historialTransacciones);
    }
}