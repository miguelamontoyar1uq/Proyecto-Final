package co.edu.uniquindio.poo.proyectofinal.model;

import java.time.LocalDateTime;

public class Notificacion {
    private final String id;
    private final String mensaje;
    private final LocalDateTime fecha;
    private final String canal; // gmail, whatsap sms

    public Notificacion(String id, String mensaje, String canal) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("El ID no puede estar vacío.");
        if (mensaje == null || mensaje.isBlank()) throw new IllegalArgumentException("El mensaje no puede estar vacío.");

        this.id = id;
        this.mensaje = mensaje;
        this.canal = canal;
        this.fecha = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getFecha() { return fecha; }
    public String getCanal() { return canal; }
}