package co.edu.uniquindio.poo.proyectofinalprogramacion.controller;

import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Cliente;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Frecuencia;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Monedero;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.MonederoVirtual;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.TipoTransaccion;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.TransaccionProgramada;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ProgramadasController {

    public static final String DEPOSITO = "Depósito";
    public static final String RETIRO = "Retiro";
    public static final String TRANSFERENCIA = "Transferencia";

    private static final LocalTime HORA_EJECUCION = LocalTime.of(8, 0);
    private static final int HORAS_RECORDATORIO = 24;

    private MonederoVirtual monederoVirtual;
    private int contador;

    public ProgramadasController(MonederoVirtual monederoVirtual) {
        this.monederoVirtual = monederoVirtual;
    }

    public List<String> obtenerTipos() {
        return List.of(DEPOSITO, RETIRO, TRANSFERENCIA);
    }

    public List<Frecuencia> obtenerFrecuencias() {
        return List.of(Frecuencia.values());
    }

    // Ordenadas por fecha de ejecución
    public List<TransaccionProgramada> obtenerProgramadas(Cliente cliente) {
        return cliente.getListaTransaccionesProgramadas().stream().sorted().toList();
    }

    public void programar(Cliente cliente, String tipo, double monto, Frecuencia frecuencia,
                          LocalDate fecha, Monedero origen, Monedero destino) {
        TransaccionProgramada programada = crear(generarId(), tipo, monto, frecuencia, fecha, origen, destino);
        monederoVirtual.programarTransaccion(cliente.getId(), programada);
    }

    // La programación se reemplaza por una nueva con el mismo código
    public void actualizarProgramacion(Cliente cliente, String codigo, double monto, Frecuencia frecuencia,
                                       LocalDate fecha, Monedero origen, Monedero destino) {
        TransaccionProgramada actual = buscar(cliente, codigo);
        TransaccionProgramada nueva = crear(codigo, obtenerTipo(actual), monto, frecuencia, fecha, origen, destino);
        nueva.setActiva(actual.isActiva());
        monederoVirtual.eliminarTransaccionProgramada(codigo);
        monederoVirtual.programarTransaccion(cliente.getId(), nueva);
    }

    public void alternarEstado(String codigo) {
        TransaccionProgramada programada = monederoVirtual.getListaTransaccionesProgramadas().stream()
                .filter(p -> p.getId().equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No existe la programación " + codigo));
        programada.setActiva(!programada.isActiva());
    }

    // Ejecuta las pendientes y avisa de las próximas
    public int procesarPendientes(Cliente cliente) {
        int procesadas = monederoVirtual.ejecutarProgramadas(cliente.getId()).size();
        monederoVirtual.enviarRecordatoriosProgramadas(cliente.getId(), HORAS_RECORDATORIO);
        return procesadas;
    }

    public void eliminarProgramacion(String codigo) {
        monederoVirtual.eliminarTransaccionProgramada(codigo);
    }

    public String obtenerTipo(TransaccionProgramada programada) {
        return switch (programada.getTipoTransaccion()) {
            case DEPOSITO -> DEPOSITO;
            case RETIRO -> RETIRO;
            case TRANSFERENCIA -> TRANSFERENCIA;
        };
    }

    public Monedero obtenerMonederoOrigen(TransaccionProgramada programada) {
        return monederoVirtual.buscarMonedero(programada.getNumeroCuentaOrigen()).orElse(null);
    }

    public Monedero obtenerMonederoDestino(TransaccionProgramada programada) {
        if (programada.getCuentaDestino() == null) {
            return null;
        }
        return monederoVirtual.buscarMonedero(programada.getCuentaDestino()).orElse(null);
    }

    private TransaccionProgramada crear(String codigo, String tipo, double monto, Frecuencia frecuencia,
                                        LocalDate fecha, Monedero origen, Monedero destino) {
        if (tipo == null) {
            throw new IllegalArgumentException("Seleccione el tipo de transacción");
        }
        if (frecuencia == null) {
            throw new IllegalArgumentException("Seleccione la frecuencia");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("Seleccione la fecha de ejecución");
        }
        if (origen == null) {
            throw new IllegalArgumentException("Seleccione el monedero origen");
        }
        boolean transferencia = TRANSFERENCIA.equals(tipo);
        if (transferencia && destino == null) {
            throw new IllegalArgumentException("Seleccione el monedero destino");
        }
        if (transferencia && destino == origen) {
            throw new IllegalArgumentException("El monedero origen y destino no pueden ser el mismo");
        }
        TipoTransaccion tipoModelo = switch (tipo) {
            case DEPOSITO -> TipoTransaccion.DEPOSITO;
            case RETIRO -> TipoTransaccion.RETIRO;
            case TRANSFERENCIA -> TipoTransaccion.TRANSFERENCIA;
            default -> throw new IllegalArgumentException("Tipo de transacción no válido");
        };
        return new TransaccionProgramada(codigo, tipoModelo, monto, tipo + " programado",
                origen.getNumeroCuenta(), transferencia ? destino.getNumeroCuenta() : null,
                frecuencia, calcularFechaEjecucion(fecha));
    }

    // 8:00 a. m.; si es hoy y ya pasó, un minuto después de ahora
    private LocalDateTime calcularFechaEjecucion(LocalDate fecha) {
        LocalDateTime programada = fecha.atTime(HORA_EJECUCION);
        if (programada.isBefore(LocalDateTime.now()) && !fecha.isBefore(LocalDate.now())) {
            return LocalDateTime.now().plusMinutes(1);
        }
        return programada;
    }

    private TransaccionProgramada buscar(Cliente cliente, String codigo) {
        return cliente.getListaTransaccionesProgramadas().stream()
                .filter(p -> p.getId().equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No existe la programación " + codigo));
    }

    private String generarId() {
        String id;
        do {
            id = String.format("PRG-%03d", ++contador);
        } while (existeId(id));
        return id;
    }

    private boolean existeId(String id) {
        return monederoVirtual.getListaTransaccionesProgramadas().stream()
                .anyMatch(p -> p.getId().equals(id));
    }
}
