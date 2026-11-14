package co.edu.uniquindio.poo.proyectofinalprogramacion.viewController;

import co.edu.uniquindio.poo.proyectofinalprogramacion.App;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class Alertas {

    private Alertas() {
    }

    public static void mostrarInformacion(String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, "Información", mensaje);
    }

    public static void mostrarAdvertencia(String mensaje) {
        mostrar(Alert.AlertType.WARNING, "Advertencia", mensaje);
    }

    public static void mostrarError(String mensaje) {
        mostrar(Alert.AlertType.ERROR, "Error", mensaje);
    }

    public static boolean confirmar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Confirmación");
        alerta.setHeaderText(null);
        aplicarEstilo(alerta);
        return alerta.showAndWait().filter(boton -> boton == ButtonType.YES).isPresent();
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        aplicarEstilo(alerta);
        alerta.showAndWait();
    }

    // Aplicar estilos.css al diálogo
    private static void aplicarEstilo(Alert alerta) {
        alerta.getDialogPane().getStylesheets()
                .add(App.class.getResource("css/estilos.css").toExternalForm());
    }
}
