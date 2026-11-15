package co.edu.uniquindio.poo.proyectofinalprogramacion.model;

import co.edu.uniquindio.poo.proyectofinalprogramacion.exceptions.LimiteRetiroExcedidoException;
import co.edu.uniquindio.poo.proyectofinalprogramacion.exceptions.MonederoInactivoException;
import co.edu.uniquindio.poo.proyectofinalprogramacion.exceptions.SaldoInsuficienteException;
import co.edu.uniquindio.poo.proyectofinalprogramacion.exceptions.TransaccionNoReversibleException;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class MonederoVirtual {
    private static final double BONO_SALDO_UNIDADES = 50;
    private static final double FACTOR_DESCUENTO_COMISION = 0.90; // reducción del 10% de la comisión
    private static final String CANAL_POR_DEFECTO = "Gmail";

    private final String nombre;
    private Map<String, Cliente> listaClientes;
    private List<Transaccion> listaTransacciones;
    private List<TransaccionProgramada> listaTransaccionesProgramadas;
    private final Map<String, CanalNotificacion> canales;

    public MonederoVirtual(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del monedero virtual no puede estar vacío.");
        }
        this.nombre = nombre;
        listaClientes = new HashMap<>();
        listaTransacciones = new ArrayList<>();
        listaTransaccionesProgramadas = new ArrayList<>();
        canales = new HashMap<>();
        registrarCanal(new CanalGmail());
        registrarCanal(new CanalWhatsapp());
        registrarCanal(new CanalSms());
    }

    // ---------- Clientes y búsquedas ----------

    public void registrarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        }
        if (listaClientes.containsKey(cliente.getId())) {
            throw new IllegalArgumentException("Ya existe un cliente con la identificación " + cliente.getId());
        }
        this.listaClientes.put(cliente.getId(), cliente);
    }

    public Optional<Cliente> buscarCliente(String id) {
        return Optional.ofNullable(listaClientes.get(id));
    }

    public Cliente obtenerCliente(String idCliente) {
        return buscarCliente(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("No existe el cliente " + idCliente));
    }

    public Optional<Monedero> buscarMonedero(String numeroCuenta) {
        return listaClientes.values().stream()
                .flatMap(c -> c.getListaMonederos().values().stream())
                .filter(m -> m.getNumeroCuenta().equals(numeroCuenta))
                .findFirst();
    }

    public Monedero obtenerMonederoDelCliente(Cliente cliente, String numeroCuenta) {
        return cliente.getListaMonederos().values().stream()
                .filter(m -> m.getNumeroCuenta().equals(numeroCuenta))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La cuenta " + numeroCuenta + " no pertenece al cliente."));
    }

    public void registrarTransaccionGlobal(Transaccion transaccion) {
        if (transaccion != null) {
            this.listaTransacciones.add(transaccion);
        }
    }

    public void agregarTransaccionProgramada(TransaccionProgramada transaccion) {
        if (transaccion != null) {
            this.listaTransaccionesProgramadas.add(transaccion);
        }
    }

    // ---------- Notificaciones (usan la interfaz CanalNotificacion) ----------

    public void registrarCanal(CanalNotificacion canal) {
        if (canal == null) {
            throw new IllegalArgumentException("El canal no puede ser nulo.");
        }
        canales.put(canal.getNombre().toLowerCase(), canal);
    }

    // Crea la notificación, la guarda en el cliente y la envía por el canal elegido
    public Notificacion notificar(Cliente cliente, String mensaje, String nombreCanal) {
        if (nombreCanal == null) {
            throw new IllegalArgumentException("El canal no puede ser nulo.");
        }
        CanalNotificacion canal = canales.get(nombreCanal.toLowerCase());
        if (canal == null) {
            throw new IllegalArgumentException("Canal de notificación no soportado: " + nombreCanal);
        }
        Notificacion notificacion = new Notificacion(UUID.randomUUID().toString(), mensaje, canal.getNombre());
        cliente.recibirNotificacion(notificacion);
        canal.enviar(cliente, notificacion);
        return notificacion;
    }

    private void notificar(Cliente cliente, String mensaje) {
        notificar(cliente, mensaje, CANAL_POR_DEFECTO);
    }

    // Cada monedero decide si genera alerta (saldo bajo, meta de ahorro)
    private void verificarAlertasMonedero(Cliente cliente, Monedero monedero) {
        monedero.generarAlerta().ifPresent(mensaje -> notificar(cliente, mensaje));
    }

    private void verificarTransaccionAtipica(Cliente cliente, Transaccion transaccion) {
        boolean atipica = cliente.obtenerTransaccionesAtipicas().contains(transaccion);

        if (atipica && !transaccion.isEsSospechosa()) {
            transaccion.marcarComoSospechosa();
            notificar(cliente, "Alerta: la transacción " + transaccion.getId() + " ("
                    + transaccion.getTipo() + ") por " + transaccion.getMonto()
                    + " es atípica respecto a tu historial.");
        }
    }

    // Avisa de las programadas que se ejecutarán dentro de las próximas horas indicadas
    public void enviarRecordatoriosProgramadas(String idCliente, int horasAnticipacion) {
        if (horasAnticipacion <= 0) {
            throw new IllegalArgumentException("Las horas de anticipación deben ser mayores a cero.");
        }
        Cliente cliente = obtenerCliente(idCliente);
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusHours(horasAnticipacion);

        cliente.getListaTransaccionesProgramadas().stream()
                .filter(TransaccionProgramada::isActiva)
                .filter(p -> p.getFechaEjecucion().isAfter(ahora) && !p.getFechaEjecucion().isAfter(limite))
                .sorted()
                .forEach(p -> notificar(cliente, "Recordatorio: tu " + p.getTipoTransaccion()
                                + " programada por " + p.getMonto() + " se ejecutará el " + p.getFechaEjecucion() + ".",
                        "WhatsApp"));
    }

    // ---------- Comisiones y beneficios ----------

    // Tarifa según el rango del cliente, con descuento extra si canjeó el beneficio
    private double calcularComisionTransferencia(Cliente cliente, double monto) {
        double tasa = switch (cliente.getRango()) {
            case BRONCE -> 0.10;
            case PLATA -> 0.08;
            case ORO -> 0.06;
            case PLATINO -> 0.04;
        };
        double comision = monto * tasa;
        if (cliente.tieneBeneficioVigente(TipoBeneficio.DESCUENTO_TRANSFERENCIA)) {
            comision *= FACTOR_DESCUENTO_COMISION;
        }
        return comision;
    }

    // Para DESCUENTO_TRANSFERENCIA y SIN_CARGOS_RETIRO pasa cuentaBono = null.
    // Para BONO_SALDO, cuentaBono es el monedero donde se acredita el bono.
    public CatalogoBeneficio canjearBeneficio(String idCliente, TipoBeneficio tipo, String cuentaBono)
            throws MonederoInactivoException {

        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de beneficio no puede ser nulo.");
        }
        Cliente cliente = obtenerCliente(idCliente);

        // Se valida el monedero antes de gastar los puntos
        Monedero monederoBono = null;
        if (tipo == TipoBeneficio.BONO_SALDO) {
            monederoBono = obtenerMonederoDelCliente(cliente, cuentaBono);
            if (!monederoBono.isActivo()) {
                throw new MonederoInactivoException("El monedero " + cuentaBono + " se encuentra inactivo.");
            }
        }

        CatalogoBeneficio beneficio = cliente.canjearBeneficio(tipo);

        if (monederoBono != null) {
            monederoBono.acreditar(BONO_SALDO_UNIDADES);
        }

        notificar(cliente, "Canjeaste: " + tipo.getDescripcion() + ". Puntos restantes: "
                + cliente.getPuntosAcumulados());
        return beneficio;
    }

    // ---------- Operaciones ----------

    public Deposito depositar(String idCliente, String numeroCuenta, double monto)
            throws MonederoInactivoException {
        Cliente cliente = obtenerCliente(idCliente);
        Monedero monedero = obtenerMonederoDelCliente(cliente, numeroCuenta);

        Deposito deposito = monedero.depositar(monto);
        registrarTransaccionGlobal(deposito);
        cliente.acumularPuntos(deposito);
        verificarTransaccionAtipica(cliente, deposito);
        verificarAlertasMonedero(cliente, monedero);
        return deposito;
    }

    public Retiro retirar(String idCliente, String numeroCuenta, double monto)
            throws MonederoInactivoException, SaldoInsuficienteException, LimiteRetiroExcedidoException {
        Cliente cliente = obtenerCliente(idCliente);
        Monedero monedero = obtenerMonederoDelCliente(cliente, numeroCuenta);

        boolean exentoCargo = cliente.tieneBeneficioVigente(TipoBeneficio.SIN_CARGOS_RETIRO);
        Retiro retiro = monedero.retirar(monto, exentoCargo);
        registrarTransaccionGlobal(retiro);
        cliente.acumularPuntos(retiro);
        verificarTransaccionAtipica(cliente, retiro);
        verificarAlertasMonedero(cliente, monedero);
        return retiro;
    }

    public Transferencia transferir(String idCliente, String cuentaOrigen, String cuentaDestino, double monto)
            throws MonederoInactivoException, SaldoInsuficienteException {

        if (monto <= 0) {
            throw new IllegalArgumentException("El monto a transferir debe ser mayor a cero.");
        }
        if (cuentaOrigen.equals(cuentaDestino)) {
            throw new IllegalArgumentException("La cuenta origen y destino no pueden ser la misma.");
        }

        Cliente cliente = obtenerCliente(idCliente);
        Monedero origen = obtenerMonederoDelCliente(cliente, cuentaOrigen);
        Monedero destino = buscarMonedero(cuentaDestino)
                .orElseThrow(() -> new IllegalArgumentException("No existe la cuenta destino " + cuentaDestino));

        // Se revisa antes de mover plata, para no debitar y luego fallar al acreditar
        if (!destino.isActivo()) {
            throw new MonederoInactivoException("El monedero destino " + cuentaDestino + " se encuentra inactivo.");
        }

        double comision = calcularComisionTransferencia(cliente, monto);
        origen.debitar(monto + comision);
        destino.acreditar(monto);

        Transferencia transferencia = new Transferencia(UUID.randomUUID().toString(), monto,
                "Transferencia de " + cuentaOrigen + " a " + cuentaDestino,
                cuentaDestino, EstadoTransaccion.COMPLETADA, comision);
        origen.registrarTransaccion(transferencia);
        destino.registrarTransaccion(transferencia);
        registrarTransaccionGlobal(transferencia);
        cliente.acumularPuntos(transferencia);
        verificarTransaccionAtipica(cliente, transferencia);
        verificarAlertasMonedero(cliente, origen);

        return transferencia;
    }

    // ---------- Transacciones programadas ----------

    public void programarTransaccion(String idCliente, TransaccionProgramada transaccion) {
        Cliente cliente = obtenerCliente(idCliente);
        cliente.programarTransaccion(transaccion);
        agregarTransaccionProgramada(transaccion);
    }

    public List<Transaccion> ejecutarProgramadas(String idCliente) {

        Cliente cliente = obtenerCliente(idCliente);

        // Pendientes y ordenadas por fecha (usa el compareTo de TransaccionProgramada)
        List<TransaccionProgramada> pendientes = cliente.getListaTransaccionesProgramadas().stream()
                .filter(TransaccionProgramada::estaPendiente)
                .sorted()
                .toList();

        List<Transaccion> ejecutadas = new ArrayList<>();

        for (TransaccionProgramada programada : pendientes) {
            String cuentaOrigen = programada.getNumeroCuentaOrigen();
            double monto = programada.getMonto();

            try {
                Transaccion ejecutada = switch (programada.getTipoTransaccion()) {
                    case DEPOSITO -> depositar(idCliente, cuentaOrigen, monto);
                    case RETIRO -> retirar(idCliente, cuentaOrigen, monto);
                    case TRANSFERENCIA -> transferir(idCliente, cuentaOrigen, programada.getCuentaDestino(), monto);
                };
                ejecutadas.add(ejecutada);

            } catch (MonederoInactivoException | SaldoInsuficienteException
                     | LimiteRetiroExcedidoException | IllegalArgumentException e) {
                notificar(cliente, "No se pudo ejecutar la transacción programada " + programada.getId()
                        + ": " + e.getMessage(), "SMS");
            } finally {
                programada.reprogramar();
            }
        }

        return ejecutadas;
    }

    // ---------- Consultas ----------

    public double consultarSaldo(String idCliente, String numeroCuenta) {
        Cliente cliente = obtenerCliente(idCliente);
        return obtenerMonederoDelCliente(cliente, numeroCuenta).getSaldo();
    }

    public double consultarSaldoTotal(String idCliente) {
        return obtenerCliente(idCliente).getListaMonederos().values().stream()
                .mapToDouble(Monedero::getSaldo)
                .sum();
    }

    // Historial de todos los monederos del cliente, del más reciente al más antiguo
    public List<Transaccion> consultarHistorial(String idCliente) {
        return obtenerCliente(idCliente).getListaMonederos().values().stream()
                .flatMap(m -> m.getHistorialTransacciones().stream())
                .distinct()
                .sorted(Comparator.comparing(Transaccion::getFecha).reversed())
                .toList();
    }

    public List<Transaccion> consultarHistorial(String idCliente, String numeroCuenta) {
        Cliente cliente = obtenerCliente(idCliente);
        return obtenerMonederoDelCliente(cliente, numeroCuenta).getHistorialTransacciones().stream()
                .sorted(Comparator.comparing(Transaccion::getFecha).reversed())
                .toList();
    }

    // ---------- Análisis de patrones de gasto ----------

    // Gastos de un monedero: retiros y transferencias enviadas, sin las revertidas
    private List<Transaccion> gastosDe(Monedero monedero) {
        return monedero.getHistorialTransacciones().stream()
                .filter(t -> t.getEstado() == EstadoTransaccion.COMPLETADA)
                .filter(t -> t instanceof Retiro
                        || (t instanceof Transferencia tr && !tr.getCuentaDestino().equals(monedero.getNumeroCuenta())))
                .toList();
    }

    public List<Transaccion> obtenerGastos(String idCliente) {
        return obtenerCliente(idCliente).getListaMonederos().values().stream()
                .flatMap(m -> gastosDe(m).stream())
                .toList();
    }

    public double totalGastado(String idCliente) {
        return obtenerGastos(idCliente).stream()
                .mapToDouble(Transaccion::getMonto)
                .sum();
    }

    public double promedioGasto(String idCliente) {
        return obtenerGastos(idCliente).stream()
                .mapToDouble(Transaccion::getMonto)
                .average()
                .orElse(0);
    }

    public Map<String, Double> gastoPorTipo(String idCliente) {
        return obtenerGastos(idCliente).stream()
                .collect(Collectors.groupingBy(Transaccion::getTipo,
                        Collectors.summingDouble(Transaccion::getMonto)));
    }

    public Map<YearMonth, Double> gastoPorMes(String idCliente) {
        return obtenerGastos(idCliente).stream()
                .collect(Collectors.groupingBy(t -> YearMonth.from(t.getFecha()),
                        TreeMap::new,
                        Collectors.summingDouble(Transaccion::getMonto)));
    }

    public Map<String, Double> gastoPorMonedero(String idCliente) {
        return obtenerCliente(idCliente).getListaMonederos().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> gastosDe(e.getValue()).stream().mapToDouble(Transaccion::getMonto).sum()));
    }

    public List<Transaccion> topGastos(String idCliente, int cantidad) {
        return obtenerGastos(idCliente).stream()
                .sorted(Comparator.comparingDouble(Transaccion::getMonto).reversed())
                .limit(cantidad)
                .toList();
    }

    public String generarReporteGastos(String idCliente) {
        Cliente cliente = obtenerCliente(idCliente);
        StringBuilder sb = new StringBuilder();
        sb.append("REPORTE DE GASTOS - ").append(cliente.getNombre()).append("\n");
        sb.append(String.format("Total gastado: %.2f%n", totalGastado(idCliente)));
        sb.append(String.format("Gasto promedio: %.2f%n", promedioGasto(idCliente)));

        sb.append("\nGasto por tipo:\n");
        gastoPorTipo(idCliente).forEach((tipo, total) -> sb.append(String.format("  %s: %.2f%n", tipo, total)));

        sb.append("\nGasto por mes:\n");
        gastoPorMes(idCliente).forEach((mes, total) -> sb.append(String.format("  %s: %.2f%n", mes, total)));

        sb.append("\nGasto por monedero:\n");
        gastoPorMonedero(idCliente).forEach((nombreMonedero, total) ->
                sb.append(String.format("  %s: %.2f%n", nombreMonedero, total)));

        sb.append("\nTop 3 gastos más altos:\n");
        topGastos(idCliente, 3).forEach(t ->
                sb.append(String.format("  %s - %.2f (%s)%n", t.getTipo(), t.getMonto(), t.getFecha())));

        return sb.toString();
    }

    // ---------- Reversión de transacciones ----------

    public Transaccion revertirTransaccion(String idCliente, String idTransaccion)
            throws MonederoInactivoException, SaldoInsuficienteException, TransaccionNoReversibleException {

        Cliente cliente = obtenerCliente(idCliente);

        // Monederos del cliente que tienen esta transacción en su historial
        List<Monedero> monederosConTransaccion = cliente.getListaMonederos().values().stream()
                .filter(m -> m.getHistorialTransacciones().stream()
                        .anyMatch(t -> t.getId().equals(idTransaccion)))
                .toList();

        if (monederosConTransaccion.isEmpty()) {
            throw new IllegalArgumentException("La transacción " + idTransaccion + " no pertenece al cliente.");
        }

        Transaccion transaccion = monederosConTransaccion.get(0).getHistorialTransacciones().stream()
                .filter(t -> t.getId().equals(idTransaccion))
                .findFirst()
                .orElseThrow();

        if (transaccion.getEstado() == EstadoTransaccion.REVERTIDA) {
            throw new TransaccionNoReversibleException("La transacción " + idTransaccion + " ya fue revertida.");
        }

        if (transaccion instanceof Deposito) {
            // Se devuelve lo depositado: falla sin tocar nada si ya no hay saldo suficiente
            monederosConTransaccion.get(0).debitar(transaccion.getMonto());
        } else if (transaccion instanceof Retiro retiro) {
            // Se devuelve el monto retirado y también el cargo que se cobró
            monederosConTransaccion.get(0).acreditar(retiro.getMonto() + retiro.getCargoRetiro());
        } else if (transaccion instanceof Transferencia transferencia) {
            revertirTransferencia(monederosConTransaccion, transferencia);
        }

        transaccion.setEstado(EstadoTransaccion.REVERTIDA);
        cliente.revertirPuntos(transaccion);
        return transaccion;
    }

    private void revertirTransferencia(List<Monedero> monederosDelCliente, Transferencia transferencia)
            throws MonederoInactivoException, SaldoInsuficienteException, TransaccionNoReversibleException {

        // El origen es el monedero del cliente que NO es el destino: solo quien envió puede revertir
        Monedero origen = monederosDelCliente.stream()
                .filter(m -> !m.getNumeroCuenta().equals(transferencia.getCuentaDestino()))
                .findFirst()
                .orElseThrow(() -> new TransaccionNoReversibleException(
                        "Solo quien envió la transferencia puede revertirla."));

        Monedero destino = buscarMonedero(transferencia.getCuentaDestino())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe la cuenta destino " + transferencia.getCuentaDestino()));

        // Se revisa antes de mover plata para no dejar la reversión a medias
        if (!origen.isActivo()) {
            throw new MonederoInactivoException("El monedero " + origen.getNumeroCuenta() + " se encuentra inactivo.");
        }

        // debitar valida que el destino esté activo y tenga saldo ANTES de modificar nada
        destino.debitar(transferencia.getMonto());
        // Al emisor se le devuelve el monto y la comisión que pagó
        origen.acreditar(transferencia.getMonto() + transferencia.getComisionTransferencia());
    }

    // ---------- Getters ----------

    public String getNombre() {
        return nombre;
    }

    public Map<String, Cliente> getListaClientes() {
        return Collections.unmodifiableMap(listaClientes);
    }

    public List<Transaccion> getListaTransacciones() {
        return Collections.unmodifiableList(listaTransacciones);
    }

    public List<TransaccionProgramada> getListaTransaccionesProgramadas() {
        return Collections.unmodifiableList(listaTransaccionesProgramadas);
    }
}