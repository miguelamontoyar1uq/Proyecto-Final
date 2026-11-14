package co.edu.uniquindio.poo.proyectofinalprogramacion.viewController;

import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public class Formatos {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Formatos() {
    }

    public static String dinero(double valor) {
        return String.format("$ %,.2f", valor);
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "—" : fecha.format(FECHA);
    }

    public static String fechaHora(LocalDateTime fecha) {
        return fecha == null ? "—" : fecha.format(FECHA_HORA);
    }

    // Convertir texto a número decimal
    public static double decimal(String texto, String campo) {
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser un número");
        }
    }

    // Texto a mostrar en ComboBox
    public static <T> StringConverter<T> texto(Function<T, String> funcion) {
        return new StringConverter<>() {
            @Override
            public String toString(T objeto) {
                return objeto == null ? "" : funcion.apply(objeto);
            }

            @Override
            public T fromString(String texto) {
                return null;
            }
        };
    }
}
