package co.edu.uniquindio.poo.proyectofinalprogramacion.model;

import java.time.LocalDateTime;

public class TransaccionProgramada implements Comparable<TransaccionProgramada> {
    private final String id;
    private TipoTransaccion tipoTransaccion; // "Depósito" o "Transferencia"
    private final double monto;
    private final String descripcion;
    private final String numeroCuentaOrigen;
    private final String cuentaDestino; // solo se usa en transferencias
    private final Frecuencia frecuencia;
    private LocalDateTime fechaEjecucion;
    private boolean activa;

    public TransaccionProgramada(String id, TipoTransaccion tipoTransaccion, double monto, String descripcion,
                                 String numeroCuentaOrigen, String cuentaDestino,
                                 Frecuencia frecuencia, LocalDateTime fechaEjecucion) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID no puede estar vacío.");
        }
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero.");
        }
        if (fechaEjecucion == null || fechaEjecucion.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de ejecución debe ser futura.");
        }
        this.id = id;
        this.tipoTransaccion=tipoTransaccion;
        this.monto = monto;
        this.descripcion = descripcion;
        this.numeroCuentaOrigen = numeroCuentaOrigen;
        this.cuentaDestino = cuentaDestino;
        this.frecuencia = frecuencia;
        this.fechaEjecucion = fechaEjecucion;
        this.activa = true;
    }

    // Se usa después de ejecutarla: mueve la fecha a la siguiente vez
    public void reprogramar() {
        LocalDateTime ahora = LocalDateTime.now();
        do {
            fechaEjecucion = switch (frecuencia) {
                case DIARIA -> fechaEjecucion.plusDays(1);
                case SEMANAL -> fechaEjecucion.plusWeeks(1);
                case MENSUAL -> fechaEjecucion.plusMonths(1);
            };
        } while (!fechaEjecucion.isAfter(ahora));
    }

    public boolean estaPendiente() {
        return activa && !fechaEjecucion.isAfter(LocalDateTime.now());
    }

    // Para ordenar por fecha de ejecución (la más próxima primero)
    @Override
    public int compareTo(TransaccionProgramada otra) {
        return this.fechaEjecucion.compareTo(otra.fechaEjecucion);
    }

    public TipoTransaccion getTipoTransaccion() {
        return tipoTransaccion;
    }
    public String getId() { return id; }
    public double getMonto() { return monto; }
    public String getDescripcion() { return descripcion; }
    public String getNumeroCuentaOrigen() { return numeroCuentaOrigen; }
    public String getCuentaDestino() { return cuentaDestino; }
    public Frecuencia getFrecuencia() { return frecuencia; }
    public LocalDateTime getFechaEjecucion() { return fechaEjecucion; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}