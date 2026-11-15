package co.edu.uniquindio.poo.proyectofinalprogramacion.model;

import java.util.Optional;

public class MonederoAhorros extends Monedero {
    private final double limiteRetiro;
    private double metaAhorro;

    public MonederoAhorros(String numeroCuenta, double saldoInicial, double limiteRetiro, double metaAhorro) {
        super(numeroCuenta, saldoInicial);
        if (limiteRetiro <= 0) {
            throw new IllegalArgumentException("El límite de retiro debe ser mayor a cero.");
        }
        if (metaAhorro <= 0) {
            throw new IllegalArgumentException("La meta de ahorro debe ser mayor a cero.");
        }
        this.limiteRetiro = limiteRetiro;
        this.metaAhorro = metaAhorro;
    }

    @Override
    public boolean validarLimiteRetiro(double monto) {
        return monto <= limiteRetiro;
    }

    @Override
    public String getTipoMonedero() {
        return "Ahorros";
    }

    @Override
    public Optional<String> generarAlerta() {
        if (isMetaAlcanzada()) {
            return Optional.of("¡Meta de ahorro alcanzada en el monedero " + getNumeroCuenta()
                    + "! Saldo actual: " + getSaldo());
        }
        return Optional.empty();
    }

    public boolean isMetaAlcanzada() {
        return getSaldo() >= metaAhorro;
    }

    // Porcentaje de avance hacia la meta (máximo 100)
    public double getProgresoMeta() {
        return Math.min(100, getSaldo() / metaAhorro * 100);
    }

    public double getLimiteRetiro() {
        return limiteRetiro;
    }

    public double getMetaAhorro() {
        return metaAhorro;
    }

    public void setMetaAhorro(double metaAhorro) {
        if (metaAhorro <= 0) {
            throw new IllegalArgumentException("La meta de ahorro debe ser mayor a cero.");
        }
        this.metaAhorro = metaAhorro;
    }
}