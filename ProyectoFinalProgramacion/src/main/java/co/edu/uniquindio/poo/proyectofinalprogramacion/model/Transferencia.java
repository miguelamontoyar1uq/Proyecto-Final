package co.edu.uniquindio.poo.proyectofinal.model;

public class Transferencia extends Transaccion {
    private String cuentaDestino;
    private double comisionTransferencia;

    public Transferencia(String id, double monto, String descripcion, String cuentaDestino, EstadoTransaccion estado, double comisionTransferencia) {
        super(id, monto, descripcion,estado);
        this.cuentaDestino = cuentaDestino;
        this.comisionTransferencia= comisionTransferencia;
    }

    @Override
    public String getTipo() {
        return "Transferencia";
    }


    @Override
    public int calcularPuntosGenerados() {
        return (int) (getMonto() / 100) * 3;
    }

    public String getCuentaDestino() {
        return cuentaDestino;
    }

    public void setCuentaDestino(String cuentaDestino) {
        this.cuentaDestino = cuentaDestino;
    }

    public double getComisionTransferencia() {
        return comisionTransferencia;
    }

    public void setComisionTransferencia(double comisionTransferencia) {
        this.comisionTransferencia = comisionTransferencia;
    }
}