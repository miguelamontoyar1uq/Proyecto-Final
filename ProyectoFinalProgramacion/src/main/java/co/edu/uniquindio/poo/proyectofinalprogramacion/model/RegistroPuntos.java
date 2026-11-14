package co.edu.uniquindio.poo.proyectofinal.model;

import java.time.LocalDateTime;

public record RegistroPuntos(
        LocalDateTime fecha,
        int cantidadPuntos,
        String tipo,
        String motivo) {

    public RegistroPuntos(int cantidadPuntos, String tipo, String motivo) {
        this(LocalDateTime.now(), cantidadPuntos, tipo, motivo);
    }
}