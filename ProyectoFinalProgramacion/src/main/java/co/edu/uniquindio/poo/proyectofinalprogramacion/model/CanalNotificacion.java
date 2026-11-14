package co.edu.uniquindio.poo.proyectofinal.model;

public interface CanalNotificacion {
    String getNombre();
    void enviar(Cliente cliente, Notificacion notificacion);
}