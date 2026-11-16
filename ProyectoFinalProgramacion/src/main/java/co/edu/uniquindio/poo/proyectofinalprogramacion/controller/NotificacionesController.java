package co.edu.uniquindio.poo.proyectofinalprogramacion.controller;

import co.edu.uniquindio.poo.proyectofinalprogramacion.model.CanalGmail;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.CanalSms;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.CanalWhatsapp;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Cliente;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Notificacion;

import java.util.Comparator;
import java.util.List;

public class NotificacionesController {

    public List<String> obtenerCanales() {
        return List.of(new CanalGmail().getNombre(), new CanalWhatsapp().getNombre(), new CanalSms().getNombre());
    }

    // Canal nulo significa sin filtro; las más recientes primero
    public List<Notificacion> obtenerNotificaciones(Cliente cliente, String canal) {
        return cliente.getNotificaciones().stream()
                .filter(n -> canal == null || n.getCanal().equals(canal))
                .sorted(Comparator.comparing(Notificacion::getFecha).reversed())
                .toList();
    }

    public void eliminarNotificacion(Cliente cliente, String id) {
        cliente.eliminarNotificacion(id);
    }
}
