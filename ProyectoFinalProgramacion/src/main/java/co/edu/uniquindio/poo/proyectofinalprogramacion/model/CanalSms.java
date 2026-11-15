package co.edu.uniquindio.poo.proyectofinalprogramacion.model;

public class CanalSms implements CanalNotificacion {
    @Override
    public String getNombre() {
        return "SMS";
    }

    @Override
    public void enviar(Cliente cliente, Notificacion notificacion) {
        System.out.println("[SMS -> " + cliente.getTelefono() + "] " + notificacion.getMensaje());
    }
}