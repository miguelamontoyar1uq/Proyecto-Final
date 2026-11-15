package co.edu.uniquindio.poo.proyectofinalprogramacion.model;

import java.util.*;

public class Cliente {
    private final String id;
    private String nombre;
    private String documento;
    private String telefono;
    private String correo;
    private Map<String, Monedero> listaMonederos;
    private List<Notificacion> notificaciones;
    private int puntosAcumulados;
    private RangoCliente rango;
    private List<CatalogoBeneficio> beneficiosActivos;
    private List<RegistroPuntos> historialPuntos;
    private List<TransaccionProgramada> listaTransaccionesProgramadas;

    public Cliente(String id, String nombre, String documento, String telefono, String correo) {
        validarTextoObligatorio(id, "El id");
        validarTextoObligatorio(nombre, "El nombre");
        validarTextoObligatorio(documento, "El documento");
        validarTelefono(telefono);
        validarCorreo(correo);

        this.id = id;
        this.nombre = nombre;
        this.documento = documento;
        this.telefono = telefono;
        this.correo = correo;
        this.listaMonederos = new HashMap<>();
        this.notificaciones = new ArrayList<>();
        this.beneficiosActivos = new ArrayList<>();
        this.historialPuntos = new ArrayList<>();
        this.puntosAcumulados = 0;
        this.rango = RangoCliente.BRONCE; // todos los clientes empiezan en Bronce (0-500 puntos)
        this.listaTransaccionesProgramadas = new ArrayList<>();
    }

    // ---------- Validaciones ----------

