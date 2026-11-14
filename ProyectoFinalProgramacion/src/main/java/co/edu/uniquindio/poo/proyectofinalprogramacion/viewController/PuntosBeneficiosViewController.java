package co.edu.uniquindio.poo.proyectofinalprogramacion.viewController;

import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.MonederoVirtualController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.PuntosController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Beneficio;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.BeneficioCanjeado;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Cliente;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.RegistroPuntos;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class PuntosBeneficiosViewController {

    private MonederoVirtualController fachada;
    private PuntosController puntosController;
    private ObservableList<Beneficio> listaBeneficios;
    private ObservableList<RegistroPuntos> listaHistorial;
    private ObservableList<BeneficioCanjeado> listaCanjeados;

    @FXML private Label lblPuntos, lblRango, lblDescuento, lblProgreso;
    @FXML private ProgressBar pbRango;
    @FXML private TableView<Beneficio> tblBeneficios;
    @FXML private TableColumn<Beneficio, String> colNombre, colDescripcion, colPuntosRequeridos, colDuracion;
    @FXML private TableView<RegistroPuntos> tblHistorialPuntos;
    @FXML private TableColumn<RegistroPuntos, String> colFechaPuntos, colMovimiento, colPuntosMov, colTransaccion;
    @FXML private TableView<BeneficioCanjeado> tblCanjeados;
    @FXML private TableColumn<BeneficioCanjeado, String> colNombreCanjeado, colFechaCanje, colVigencia;

    public PuntosBeneficiosViewController() {
        this.fachada = MonederoVirtualController.getInstancia();
        this.puntosController = fachada.getPuntosController();
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
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colDescripcion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));
        colPuntosRequeridos.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getPuntosRequeridos())));
        colDuracion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDuracion() == 0
                ? "Inmediato" : c.getValue().getDuracion() + " días"));
        tblBeneficios.setItems(listaBeneficios);

        // Historial de puntos
        colFechaPuntos.setCellValueFactory(c ->
                new SimpleStringProperty(Formatos.fechaHora(c.getValue().getFecha())));
        colMovimiento.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTipoMovimiento().toString()));
        colPuntosMov.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getPuntos())));
        colTransaccion.setCellValueFactory(c ->
                new SimpleStringProperty(puntosController.obtenerTransaccionAsociada(c.getValue())));
        tblHistorialPuntos.setItems(listaHistorial);

        // Beneficios canjeados
        colNombreCanjeado.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getBeneficio().getNombre()));
        colFechaCanje.setCellValueFactory(c ->
                new SimpleStringProperty(Formatos.fechaHora(c.getValue().getFechaCanje())));
        colVigencia.setCellValueFactory(c ->
                new SimpleStringProperty(puntosController.obtenerVigencia(c.getValue())));
        tblCanjeados.setItems(listaCanjeados);
    }

    @FXML
    private void canjearBeneficio() {
        Beneficio seleccionado = tblBeneficios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alertas.mostrarAdvertencia("Seleccione un beneficio del catálogo");
            return;
        }
        if (!Alertas.confirmar("¿Canjear " + seleccionado.getNombre() + " por "
                + seleccionado.getPuntosRequeridos() + " puntos?")) {
            return;
        }
        try {
            puntosController.canjearBeneficio(fachada.getClienteActivo(), seleccionado);
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
        lblDescuento.setText(String.format("%.0f%%", cliente.getRango().getDescuentoTarifa()));
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
