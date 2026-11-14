package co.edu.uniquindio.poo.proyectofinal.model;

public enum TipoBeneficio {
    DESCUENTO_TRANSFERENCIA("Reducción del 10% en la comisión por transferencias", 100),
    SIN_CARGOS_RETIRO("Un mes sin cargos por retiros", 500),
    BONO_SALDO("Bono de saldo de 50 unidades en el monedero", 1000);

    private final String descripcion;
    private final int costoPuntos;

    TipoBeneficio(String descripcion, int costoPuntos) {
        this.descripcion = descripcion;
        this.costoPuntos = costoPuntos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getCostoPuntos() {
        return costoPuntos;
    }
}