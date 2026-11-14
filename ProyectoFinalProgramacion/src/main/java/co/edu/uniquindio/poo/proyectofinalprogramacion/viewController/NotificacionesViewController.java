package co.edu.uniquindio.poo.proyectofinalprogramacion.viewController;

import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.MonederoVirtualController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.NotificacionesController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Notificacion;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.enums.Canal;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.enums.TipoNotificacion;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class NotificacionesViewController {

    private MonederoVirtualController fachada;
    private NotificacionesController notificacionesController;
    private ObservableList<Notificacion> listaNotificaciones;

    @FXML private ComboBox<TipoNotificacion> cmbTipo;
    @FXML private ComboBox<Canal> cmbCanal;
    @FXML private TableView<Notificacion> tblNotificaciones;
    @FXML private TableColumn<Notificacion, String> colFecha, colTipo, colCanal, colMensaje;

    public NotificacionesViewController() {
        this.fachada = MonederoVirtualController.getInstancia();
        this.notificacionesController = fachada.getNotificacionesController();
        this.listaNotificaciones = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        cmbTipo.setItems(FXCollections.observableArrayList(notificacionesController.obtenerTipos()));
        cmbCanal.setItems(FXCollections.observableArrayList(notificacionesController.obtenerCanales()));
        configurarTabla();
        actualizarTabla();
    }

    private void configurarTabla() {
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(Formatos.fechaHora(c.getValue().getFecha())));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo().toString()));
        colCanal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCanal().toString()));
        colMensaje.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMensaje()));
        tblNotificaciones.setItems(listaNotificaciones);
    }

    @FXML
    private void filtrar() {
        actualizarTabla();
    }

    @FXML
    private void limpiarFiltros() {
        cmbTipo.setValue(null);
        cmbCanal.setValue(null);
        actualizarTabla();
    }

    @FXML
    private void eliminarNotificacion() {
        Notificacion seleccionada = tblNotificaciones.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            Alertas.mostrarAdvertencia("Seleccione una notificación de la tabla");
            return;
        }
        if (!Alertas.confirmar("¿Eliminar la notificación seleccionada?")) {
            return;
        }
        try {
            notificacionesController.eliminarNotificacion(fachada.getClienteActivo(), seleccionada.getCodigo());
            actualizarTabla();
            Alertas.mostrarInformacion("Notificación eliminada correctamente");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarError(e.getMessage());
        }
    }

    private void actualizarTabla() {
        listaNotificaciones.setAll(notificacionesController.obtenerNotificaciones(
                fachada.getClienteActivo(), cmbTipo.getValue(), cmbCanal.getValue()));
        tblNotificaciones.refresh();
    }
}
