package co.edu.uniquindio.poo.proyectofinalprogramacion.model;

import java.util.List;

public record AnalisisGasto(
        double promedio,
        double desviacionEstandar,
        double umbralAtipico,
        List<Transaccion> transaccionesSospechosas) {
}
