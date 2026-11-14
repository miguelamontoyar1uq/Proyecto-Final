package co.edu.uniquindio.poo.proyectofinal.model;

import java.time.LocalDateTime;

public class CatalogoBeneficio {
    private final TipoBeneficio tipo;
    private final LocalDateTime fechaActivacion;
    private final LocalDateTime fechaExpiracion;

    public CatalogoBeneficio(TipoBeneficio tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de beneficio no puede ser nulo.");
        }
        this.tipo = tipo;
        this.fechaActivacion = LocalDateTime.now();
        this.fechaExpiracion = fechaActivacion.plusMonths(1);
    }

    public boolean estaVigente() {
        return LocalDateTime.now().isBefore(fechaExpiracion);
    }

    public TipoBeneficio getTipo() {
        return tipo;
    }

    public LocalDateTime getFechaActivacion() {
        return fechaActivacion;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }
}