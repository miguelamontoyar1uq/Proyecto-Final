package co.edu.uniquindio.poo.proyectofinalprogramacion;

import co.edu.uniquindio.poo.proyectofinalprogramacion.controller.MonederoVirtualController;
import co.edu.uniquindio.poo.proyectofinalprogramacion.viewController.Alertas;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class App extends Application {

    private Stage stage;
    private MonederoVirtualController monederoVirtualController;

    public App() {
        this.monederoVirtualController = MonederoVirtualController.getInstancia();
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        cargarFuente();
        stage.setTitle("E-MONEY");
        stage.setMinWidth(900);
        stage.setMinHeight(550);
        mostrarPrincipal();
        stage.show();
    }

    public void mostrarPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/Principal.fxml"));
            cambiarVista(loader.load());
        } catch (IOException e) {
            Alertas.mostrarError("No se pudo abrir la aplicación: " + e.getMessage());
            Platform.exit();
        }
    }

    public MonederoVirtualController getMonederoVirtualController() {
        return monederoVirtualController;
    }

    // Registra DM Serif Display para el CSS
    private void cargarFuente() {
        try (InputStream fuente = App.class.getResourceAsStream("fonts/DMSerifDisplay-Regular.ttf")) {
            if (fuente == null || Font.loadFont(fuente, 15) == null) {
                System.out.println("No se encontró la fuente DM Serif Display; se usa la fuente por defecto.");
            }
        } catch (IOException e) {
            System.out.println("No se pudo leer la fuente: " + e.getMessage());
        }
    }

    /** Pone la vista en la ventana (la primera vez crea la escena, después solo cambia la raíz). */
    private void cambiarVista(Parent raiz) {
        if (stage.getScene() == null) {
            stage.setScene(new Scene(raiz, 1300, 750));
        } else {
            stage.getScene().setRoot(raiz);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}