package co.edu.uniquindio.poo.proyectofinalprogramacion.viewController;

import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.MonederoVirtualController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.MonederosController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.PuntosController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.CatalogoBeneficio;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Cliente;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Monedero;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.RegistroPuntos;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.TipoBeneficio;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.Optional;

public class PuntosBeneficiosViewController {

    private MonederoVirtualController fachada;
    private PuntosController puntosController;
    private MonederosController monederosController;
    private ObservableList<TipoBeneficio> listaBeneficios;
    private ObservableList<RegistroPuntos> listaHistorial;
    private ObservableList<CatalogoBeneficio> listaCanjeados;

    @FXML private Label lblPuntos, lblRango, lblDescuento, lblProgreso;
    @FXML private ProgressBar pbRango;
    @FXML private TableView<TipoBeneficio> tblBeneficios;
    @FXML private TableColumn<TipoBeneficio, String> colNombre, colDescripcion, colPuntosRequeridos, colDuracion;
    @FXML private TableView<RegistroPuntos> tblHistorialPuntos;
    @FXML private TableColumn<RegistroPuntos, String> colFechaPuntos, colMovimiento, colPuntosMov, colTransaccion;
    @FXML private TableView<CatalogoBeneficio> tblCanjeados;
    @FXML private TableColumn<CatalogoBeneficio, String> colNombreCanjeado, colFechaCanje, colVigencia;

    public PuntosBeneficiosViewController() {
        this.fachada = MonederoVirtualController.getInstancia();
        this.puntosController = fachada.getPuntosController();
        this.monederosController = fachada.getMonederosController();
        this.listaBeneficios = FXCollections.observableArrayList();
        this.listaHistorial = FXCollections.observableArrayList();
        this.listaCanjeados = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        configurarTablas();
        actualizarResumen();
        actualizarTablas();
    }

    private void configurarTablas() {
        // Catálogo de beneficios
        colNombre.setCellValueFactory(c ->
                new SimpleStringProperty(puntosController.obtenerNombre(c.getValue())));
        colDescripcion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));
        colPuntosRequeridos.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getCostoPuntos())));
        colDuracion.setCellValueFactory(c ->
                new SimpleStringProperty(puntosController.obtenerDuracion(c.getValue())));
        tblBeneficios.setItems(listaBeneficios);

        // Historial de puntos
        colFechaPuntos.setCellValueFactory(c -> new SimpleStringProperty(Formatos.fechaHora(c.getValue().fecha())));
        colMovimiento.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().tipo()));
        colPuntosMov.setCellValueFactory(c ->
                new SimpleStringProperty(puntosController.obtenerPuntosConSigno(c.getValue())));
        colTransaccion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().motivo()));
        tblHistorialPuntos.setItems(listaHistorial);

        // Beneficios canjeados
        colNombreCanjeado.setCellValueFactory(c ->
                new SimpleStringProperty(puntosController.obtenerNombre(c.getValue().getTipo())));
        colFechaCanje.setCellValueFactory(c ->
                new SimpleStringProperty(Formatos.fechaHora(c.getValue().getFechaActivacion())));
        colVigencia.setCellValueFactory(c ->
                new SimpleStringProperty(puntosController.obtenerVigencia(c.getValue())));
        tblCanjeados.setItems(listaCanjeados);
    }

    @FXML
    private void canjearBeneficio() {
        TipoBeneficio seleccionado = tblBeneficios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alertas.mostrarAdvertencia("Seleccione un beneficio del catálogo");
            return;
        }
        if (!Alertas.confirmar("¿Canjear " + puntosController.obtenerNombre(seleccionado) + " por "
                + seleccionado.getCostoPuntos() + " puntos?")) {
            return;
        }
        Cliente cliente = fachada.getClienteActivo();
        Monedero monederoBono = null;
        // El bono necesita un monedero destino
        if (seleccionado == TipoBeneficio.BONO_SALDO) {
            List<Monedero> monederos = monederosController.obtenerMonederos(cliente);
            Optional<Monedero> elegido = Alertas.elegir("¿En qué monedero se acredita el bono?",
                    monederos, monederosController::describir);
            if (elegido.isEmpty()) {
                Alertas.mostrarAdvertencia("Se necesita un monedero para recibir el bono");
                return;
            }
            monederoBono = elegido.get();
        }
        try {
            puntosController.canjearBeneficio(cliente, seleccionado, monederoBono);
            actualizarResumen();
            actualizarTablas();
            fachada.notificarCambios();
            Alertas.mostrarInformacion("Beneficio canjeado correctamente");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarError(e.getMessage());
        }
    }

    @FXML
    private void limpiarSeleccion() {
        tblBeneficios.getSelectionModel().clearSelection();
        tblHistorialPuntos.getSelectionModel().clearSelection();
        tblCanjeados.getSelectionModel().clearSelection();
    }

    private void actualizarResumen() {
        Cliente cliente = fachada.getClienteActivo();
        lblPuntos.setText(String.valueOf(cliente.getPuntosAcumulados()));
        lblRango.setText(cliente.getRango().toString());
        lblDescuento.setText(puntosController.obtenerTarifaTransferencia(cliente));
        lblProgreso.setText(puntosController.obtenerTextoProgreso(cliente));
        pbRango.setProgress(puntosController.obtenerProgresoRango(cliente));
    }

    private void actualizarTablas() {
        Cliente cliente = fachada.getClienteActivo();
        listaBeneficios.setAll(puntosController.obtenerBeneficios());
        listaHistorial.setAll(puntosController.obtenerHistorialPuntos(cliente));
        listaCanjeados.setAll(puntosController.obtenerBeneficiosCanjeados(cliente));
        tblBeneficios.refresh();
        tblHistorialPuntos.refresh();
        tblCanjeados.refresh();
    }
}
