package co.edu.uniquindio.poo.proyectofinal.model;

public class Retiro extends Transaccion {

    private final double cargoRetiro;

    public Retiro(String id, double monto, String descripcion, EstadoTransaccion estado, double cargoRetiro)  {
        super(id, monto, descripcion,estado);
        this.cargoRetiro=cargoRetiro;
    }

    @Override
    public String getTipo() {
        return "Retiro";
    }
    @Override
    public int calcularPuntosGenerados() {
        return (int) (getMonto() / 100) * 2;
    }

    public double getCargoRetiro() { return cargoRetiro; }
}