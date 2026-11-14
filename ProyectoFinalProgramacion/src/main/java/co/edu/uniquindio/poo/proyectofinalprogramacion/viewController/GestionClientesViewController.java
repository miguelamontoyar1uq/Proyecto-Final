package co.edu.uniquindio.poo.proyectofinalprogramacion.viewController;

import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.ClientesController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.MonederoVirtualController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Cliente;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class GestionClientesViewController {

    private MonederoVirtualController fachada;
    private ClientesController clientesController;
    private ObservableList<Cliente> listaClientes;

    @FXML private TextField txtDocumento, txtNombre, txtTelefono, txtCorreo, txtBuscar;
    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, String> colDocumento, colNombre, colTelefono, colCorreo, colRango, colPuntos;

    public GestionClientesViewController() {
        this.fachada = MonederoVirtualController.getInstancia();
        this.clientesController = fachada.getClientesController();
        this.listaClientes = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        configurarTabla();
        configurarEventos();
        actualizarTabla();
    }

    private void configurarTabla() {
        colDocumento.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDocumento()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colTelefono.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefono()));
        colCorreo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCorreo()));
        colRango.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRango().toString()));
        colPuntos.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getPuntosAcumulados())));
        tblClientes.setItems(listaClientes);
    }

    private void configurarEventos() {
        // Mostrar datos al seleccionar fila
        tblClientes.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                mostrarCliente(seleccionado);
            }
        });
        // Filtrar mientras se escribe
        txtBuscar.textProperty().addListener((obs, anterior, nuevo) -> actualizarTabla());
    }

    @FXML
    private void agregarCliente() {
        if (tblClientes.getSelectionModel().getSelectedItem() != null) {
            Alertas.mostrarAdvertencia("Hay un cliente seleccionado. Presione Limpiar para agregar uno nuevo.");
            return;
        }
        try {
            clientesController.agregarCliente(txtDocumento.getText(), txtNombre.getText(),
                    txtTelefono.getText(), txtCorreo.getText());
            actualizarTabla();
            limpiarFormulario();
            fachada.notificarCambios();
            Alertas.mostrarInformacion("Cliente agregado correctamente");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarError(e.getMessage());
        }
    }

    @FXML
    private void actualizarCliente() {
        Cliente seleccionado = tblClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alertas.mostrarAdvertencia("Seleccione un cliente de la tabla");
            return;
        }
        try {
            clientesController.actualizarCliente(seleccionado.getDocumento(), txtNombre.getText(),
                    txtTelefono.getText(), txtCorreo.getText());
            actualizarTabla();
            limpiarFormulario();
            fachada.notificarCambios();
            Alertas.mostrarInformacion("Cliente actualizado correctamente");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarError(e.getMessage());
        }
    }

    @FXML
    private void eliminarCliente() {
        Cliente seleccionado = tblClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alertas.mostrarAdvertencia("Seleccione un cliente de la tabla");
            return;
        }
        if (!Alertas.confirmar("¿Eliminar al cliente " + seleccionado.getNombre() + "?")) {
            return;
        }
        try {
            clientesController.eliminarCliente(seleccionado.getDocumento());
            actualizarTabla();
            limpiarFormulario();
            fachada.notificarCambios();
            Alertas.mostrarInformacion("Cliente eliminado correctamente");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarError(e.getMessage());
        }
    }

    @FXML
    private void limpiarFormulario() {
        tblClientes.getSelectionModel().clearSelection();
        txtDocumento.clear();
        txtNombre.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        txtDocumento.setDisable(false);
    }

    private void mostrarCliente(Cliente cliente) {
        txtDocumento.setText(cliente.getDocumento());
        txtNombre.setText(cliente.getNombre());
        txtTelefono.setText(cliente.getTelefono());
        txtCorreo.setText(cliente.getCorreo());
        // El documento no se modifica
        txtDocumento.setDisable(true);
    }

    private void actualizarTabla() {
        listaClientes.setAll(clientesController.buscarClientes(txtBuscar.getText()));
        tblClientes.refresh();
    }
}
