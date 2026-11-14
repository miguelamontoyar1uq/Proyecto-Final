package co.edu.uniquindio.poo.proyectofinalprogramacion.viewController;

import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.MonederoVirtualController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.MonederosController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.ProgramadasController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Cliente;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Monedero;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.TransaccionProgramada;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.enums.Frecuencia;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class TransaccionesProgramadasViewController {

    private MonederoVirtualController fachada;
    private ProgramadasController programadasController;
    private MonederosController monederosController;
    private ObservableList<TransaccionProgramada> listaProgramadas;

    @FXML private ComboBox<String> cmbTipo;
    @FXML private ComboBox<Frecuencia> cmbFrecuencia;
    @FXML private ComboBox<Monedero> cmbMonederoOrigen, cmbMonederoDestino;
    @FXML private TextField txtMonto;
    @FXML private DatePicker dpProximaEjecucion;
    @FXML private Label lblMonederoDestino;
    @FXML private TableView<TransaccionProgramada> tblProgramadas;
    @FXML private TableColumn<TransaccionProgramada, String> colCodigo, colTipo, colMonto,
            colFrecuencia, colProxima, colEstado;

    public TransaccionesProgramadasViewController() {
        this.fachada = MonederoVirtualController.getInstancia();
        this.programadasController = fachada.getProgramadasController();
        this.monederosController = fachada.getMonederosController();
        this.listaProgramadas = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        Cliente cliente = fachada.getClienteActivo();
        cmbTipo.setItems(FXCollections.observableArrayList(programadasController.obtenerTipos()));
        cmbFrecuencia.setItems(FXCollections.observableArrayList(programadasController.obtenerFrecuencias()));
        cmbMonederoOrigen.setItems(FXCollections.observableArrayList(monederosController.obtenerMonederos(cliente)));
        cmbMonederoDestino.setItems(FXCollections.observableArrayList(monederosController.obtenerTodosLosMonederos()));
        cmbMonederoDestino.setConverter(Formatos.texto(monederosController::describir));
        configurarTabla();
        configurarEventos();
        actualizarTabla();
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colTipo.setCellValueFactory(c ->
                new SimpleStringProperty(programadasController.obtenerTipo(c.getValue())));
        colMonto.setCellValueFactory(c -> new SimpleStringProperty(Formatos.dinero(c.getValue().getMonto())));
        colFrecuencia.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getFrecuencia().toString()));
        colProxima.setCellValueFactory(c ->
                new SimpleStringProperty(Formatos.fecha(c.getValue().getFechaProximaEjecucion())));
        colEstado.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().isActiva() ? "Activa" : "Pausada"));
        tblProgramadas.setItems(listaProgramadas);
    }

    private void configurarEventos() {
        tblProgramadas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionada) -> {
            if (seleccionada != null) {
                mostrarProgramada(seleccionada);
            }
        });
    }

    @FXML
    private void cambiarTipo() {
        mostrarDestino(ProgramadasController.TRANSFERENCIA.equals(cmbTipo.getValue()));
    }

    @FXML
    private void programarTransaccion() {
        if (tblProgramadas.getSelectionModel().getSelectedItem() != null) {
            Alertas.mostrarAdvertencia("Hay una programación seleccionada. Presione Limpiar para crear una nueva.");
            return;
        }
        try {
            programadasController.programar(fachada.getClienteActivo(), cmbTipo.getValue(),
                    Formatos.decimal(txtMonto.getText(), "monto"), cmbFrecuencia.getValue(),
                    dpProximaEjecucion.getValue(), cmbMonederoOrigen.getValue(), cmbMonederoDestino.getValue());
            actualizarTabla();
            limpiarFormulario();
            Alertas.mostrarInformacion("Transacción programada correctamente");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarError(e.getMessage());
        }
    }

    @FXML
    private void actualizarProgramacion() {
        TransaccionProgramada seleccionada = tblProgramadas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            Alertas.mostrarAdvertencia("Seleccione una programación de la tabla");
            return;
        }
        try {
            programadasController.actualizarProgramacion(seleccionada.getCodigo(),
                    Formatos.decimal(txtMonto.getText(), "monto"), cmbFrecuencia.getValue(),
                    dpProximaEjecucion.getValue(), cmbMonederoOrigen.getValue(), cmbMonederoDestino.getValue());
            actualizarTabla();
            limpiarFormulario();
            Alertas.mostrarInformacion("Programación actualizada correctamente");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarError(e.getMessage());
        }
    }

    @FXML
    private void alternarEstado() {
        TransaccionProgramada seleccionada = tblProgramadas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            Alertas.mostrarAdvertencia("Seleccione una programación de la tabla");
            return;
        }
        try {
            programadasController.alternarEstado(seleccionada.getCodigo());
            actualizarTabla();
            limpiarFormulario();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarError(e.getMessage());
        }
    }

    @FXML
    private void procesarPendientes() {
        try {
            int procesadas = programadasController.procesarPendientes(fachada.getClienteActivo());
            actualizarTabla();
            fachada.notificarCambios();
            Alertas.mostrarInformacion("Transacciones procesadas: " + procesadas);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarError(e.getMessage());
        }
    }

    @FXML
    private void eliminarProgramacion() {
        TransaccionProgramada seleccionada = tblProgramadas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            Alertas.mostrarAdvertencia("Seleccione una programación de la tabla");
            return;
        }
        if (!Alertas.confirmar("¿Eliminar la programación " + seleccionada.getCodigo() + "?")) {
            return;
        }
        try {
            programadasController.eliminarProgramacion(seleccionada.getCodigo());
            actualizarTabla();
            limpiarFormulario();
            Alertas.mostrarInformacion("Programación eliminada correctamente");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarError(e.getMessage());
        }
    }

    @FXML
    private void limpiarFormulario() {
        tblProgramadas.getSelectionModel().clearSelection();
        cmbTipo.setValue(null);
        txtMonto.clear();
        cmbFrecuencia.setValue(null);
        dpProximaEjecucion.setValue(null);
        cmbMonederoOrigen.setValue(null);
        cmbMonederoDestino.setValue(null);
        cmbTipo.setDisable(false);
        mostrarDestino(false);
    }

    private void mostrarProgramada(TransaccionProgramada programada) {
        cmbTipo.setValue(programadasController.obtenerTipo(programada));
        txtMonto.setText(String.valueOf(programada.getMonto()));
        cmbFrecuencia.setValue(programada.getFrecuencia());
        dpProximaEjecucion.setValue(programada.getFechaProximaEjecucion());
        cmbMonederoOrigen.setValue(programada.getMonederoOrigen());
        cmbMonederoDestino.setValue(programada.getMonederoDestino());
        mostrarDestino(programada.getMonederoDestino() != null);

        // El tipo no se modifica
        cmbTipo.setDisable(true);
    }

    private void mostrarDestino(boolean visible) {
        for (Node nodo : new Node[]{lblMonederoDestino, cmbMonederoDestino}) {
            nodo.setVisible(visible);
            nodo.setManaged(visible);
        }
        if (!visible) {
            cmbMonederoDestino.setValue(null);
        }
    }

    private void actualizarTabla() {
        listaProgramadas.setAll(programadasController.obtenerProgramadas(fachada.getClienteActivo()));
        tblProgramadas.refresh();
    }
}
