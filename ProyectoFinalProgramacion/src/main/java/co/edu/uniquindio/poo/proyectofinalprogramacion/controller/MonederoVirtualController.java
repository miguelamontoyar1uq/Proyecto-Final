package co.edu.uniquindio.poo.proyectofinalprogramacion.controller;

import co.edu.uniquindio.poo.proyectofinalprogramacion.exceptions.MonederoInactivoException;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.*;

public class MonederoVirtualController {

    private static MonederoVirtualController instancia;

    private MonederoVirtual monederoVirtual;
    private ClientesController clientesController;
    private MonederosController monederosController;
    private TransaccionesController transaccionesController;
    private ProgramadasController programadasController;
    private AnalisisController analisisController;
    private NotificacionesController notificacionesController;
    private PuntosController puntosController;
    private Cliente clienteActivo;
    private Runnable actualizador;

    private MonederoVirtualController() {
        this.monederoVirtual = new MonederoVirtual("E-Money");
        this.clientesController = new ClientesController(monederoVirtual);
        this.monederosController = new MonederosController(monederoVirtual);
        this.transaccionesController = new TransaccionesController(monederoVirtual);
        this.programadasController = new ProgramadasController(monederoVirtual);
        this.analisisController = new AnalisisController(monederoVirtual);
        this.notificacionesController = new NotificacionesController();
        this.puntosController = new PuntosController(monederoVirtual);
        inicializarDatosPrueba();
    }

    public static MonederoVirtualController getInstancia() {
        if (instancia == null) {
            instancia = new MonederoVirtualController();
        }
        return instancia;
    }

    // Datos quemados iniciales
    private void inicializarDatosPrueba() {
        Cliente ana = new Cliente("C001", "Ana Gómez", "1001", "3001234567", "ana@correo.com");
        Cliente luis = new Cliente("C002", "Luis Pérez", "1002", "3109876543", "luis@correo.com");
        monederoVirtual.registrarCliente(ana);
        monederoVirtual.registrarCliente(luis);

        ana.agregarMonedero("Ahorros", new MonederoAhorros("AH-001", 500000, 300000, 2000000));
        ana.agregarMonedero("Gastos", new MonederoGastos("GA-001", 300000, 150000, 50000));
        luis.agregarMonedero("Gastos", new MonederoGastos("GA-002", 25000, 100000, 30000));

        try {
            // El último depósito es atípico
            for (double monto : new double[]{20000, 25000, 22000, 18000, 24000, 150000}) {
                monederoVirtual.depositar("C001", "GA-001", monto);
            }
            // Deja a Luis con saldo bajo
            monederoVirtual.depositar("C002", "GA-002", 2000);
        } catch (MonederoInactivoException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public Cliente getClienteActivo() {
        return clienteActivo;
    }

    public void setClienteActivo(Cliente clienteActivo) {
        this.clienteActivo = clienteActivo;
    }

    public void setActualizador(Runnable actualizador) {
        this.actualizador = actualizador;
    }

    // Refresca la barra del cliente activo
    public void notificarCambios() {
        if (actualizador != null) {
            actualizador.run();
        }
    }

    public ClientesController getClientesController() {
        return clientesController;
    }

    public MonederosController getMonederosController() {
        return monederosController;
    }

    public TransaccionesController getTransaccionesController() {
        return transaccionesController;
    }

    public ProgramadasController getProgramadasController() {
        return programadasController;
    }

    public AnalisisController getAnalisisController() {
        return analisisController;
    }

    public NotificacionesController getNotificacionesController() {
        return notificacionesController;
    }

    public PuntosController getPuntosController() {
        return puntosController;
    }
}
