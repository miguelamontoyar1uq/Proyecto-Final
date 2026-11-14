package co.edu.uniquindio.poo.proyectofinalprogramacion.viewController;

import co.edu.uniquindio.poo.proyectofinalprogramacion.App;
import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.ClientesController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.MonederoVirtualController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Cliente;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class PrincipalViewController {

    private MonederoVirtualController fachada;
    private ClientesController clientesController;
    private List<Button> botonesMenu;
    private String vistaActual;
    private Button botonActual;
    private boolean actualizando;

    @FXML private BorderPane areaContenido;
    @FXML private VBox panelBienvenida;
    @FXML private ComboBox<Cliente> cmbClienteActivo;
    @FXML private Label lblRangoActivo, lblPuntosActivo;
    @FXML private Button btnClientes, btnMonederos, btnTransacciones, btnHistorial,
            btnProgramadas, btnAnalisis, btnNotificaciones, btnPuntos;

    public PrincipalViewController() {
        this.fachada = MonederoVirtualController.getInstancia();
        this.clientesController = fachada.getClientesController();
    }

    @FXML
    private void initialize() {
        botonesMenu = List.of(btnClientes, btnMonederos, btnTransacciones, btnHistorial,
                btnProgramadas, btnAnalisis, btnNotificaciones, btnPuntos);
        cmbClienteActivo.setConverter(Formatos.texto(Cliente::getNombre));
        fachada.setActualizador(this::actualizarBarraCliente);
        actualizarBarraCliente();
    }

    @FXML
    private void mostrarBienvenida() {
        areaContenido.setCenter(panelBienvenida);
        vistaActual = null;
        botonActual = null;
        marcarOpcion(null);
    }

    @FXML
    private void abrirClientes() {
        cargarVista("GestionClientes.fxml", btnClientes);
    }

    @FXML
    private void abrirMonederos() {
        cargarVista("GestionMonederos.fxml", btnMonederos);
    }

    @FXML
    private void abrirTransacciones() {
        cargarVista("Transacciones.fxml", btnTransacciones);
    }

    @FXML
    private void abrirHistorial() {
        cargarVista("ConsultasHistorial.fxml", btnHistorial);
    }

    @FXML
    private void abrirProgramadas() {
        cargarVista("TransaccionesProgramadas.fxml", btnProgramadas);
    }

    @FXML
    private void abrirAnalisis() {
        cargarVista("AnalisisGastos.fxml", btnAnalisis);
    }

    @FXML
    private void abrirNotificaciones() {
        cargarVista("Notificaciones.fxml", btnNotificaciones);
    }

    @FXML
    private void abrirPuntosBeneficios() {
        cargarVista("PuntosBeneficios.fxml", btnPuntos);
    }

    @FXML
    private void cambiarClienteActivo() {
        if (actualizando) {
            return;
        }
        Cliente seleccionado = cmbClienteActivo.getValue();
        if (seleccionado == null) {
            return;
        }
        fachada.setClienteActivo(seleccionado);
        actualizarEtiquetas();
        // Recargar la vista abierta
        if (vistaActual != null) {
            cargarVista(vistaActual, botonActual);
        }
    }

    private void cargarVista(String archivo, Button boton) {
        if (boton != btnClientes && fachada.getClienteActivo() == null) {
            Alertas.mostrarAdvertencia("Registre un cliente en Gestión Clientes primero");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/" + archivo));
            areaContenido.setCenter(loader.load());
            vistaActual = archivo;
            botonActual = boton;
            marcarOpcion(boton);
        } catch (IOException e) {
            Alertas.mostrarError("No se pudo abrir la vista: " + e.getMessage());
        }
    }

    private void marcarOpcion(Button boton) {
        botonesMenu.forEach(b -> b.getStyleClass().remove("activo"));
        if (boton != null) {
            boton.getStyleClass().add("activo");
        }
    }

    // Se invoca cuando cambian los datos
    private void actualizarBarraCliente() {
        actualizando = true;
        List<Cliente> clientes = clientesController.obtenerClientes();
        Cliente activo = fachada.getClienteActivo();
        if (activo == null || !clientes.contains(activo)) {
            activo = clientes.isEmpty() ? null : clientes.get(0);
            fachada.setClienteActivo(activo);
        }
        cmbClienteActivo.setItems(FXCollections.observableArrayList(clientes));
        cmbClienteActivo.setValue(activo);
        actualizarEtiquetas();
        actualizando = false;
    }

    private void actualizarEtiquetas() {
        Cliente activo = fachada.getClienteActivo();
        lblRangoActivo.setText("Rango: " + (activo == null ? "—" : activo.getRango().toString()));
        lblPuntosActivo.setText("Puntos: " + (activo == null ? 0 : activo.getPuntosAcumulados()));
    }
}
