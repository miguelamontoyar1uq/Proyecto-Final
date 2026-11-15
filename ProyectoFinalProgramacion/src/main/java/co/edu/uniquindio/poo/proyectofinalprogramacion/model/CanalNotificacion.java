package co.edu.uniquindio.poo.proyectofinalprogramacion.model;

public interface CanalNotificacion {
    String getNombre();
    void enviar(Cliente cliente, Notificacion notificacion);
}