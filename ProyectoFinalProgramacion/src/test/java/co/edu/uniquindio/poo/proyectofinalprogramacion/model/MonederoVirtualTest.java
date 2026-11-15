package co.edu.uniquindio.poo.proyectofinalprogramacion.model;

import co.edu.uniquindio.poo.proyectofinalprogramacion.exceptions.SaldoInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MonederoVirtualTest {

    private MonederoVirtual monederoVirtual;
    private Cliente cliente;

    @BeforeEach
    void preparar() {
        monederoVirtual = new MonederoVirtual("Monedero Uniquindío");
        cliente = new Cliente("1", "Juan", "123456789", "3001234567", "juan@correo.com");

        // Gastos: saldo 50.000, límite de retiro 100.000, umbral de saldo bajo 1.000
        cliente.agregarMonedero("Gastos", new MonederoGastos("G-001", 50000, 100000, 1000));
        // Ahorros: saldo 0, límite de retiro 100.000, meta 1.000.000
        cliente.agregarMonedero("Ahorros", new MonederoAhorros("A-001", 1, 100000, 1000000));

        monederoVirtual.registrarCliente(cliente);
    }

    // 1. Un depósito suma al saldo y genera puntos (1 punto por cada 100)
    @Test
    void depositarSumaSaldoYGeneraPuntos() throws Exception {
        monederoVirtual.depositar("1", "G-001", 20000);

        assertEquals(70000, monederoVirtual.consultarSaldo("1", "G-001"), 0.001);
        assertEquals(200, cliente.getPuntosAcumulados());
    }

    // 2. Un retiro descuenta el monto más el cargo del 1%
    @Test
    void retirarDescuentaMontoMasCargo() throws Exception {
        Retiro retiro = monederoVirtual.retirar("1", "G-001", 10000);

        assertEquals(100, retiro.getCargoRetiro(), 0.001);
        assertEquals(39900, monederoVirtual.consultarSaldo("1", "G-001"), 0.001);
    }

    // 3. Retirar más de lo que hay lanza SaldoInsuficienteException
    @Test
    void retirarSinSaldoLanzaExcepcion() {
        assertThrows(SaldoInsuficienteException.class,
                () -> monederoVirtual.retirar("1", "G-001", 60000));

        // El saldo no debe cambiar
        assertEquals(50000, monederoVirtual.consultarSaldo("1", "G-001"), 0.001);
    }

    // 4. Una transferencia de un cliente BRONCE cobra 10% de comisión
    @Test
    void transferirCobraComisionSegunRango() throws Exception {
        Transferencia t = monederoVirtual.transferir("1", "G-001", "A-001", 10000);

        assertEquals(1000, t.getComisionTransferencia(), 0.001);
        assertEquals(39000, monederoVirtual.consultarSaldo("1", "G-001"), 0.001);  // 50.000 - 10.000 - 1.000
        assertEquals(10001, monederoVirtual.consultarSaldo("1", "A-001"), 0.001);  // 1 + 10.000
    }

    // 5. Revertir un depósito devuelve el saldo, marca la transacción y quita los puntos
    @Test
    void revertirDepositoDevuelveSaldoYPuntos() throws Exception {
        Deposito deposito = monederoVirtual.depositar("1", "G-001", 20000);

        monederoVirtual.revertirTransaccion("1", deposito.getId());

        assertEquals(EstadoTransaccion.REVERTIDA, deposito.getEstado());
        assertEquals(50000, monederoVirtual.consultarSaldo("1", "G-001"), 0.001);
        assertEquals(0, cliente.getPuntosAcumulados());
    }
}