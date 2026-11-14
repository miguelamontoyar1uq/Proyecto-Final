package co.edu.uniquindio.poo.proyectofinalprogramacion.viewController;

import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.ClientesController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.MonederoVirtualController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.MonederosController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.TransaccionesController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Cliente;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Monedero;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.List;

public class TransaccionesViewController {

    private MonederoVirtualController fachada;
    private TransaccionesController transaccionesController;
    private MonederosController monederosController;
    private ClientesController clientesController;

    @FXML private ComboBox<String> cmbTipoOperacion;
    @FXML private ComboBox<Monedero> cmbMonederoOrigen, cmbMonederoDestino;
    @FXML private TextField txtMonto, txtDocumentoDestino;
    @FXML private Label lblDocumentoDestino, lblMonederoDestino;
    @FXML private Label lblSaldo, lblCargo, lblPuntosGanar, lblMensaje;

    public TransaccionesViewController() {
        this.fachada = MonederoVirtualController.getInstancia();
        this.transaccionesController = fachada.getTransaccionesController();
        this.monederosController = fachada.getMonederosController();
        this.clientesController = fachada.getClientesController();
    }

    @FXML
    private void initialize() {
        Cliente cliente = fachada.getClienteActivo();
        cmbTipoOperacion.setItems(FXCollections.observableArrayList(
                transaccionesController.obtenerTiposOperacion()));
        cmbMonederoOrigen.setItems(FXCollections.observableArrayList(
                monederosController.obtenerMonederos(cliente)));
        actualizarResumen();
    }

    @FXML
    private void cambiarTipoOperacion() {
        boolean transferencia = TransaccionesController.TRANSFERENCIA.equals(cmbTipoOperacion.getValue());
        cambiarVisibilidad(transferencia, lblDocumentoDestino, txtDocumentoDestino,
                lblMonederoDestino, cmbMonederoDestino);
        if (!transferencia) {
            limpiarDestino();
        }
        actualizarResumen();
    }

    @FXML
    private void buscarDestino() {
        try {
            Cliente destino = clientesController.buscarCliente(txtDocumentoDestino.getText());
            List<Monedero> monederos = monederosController.obtenerMonederos(destino);
            cmbMonederoDestino.setItems(FXCollections.observableArrayList(monederos));
            cmbMonederoDestino.setValue(null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarError(e.getMessage());
        }
    }

    @FXML
    private void actualizarResumen() {
        Monedero origen = cmbMonederoOrigen.getValue();
        lblSaldo.setText(Formatos.dinero(origen == null ? 0 : origen.getSaldo()));
        lblCargo.setText(Formatos.dinero(0));
        lblPuntosGanar.setText("0");
        lblMensaje.setText("");
        if (cmbTipoOperacion.getValue() == null || txtMonto.getText().isBlank()) {
            return;
        }
        try {
            Cliente cliente = fachada.getClienteActivo();
            double monto = Formatos.decimal(txtMonto.getText(), "monto");
            String tipo = cmbTipoOperacion.getValue();
            lblCargo.setText(Formatos.dinero(transaccionesController.calcularCargo(cliente, tipo, monto)));
            lblPuntosGanar.setText(String.valueOf(transaccionesController.calcularPuntos(cliente, tipo, monto)));
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    private void ejecutarTransaccion() {
        try {
            double monto = Formatos.decimal(txtMonto.getText(), "monto");
            transaccionesController.ejecutarOperacion(fachada.getClienteActivo(), cmbTipoOperacion.getValue(),
                    cmbMonederoOrigen.getValue(), cmbMonederoDestino.getValue(), monto);
            fachada.notificarCambios();
            // Conservar tipo y monedero
            txtMonto.clear();
            actualizarResumen();
            Alertas.mostrarInformacion("Operación realizada correctamente");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarError(e.getMessage());
        }
    }

    @FXML
    private void limpiarFormulario() {
        cmbTipoOperacion.setValue(null);
        cmbMonederoOrigen.setValue(null);
        txtMonto.clear();
        limpiarDestino();
        actualizarResumen();
    }

    private void limpiarDestino() {
        txtDocumentoDestino.clear();
        cmbMonederoDestino.setValue(null);
        cmbMonederoDestino.getItems().clear();
    }

    private void cambiarVisibilidad(boolean visible, Node... nodos) {
        for (Node nodo : nodos) {
            nodo.setVisible(visible);
            nodo.setManaged(visible);
        }
    }
}
