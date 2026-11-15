package co.edu.uniquindio.poo.proyectofinalprogramacion.model;

import java.time.LocalDateTime;

public abstract class Transaccion {
    private final String id;
    private final LocalDateTime fecha;
    private final double monto;
    private final String descripcion;
    private boolean esSospechosa;
    private EstadoTransaccion estado;

    public Transaccion(String id, double monto, String descripcion, EstadoTransaccion estado) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID de la transacción no puede estar vacío.");
        }
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto de la transacción debe ser mayor a cero.");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo.");
        }
        this.id = id;
        this.fecha = LocalDateTime.now();
        this.monto = monto;
        this.descripcion = descripcion;
        this.esSospechosa = false;
        this.estado = estado;
    }

    // Evita tener que usar instanceof para saber el tipo
    public abstract String getTipo();

    // Cada tipo de transacción define cuántos puntos genera
    public abstract int calcularPuntosGenerados();

    public String getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public double getMonto() {
        return monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isEsSospechosa() {
        return esSospechosa;
    }

    public EstadoTransaccion getEstado() {
        return estado;
    }

    public int getPuntosGenerados() {
        return calcularPuntosGenerados();
    }

    public void marcarComoSospechosa() {
        this.esSospechosa = true;
    }

    public void setEstado(EstadoTransaccion estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo.");
        }
        this.estado = estado;
    }
}