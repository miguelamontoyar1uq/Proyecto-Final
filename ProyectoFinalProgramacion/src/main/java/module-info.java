module co.edu.uniquindio.poo.proyectofinalprogramacion {
    requires javafx.controls;
    requires javafx.fxml;


    opens co.edu.uniquindio.poo.proyectofinalprogramacion to javafx.fxml;
    exports co.edu.uniquindio.poo.proyectofinalprogramacion;
}