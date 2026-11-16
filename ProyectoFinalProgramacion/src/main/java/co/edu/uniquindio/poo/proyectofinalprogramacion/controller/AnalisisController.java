package co.edu.uniquindio.poo.proyectofinalprogramacion.controller;

import co.edu.uniquindio.poo.proyectofinalprogramacion.model.AnalisisGasto;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Cliente;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.MonederoVirtual;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Transaccion;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AnalisisController {

    private MonederoVirtual monederoVirtual;

    public AnalisisController(MonederoVirtual monederoVirtual) {
        this.monederoVirtual = monederoVirtual;
    }

    // Promedio, desviación estándar y umbral atípico del historial
    public AnalisisGasto analizar(Cliente cliente) {
        List<Transaccion> historial = monederoVirtual.consultarHistorial(cliente.getId());
        if (historial.isEmpty()) {
            throw new IllegalStateException("El cliente no tiene transacciones para analizar");
        }
        double promedio = historial.stream()
                .mapToDouble(Transaccion::getMonto)
                .average()
                .orElse(0);
        double desviacion = Math.sqrt(historial.stream()
                .mapToDouble(t -> Math.pow(t.getMonto() - promedio, 2))
                .average()
                .orElse(0));
        double umbral = promedio + 2 * desviacion;
        List<Transaccion> sospechosas = historial.stream()
                .filter(Transaccion::isEsSospechosa)
                .toList();
        return new AnalisisGasto(promedio, desviacion, umbral, sospechosas);
    }

    public Map<String, Double> obtenerGastosPorMonedero(Cliente cliente) {
        return new TreeMap<>(monederoVirtual.gastoPorMonedero(cliente.getId()));
    }
}
