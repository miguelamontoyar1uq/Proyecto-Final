package co.edu.uniquindio.poo.proyectofinalprogramacion.viewController;

import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.AnalisisController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.MonederoVirtualController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.TransaccionesController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.AnalisisGasto;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Cliente;
import co.edu.uniquindio.poo.proyectofinalprogramacion.model.Transaccion;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Map;

public class AnalisisGastosViewController {

    private MonederoVirtualController fachada;
    private AnalisisController analisisController;
    private TransaccionesController transaccionesController;
    private ObservableList<Transaccion> listaSospechosas;

    @FXML private Label lblPromedio, lblDesviacion, lblUmbral;
    @FXML private BarChart<String, Number> chartGastos;
    @FXML private TableView<Transaccion> tblSospechosas;
    @FXML private TableColumn<Transaccion, String> colCodigo, colFecha, colTipo, colMonto;

    public AnalisisGastosViewController() {
        this.fachada = MonederoVirtualController.getInstancia();
        this.analisisController = fachada.getAnalisisController();
        this.transaccionesController = fachada.getTransaccionesController();
        this.listaSospechosas = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        configurarTabla();
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(Formatos.fechaHora(c.getValue().getFecha())));
        colTipo.setCellValueFactory(c ->
                new SimpleStringProperty(transaccionesController.obtenerTipo(c.getValue())));
        colMonto.setCellValueFactory(c -> new SimpleStringProperty(Formatos.dinero(c.getValue().getMonto())));
        tblSospechosas.setItems(listaSospechosas);
    }

    @FXML
    private void ejecutarAnalisis() {
        try {
            Cliente cliente = fachada.getClienteActivo();
            AnalisisGasto analisis = analisisController.analizar(cliente);
            lblPromedio.setText(Formatos.dinero(analisis.getPromedio()));
            lblDesviacion.setText(Formatos.dinero(analisis.getDesviacionEstandar()));
            lblUmbral.setText(Formatos.dinero(analisis.getUmbralAtipico()));
            listaSospechosas.setAll(analisis.getListaTransaccionesSospechosas());
            cargarGrafico(analisisController.obtenerGastosPorMonedero(cliente));
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarError(e.getMessage());
        }
    }

    @FXML
    private void limpiarAnalisis() {
        lblPromedio.setText(Formatos.dinero(0));
        lblDesviacion.setText(Formatos.dinero(0));
        lblUmbral.setText(Formatos.dinero(0));
        listaSospechosas.clear();
        chartGastos.getData().clear();
    }

    private void cargarGrafico(Map<String, Double> gastos) {
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        gastos.forEach((monedero, total) -> serie.getData().add(new XYChart.Data<>(monedero, total)));
        chartGastos.getData().clear();
        chartGastos.getData().add(serie);
    }
}
