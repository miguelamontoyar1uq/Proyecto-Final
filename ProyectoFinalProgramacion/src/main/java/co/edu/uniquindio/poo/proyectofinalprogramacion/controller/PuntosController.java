package co.edu.uniquindio.poo.proyectofinalprogramacion.controller;

import co.edu.uniquindio.poo.proyectofinalprogramacion.exceptions.MonederoInactivoException;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.CatalogoBeneficio;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Cliente;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Monedero;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.MonederoVirtual;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.RegistroPuntos;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.TipoBeneficio;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class PuntosController {

    private static final int LIMITE_BRONCE = 500;
    private static final int LIMITE_PLATA = 1000;
    private static final int LIMITE_ORO = 5000;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private MonederoVirtual monederoVirtual;

    public PuntosController(MonederoVirtual monederoVirtual) {
        this.monederoVirtual = monederoVirtual;
    }

    public List<TipoBeneficio> obtenerBeneficios() {
        return List.of(TipoBeneficio.values());
    }

    public CatalogoBeneficio canjearBeneficio(Cliente cliente, TipoBeneficio tipo, Monedero monederoBono) {
        if (tipo == null) {
            throw new IllegalArgumentException("Seleccione un beneficio");
        }
        if (tipo == TipoBeneficio.BONO_SALDO && monederoBono == null) {
            throw new IllegalArgumentException("Seleccione el monedero que recibirá el bono");
        }
        try {
            return monederoVirtual.canjearBeneficio(cliente.getId(), tipo,
                    monederoBono == null ? null : monederoBono.getNumeroCuenta());
        } catch (MonederoInactivoException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    // Los más recientes primero
    public List<RegistroPuntos> obtenerHistorialPuntos(Cliente cliente) {
        return cliente.getHistorialPuntos().stream()
                .sorted(Comparator.comparing(RegistroPuntos::fecha).reversed())
                .toList();
    }

    public List<CatalogoBeneficio> obtenerBeneficiosCanjeados(Cliente cliente) {
        return cliente.getBeneficiosActivos().stream()
                .sorted(Comparator.comparing(CatalogoBeneficio::getFechaActivacion).reversed())
                .toList();
    }

    // DESCUENTO_TRANSFERENCIA -> "Descuento transferencia"
    public String obtenerNombre(TipoBeneficio tipo) {
        String texto = tipo.name().replace('_', ' ').toLowerCase();
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    public String obtenerDuracion(TipoBeneficio tipo) {
        return tipo == TipoBeneficio.BONO_SALDO ? "Inmediato" : "1 mes";
    }

    public String obtenerVigencia(CatalogoBeneficio beneficio) {
        if (beneficio.getTipo() == TipoBeneficio.BONO_SALDO) {
            return "Acreditado";
        }
        return beneficio.estaVigente()
                ? "Hasta " + beneficio.getFechaExpiracion().format(FORMATO_FECHA)
                : "Vencido";
    }

    public String obtenerPuntosConSigno(RegistroPuntos registro) {
        return ("GANADOS".equals(registro.tipo()) ? "+" : "-") + registro.cantidadPuntos();
    }

    // Avance dentro del rango actual (0 a 1)
    public double obtenerProgresoRango(Cliente cliente) {
        int puntos = cliente.getPuntosAcumulados();
        return switch (cliente.getRango()) {
            case BRONCE -> puntos / (LIMITE_BRONCE + 1.0);
            case PLATA -> (puntos - LIMITE_BRONCE - 1) / (double) (LIMITE_PLATA - LIMITE_BRONCE);
            case ORO -> (puntos - LIMITE_PLATA - 1) / (double) (LIMITE_ORO - LIMITE_PLATA);
            case PLATINO -> 1.0;
        };
    }

    public String obtenerTextoProgreso(Cliente cliente) {
        int puntos = cliente.getPuntosAcumulados();
        return switch (cliente.getRango()) {
            case BRONCE -> "Faltan " + (LIMITE_BRONCE + 1 - puntos) + " puntos para PLATA";
            case PLATA -> "Faltan " + (LIMITE_PLATA + 1 - puntos) + " puntos para ORO";
            case ORO -> "Faltan " + (LIMITE_ORO + 1 - puntos) + " puntos para PLATINO";
            case PLATINO -> "Rango máximo alcanzado";
        };
    }

    // Comisión de transferencia por cada 100, ya con beneficios
    public String obtenerTarifaTransferencia(Cliente cliente) {
        double porcentaje = monederoVirtual.calcularComisionTransferencia(cliente, 100);
        return new DecimalFormat("0.#").format(porcentaje) + "%";
    }
}
