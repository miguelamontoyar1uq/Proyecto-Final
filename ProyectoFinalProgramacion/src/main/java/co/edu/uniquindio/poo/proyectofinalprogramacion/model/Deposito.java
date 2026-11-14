package co.edu.uniquindio.poo.proyectofinal.model;

public class Deposito extends Transaccion {

    public Deposito(String id, double monto, String descripcion, EstadoTransaccion estado) {
        super(id, monto, descripcion, estado);
    }

    @Override
    public String getTipo() {
        return "Depósito";
    }
    @Override
    public int calcularPuntosGenerados() {
        return (int) (getMonto() / 100);
    }
}