    private static void validarTextoObligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " no puede estar vacío.");
        }
    }

    private static void validarTelefono(String telefono) {
        if (telefono == null || !telefono.matches("\\d{7,15}")) {
            throw new IllegalArgumentException("El teléfono debe tener entre 7 y 15 dígitos.");
        }
    }

    private static void validarCorreo(String correo) {
        if (correo == null || !correo.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new IllegalArgumentException("El correo no tiene un formato válido.");
        }
    }

    // ---------- Monederos ----------

    public void agregarMonedero(String nombreMonedero, Monedero monedero) {
        validarTextoObligatorio(nombreMonedero, "El nombre del monedero");
        if (monedero == null) {
            throw new IllegalArgumentException("El monedero no puede ser nulo.");
        }
        if (listaMonederos.containsKey(nombreMonedero)) {
            throw new IllegalArgumentException("Ya existe un monedero con el nombre " + nombreMonedero);
        }
        boolean cuentaRepetida = listaMonederos.values().stream()
                .anyMatch(m -> m.getNumeroCuenta().equals(monedero.getNumeroCuenta()));
        if (cuentaRepetida) {
            throw new IllegalArgumentException("Ya existe un monedero con el número de cuenta "
                    + monedero.getNumeroCuenta());
        }
        this.listaMonederos.put(nombreMonedero, monedero);
    }

    private void actualizarRango() {
        if (puntosAcumulados <= 500) {
            this.rango = RangoCliente.BRONCE;
        } else if (puntosAcumulados <= 1000) {
            this.rango = RangoCliente.PLATA;
        } else if (puntosAcumulados <= 5000) {
            this.rango = RangoCliente.ORO;
        } else {
            this.rango = RangoCliente.PLATINO;
        }
    }

    // ---------- Getters y setters ----------

    public String getId() {
        return id;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        validarTelefono(telefono);
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        validarTextoObligatorio(nombre, "El nombre");
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        validarCorreo(correo);
        this.correo = correo;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        validarTextoObligatorio(documento, "El documento");
        this.documento = documento;
    }

    public Map<String, Monedero> getListaMonederos() {
        return Collections.unmodifiableMap(listaMonederos);
    }

    public List<TransaccionProgramada> getListaTransaccionesProgramadas() {
        return Collections.unmodifiableList(listaTransaccionesProgramadas);
    }

    public List<Notificacion> getNotificaciones() {
        return Collections.unmodifiableList(notificaciones);
    }

    public int getPuntosAcumulados() {
        return puntosAcumulados;
    }

    public RangoCliente getRango() {
        return rango;
    }

    public List<CatalogoBeneficio> getBeneficiosActivos() {
        return Collections.unmodifiableList(beneficiosActivos);
    }

    public List<RegistroPuntos> getHistorialPuntos() {
        return Collections.unmodifiableList(historialPuntos);
    }

    // ---------- Puntos ----------

    public void sumarPuntos(int cantidad, RegistroPuntos registro) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de puntos debe ser mayor a cero.");
        }
        this.puntosAcumulados += cantidad;
        this.historialPuntos.add(registro);
        actualizarRango();
    }

    public void restarPuntos(int cantidad, RegistroPuntos registro) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de puntos debe ser mayor a cero.");
        }
        if (cantidad > puntosAcumulados) {
            throw new IllegalArgumentException("El cliente no tiene puntos suficientes.");
        }
        this.puntosAcumulados -= cantidad;
        this.historialPuntos.add(registro);
        actualizarRango();
    }

    public void acumularPuntos(Transaccion transaccion) {
        int puntos = transaccion.getPuntosGenerados();
        if (puntos > 0) {
            sumarPuntos(puntos, new RegistroPuntos(puntos, "GANADOS", "Puntos por " + transaccion.getTipo()));
        }
    }

    public void revertirPuntos(Transaccion transaccion) {
        int puntos = Math.min(transaccion.getPuntosGenerados(), puntosAcumulados);
        if (puntos > 0) {
            restarPuntos(puntos, new RegistroPuntos(puntos, "REVERTIDOS",
                    "Reversión de " + transaccion.getTipo() + " " + transaccion.getId()));
        }
    }

    // ---------- Beneficios ----------

    public void activarBeneficio(CatalogoBeneficio beneficio) {
        if (beneficio == null) {
            throw new IllegalArgumentException("El beneficio no puede ser nulo.");
        }
        this.beneficiosActivos.add(beneficio);
    }

    public CatalogoBeneficio canjearBeneficio(TipoBeneficio tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de beneficio no puede ser nulo.");
        }
        int costo = tipo.getCostoPuntos();
        if (puntosAcumulados < costo) {
            throw new IllegalArgumentException("Necesitas " + costo + " puntos para canjear "
                    + tipo.getDescripcion() + " y tienes " + puntosAcumulados + ".");
        }
        restarPuntos(costo, new RegistroPuntos(costo, "CANJEADOS", "Canje de " + tipo.name()));

        CatalogoBeneficio beneficio = new CatalogoBeneficio(tipo);
        activarBeneficio(beneficio);
        return beneficio;
    }

    public boolean tieneBeneficioVigente(TipoBeneficio tipo) {
        return beneficiosActivos.stream()
                .anyMatch(b -> b.getTipo() == tipo && b.estaVigente());
    }

    // ---------- Notificaciones y programadas ----------

    public void recibirNotificacion(Notificacion notificacion) {
        if (notificacion == null) {
            throw new IllegalArgumentException("La notificación no puede ser nula.");
        }
        this.notificaciones.add(notificacion);
    }

    public void programarTransaccion(TransaccionProgramada transaccion) {
        if (transaccion == null) {
            throw new IllegalArgumentException("La transacción programada no puede ser nula.");
        }
        this.listaTransaccionesProgramadas.add(transaccion);
    }

    // ---------- Detección de transacciones atípicas ----------

    /**
     * Evalúa si una transacción en particular es atípica basándose en el
     * historial histórico del cliente, EXCLUYENDO la transacción actual del cálculo.
     */
    public boolean esTransaccionAtipica(Transaccion nuevaTransaccion) {

        // Une el historial de todos los monederos excluyendo la nueva transacción
        List<Transaccion> historialHistorico = listaMonederos.values().stream()
                .flatMap(monedero -> monedero.getHistorialTransacciones().stream())
                .distinct()
                .filter(t -> !t.getId().equals(nuevaTransaccion.getId()))
                .toList();

        if (historialHistorico.size() < 2) {
            return false;
        }

        double promedio = historialHistorico.stream()
                .mapToDouble(Transaccion::getMonto)
                .average()
                .orElse(0);

        double sumaCuadrados = historialHistorico.stream()
                .mapToDouble(t -> Math.pow(t.getMonto() - promedio, 2))
                .sum();

        double desviacion = Math.sqrt(sumaCuadrados / historialHistorico.size());
        double umbral = promedio + (2 * desviacion);

        return nuevaTransaccion.getMonto() > umbral;
    }
}