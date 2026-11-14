package co.edu.uniquindio.poo.proyectofinalprogramacion.viewController;

import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.MonederoVirtualController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.MonederosController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.TransaccionesController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Cliente;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Monedero;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Transaccion;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ConsultasHistorialViewController {

    private MonederoVirtualController fachada;
    private TransaccionesController transaccionesController;
    private MonederosController monederosController;
    private ObservableList<Transaccion> listaTransacciones;

    @FXML private ComboBox<Monedero> cmbMonedero;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private DatePicker dpDesde, dpHasta;
    @FXML private Label lblSaldo, lblMensaje;
    @FXML private TableView<Transaccion> tblHistorial;
    @FXML private TableColumn<Transaccion, String> colCodigo, colFecha, colTipo, colMonto,
            colEstado, colPuntos, colSospechosa;

    public ConsultasHistorialViewController() {
        this.fachada = MonederoVirtualController.getInstancia();
        this.transaccionesController = fachada.getTransaccionesController();
        this.monederosController = fachada.getMonederosController();
        this.listaTransacciones = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        Cliente cliente = fachada.getClienteActivo();
        cmbMonedero.setItems(FXCollections.observableArrayList(monederosController.obtenerMonederos(cliente)));
        cmbTipo.setItems(FXCollections.observableArrayList(transaccionesController.obtenerTiposOperacion()));
        configurarTabla();
        actualizarSaldo();
        actualizarTabla();
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(Formatos.fechaHora(c.getValue().getFecha())));
        colTipo.setCellValueFactory(c ->
                new SimpleStringProperty(transaccionesController.obtenerTipo(c.getValue())));
        colMonto.setCellValueFactory(c -> new SimpleStringProperty(Formatos.dinero(c.getValue().getMonto())));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().toString()));
        colPuntos.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getPuntosGenerados())));
        colSospechosa.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().esSospechosa() ? "Sí" : "No"));
        tblHistorial.setItems(listaTransacciones);
    }

    // Saldo del monedero o total
    @FXML
    private void actualizarSaldo() {
        Monedero seleccionado = cmbMonedero.getValue();
        double saldo = seleccionado != null ? seleccionado.getSaldo()
                : monederosController.obtenerMonederos(fachada.getClienteActivo()).stream()
                .mapToDouble(Monedero::getSaldo).sum();
        lblSaldo.setText(Formatos.dinero(saldo));
    }

    @FXML
    private void filtrar() {
        actualizarTabla();
    }

    @FXML
    private void revertirTransaccion() {
        Transaccion seleccionada = tblHistorial.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            Alertas.mostrarAdvertencia("Seleccione una transacción de la tabla");
            return;
        }
        if (!Alertas.confirmar("¿Revertir la transacción " + seleccionada.getCodigo() + "?")) {
            return;
        }
        try {
            transaccionesController.revertirTransaccion(fachada.getClienteActivo(), seleccionada);
            actualizarTabla();
            actualizarSaldo();
            fachada.notificarCambios();
            Alertas.mostrarInformacion("Transacción revertida y puntos recalculados");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarError(e.getMessage());
        }
    }

    @FXML
    private void limpiarFiltros() {
        cmbMonedero.setValue(null);
        cmbTipo.setValue(null);
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        actualizarSaldo();
        actualizarTabla();
    }

    private void actualizarTabla() {
        try {
            listaTransacciones.setAll(transaccionesController.obtenerTransacciones(fachada.getClienteActivo(),
                    cmbMonedero.getValue(), cmbTipo.getValue(), dpDesde.getValue(), dpHasta.getValue()));
            tblHistorial.refresh();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarError(e.getMessage());
        }
    }
}
