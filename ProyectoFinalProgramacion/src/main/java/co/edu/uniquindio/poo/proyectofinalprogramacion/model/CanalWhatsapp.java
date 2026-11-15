package co.edu.uniquindio.poo.proyectofinalprogramacion.model;

public class CanalWhatsapp implements CanalNotificacion {
    @Override
    public String getNombre() {
        return "WhatsApp";
    }

    @Override
    public void enviar(Cliente cliente, Notificacion notificacion) {
        System.out.println("[WhatsApp -> " + cliente.getTelefono() + "] " + notificacion.getMensaje());
    }
}