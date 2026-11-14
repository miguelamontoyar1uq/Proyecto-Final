package co.edu.uniquindio.poo.proyectofinalprogramacion.viewController;

import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.MonederoVirtualController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.MonederosController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Cliente;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Monedero;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class GestionMonederosViewController {

    private MonederoVirtualController fachada;
    private MonederosController monederosController;
    private ObservableList<Monedero> listaMonederos;

    @FXML private TextField txtNombre, txtSaldoInicial, txtMetaAhorro, txtLimiteDiario, txtUmbralSaldoBajo;
    @FXML private Label lblMeta, lblLimite, lblUmbral;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TableView<Monedero> tblMonederos;
    @FXML private TableColumn<Monedero, String> colCodigo, colNombre, colTipo, colSaldo;

    public GestionMonederosViewController() {
        this.fachada = MonederoVirtualController.getInstancia();
        this.monederosController = fachada.getMonederosController();
        this.listaMonederos = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        cmbTipo.setItems(FXCollections.observableArrayList(monederosController.obtenerTipos()));
        configurarTabla();
        configurarEventos();
        actualizarTabla();
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colTipo.setCellValueFactory(c ->
                new SimpleStringProperty(monederosController.obtenerTipo(c.getValue())));
        colSaldo.setCellValueFactory(c -> new SimpleStringProperty(Formatos.dinero(c.getValue().getSaldo())));
        tblMonederos.setItems(listaMonederos);
    }

    private void configurarEventos() {
        tblMonederos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                mostrarMonedero(seleccionado);
            }
        });
    }

    @FXML
    private void cambiarTipo() {
        mostrarCamposSegunTipo(cmbTipo.getValue());
    }

    @FXML
    private void crearMonedero() {
        if (tblMonederos.getSelectionModel().getSelectedItem() != null) {
            Alertas.mostrarAdvertencia("Hay un monedero seleccionado. Presione Limpiar para crear uno nuevo.");
            return;
        }
        try {
            double saldo = Formatos.decimal(txtSaldoInicial.getText(), "saldo inicial");
            monederosController.agregarMonedero(fachada.getClienteActivo(), cmbTipo.getValue(),
                    txtNombre.getText(), saldo, leerCampoTipo(txtMetaAhorro, "meta de ahorro"),
                    leerCampoTipo(txtLimiteDiario, "límite diario"),
                    leerCampoTipo(txtUmbralSaldoBajo, "umbral de saldo bajo"));
            actualizarTabla();
            limpiarFormulario();
            Alertas.mostrarInformacion("Monedero creado correctamente");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarError(e.getMessage());
        }
    }

    @FXML
    private void actualizarMonedero() {
        Monedero seleccionado = tblMonederos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alertas.mostrarAdvertencia("Seleccione un monedero de la tabla");
            return;
        }
        try {
            monederosController.actualizarMonedero(fachada.getClienteActivo(), seleccionado.getCodigo(),
                    txtNombre.getText(), leerCampoTipo(txtMetaAhorro, "meta de ahorro"),
                    leerCampoTipo(txtLimiteDiario, "límite diario"),
                    leerCampoTipo(txtUmbralSaldoBajo, "umbral de saldo bajo"));
            actualizarTabla();
            limpiarFormulario();
            Alertas.mostrarInformacion("Monedero actualizado correctamente");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarError(e.getMessage());
        }
    }

    @FXML
    private void eliminarMonedero() {
        Monedero seleccionado = tblMonederos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alertas.mostrarAdvertencia("Seleccione un monedero de la tabla");
            return;
        }
        if (!Alertas.confirmar("¿Eliminar el monedero " + seleccionado.getNombre() + "?")) {
            return;
        }
        try {
            monederosController.eliminarMonedero(fachada.getClienteActivo(), seleccionado.getCodigo());
            actualizarTabla();
            limpiarFormulario();
            Alertas.mostrarInformacion("Monedero eliminado correctamente");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarError(e.getMessage());
        }
    }

    @FXML
    private void limpiarFormulario() {
        tblMonederos.getSelectionModel().clearSelection();
        txtNombre.clear();
        txtSaldoInicial.clear();
        txtMetaAhorro.clear();
        txtLimiteDiario.clear();
        txtUmbralSaldoBajo.clear();
        cmbTipo.setValue(null);
        cmbTipo.setDisable(false);
        txtSaldoInicial.setDisable(false);
        mostrarCamposSegunTipo(null);
    }

    private void mostrarMonedero(Monedero monedero) {
        cmbTipo.setValue(monederosController.obtenerTipo(monedero));
        txtNombre.setText(monedero.getNombre());
        txtSaldoInicial.setText(String.valueOf(monedero.getSaldo()));
        txtMetaAhorro.setText(String.valueOf(monederosController.obtenerMeta(monedero)));
        txtLimiteDiario.setText(String.valueOf(monederosController.obtenerLimite(monedero)));
        txtUmbralSaldoBajo.setText(String.valueOf(monederosController.obtenerUmbral(monedero)));
        mostrarCamposSegunTipo(cmbTipo.getValue());

        // Tipo y saldo no se modifican
        cmbTipo.setDisable(true);
        txtSaldoInicial.setDisable(true);
    }

    private void mostrarCamposSegunTipo(String tipo) {
        boolean ahorros = MonederosController.AHORROS.equals(tipo);
        boolean gastos = MonederosController.GASTOS_DIARIOS.equals(tipo);
        cambiarVisibilidad(ahorros, lblMeta, txtMetaAhorro);
        cambiarVisibilidad(gastos, lblLimite, txtLimiteDiario, lblUmbral, txtUmbralSaldoBajo);
    }

    private void cambiarVisibilidad(boolean visible, Node... nodos) {
        for (Node nodo : nodos) {
            nodo.setVisible(visible);
            nodo.setManaged(visible);
        }
    }

    // Leer solo si el campo aplica
    private double leerCampoTipo(TextField campo, String nombre) {
        return campo.isVisible() ? Formatos.decimal(campo.getText(), nombre) : 0;
    }

    private void actualizarTabla() {
        Cliente cliente = fachada.getClienteActivo();
        listaMonederos.setAll(monederosController.obtenerMonederos(cliente));
        tblMonederos.refresh();
    }
}
