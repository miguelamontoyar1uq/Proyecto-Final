package co.edu.uniquindio.poo.proyectofinalprogramacion.model;

public class CanalGmail implements CanalNotificacion {
    @Override
    public String getNombre() {
        return "Gmail";
    }

    @Override
    public void enviar(Cliente cliente, Notificacion notificacion) {
        System.out.println("[Gmail -> " + cliente.getCorreo() + "] " + notificacion.getMensaje());
    }
}