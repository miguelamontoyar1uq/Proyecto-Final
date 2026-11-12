module co.edu.uniquindio.poo.proyectofinalprogramacion {
    requires javafx.controls;
    requires javafx.fxml;

    opens co.edu.uniquindio.poo.proyectofinalprogramacion to javafx.fxml;
    exports co.edu.uniquindio.poo.proyectofinalprogramacion;
    opens co.edu.uniquindio.poo.proyectofinalprogramacion.viewController to javafx.fxml;
    opens co.edu.uniquindio.poo.proyectofinalprogramacion.model to javafx.base, javafx.fxml;
}