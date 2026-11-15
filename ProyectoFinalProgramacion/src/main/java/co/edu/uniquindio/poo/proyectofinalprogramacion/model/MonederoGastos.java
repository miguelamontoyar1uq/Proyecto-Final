package co.edu.uniquindio.poo.proyectofinalprogramacion.model;

import java.util.Optional;

public class MonederoGastos extends Monedero {
    private final double limiteRetiro;
    private double umbralSaldoBajo;

    public MonederoGastos(String numeroCuenta, double saldoInicial, double limiteRetiro, double umbralSaldoBajo) {
        super(numeroCuenta, saldoInicial);
        if (limiteRetiro <= 0) {
            throw new IllegalArgumentException("El límite de retiro debe ser mayor a cero.");
        }
        if (umbralSaldoBajo < 0) {
            throw new IllegalArgumentException("El umbral de saldo bajo no puede ser negativo.");
        }
        this.limiteRetiro = limiteRetiro;
        this.umbralSaldoBajo = umbralSaldoBajo;
    }

    @Override
    public boolean validarLimiteRetiro(double monto) {
        return monto <= limiteRetiro;
    }

    @Override
    public String getTipoMonedero() {
        return "Gastos";
    }

    @Override
    public Optional<String> generarAlerta() {
        if (isSaldoBajo()) {
            return Optional.of("Alerta de saldo bajo en el monedero " + getNumeroCuenta()
                    + ". Saldo actual: " + getSaldo());
        }
        return Optional.empty();
    }

    public boolean isSaldoBajo() {
        return getSaldo() <= umbralSaldoBajo;
    }

    public double getLimiteRetiro() {
        return limiteRetiro;
    }

    public double getUmbralSaldoBajo() {
        return umbralSaldoBajo;
    }

    public void setUmbralSaldoBajo(double umbralSaldoBajo) {
        if (umbralSaldoBajo < 0) {
            throw new IllegalArgumentException("El umbral de saldo bajo no puede ser negativo.");
        }
        this.umbralSaldoBajo = umbralSaldoBajo;
    }
}