package com.SAFE_Rescue.API_Donaciones;

import com.SAFE_Rescue.API_Donaciones.modelo.Donacion;
import com.SAFE_Rescue.API_Donaciones.repository.DonacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Clase que se ejecuta al inicio de la aplicación para cargar datos de donaciones de prueba.
 * Implementa CommandLineRunner de Spring Boot.
 */
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private DonacionRepository donacionRepository;

    /**
     * Método principal que se ejecuta al iniciar la aplicación.
     * @param args Argumentos de línea de comandos.
     * @throws Exception Si ocurre un error durante la ejecución.
     */
    @Override
    public void run(String... args) throws Exception {
        if (donacionRepository.count() == 0) {
            System.out.println("Cargando datos iniciales de Donaciones...");
            cargarDonacionesIniciales();
            System.out.println("Datos de Donaciones cargados: " + donacionRepository.count() + " registros.");
        } else {
            System.out.println("La tabla de Donaciones ya contiene datos. Omitiendo la carga inicial.");
        }
    }

    private void cargarDonacionesIniciales() {
        // IDs Lógicos Externos (Asumimos que estos usuarios existen en el servicio de Usuarios)
        Integer donanteId1 = 101; // Ejemplo: Donante frecuente
        Integer donanteId2 = 102; // Ejemplo: Donante por única vez
        Integer donanteId3 = 103; // Ejemplo: Donante con homenaje

        // Creación de Donaciones - Montos ajustados a Integer (CLP)
        Donacion d1 = new Donacion();
        d1.setIdDonante(donanteId1);
        d1.setMonto(50000); // 50.000 CLP
        d1.setMetodoPago("Tarjeta de Crédito");
        d1.setFechaDonacion(LocalDateTime.now().minusDays(10));
        d1.setTipoHomenaje(null); // No homenaje

        Donacion d2 = new Donacion();
        d2.setIdDonante(donanteId2);
        d2.setMonto(15000); // 15.000 CLP
        d2.setMetodoPago("PayPal");
        d2.setFechaDonacion(LocalDateTime.now().minusDays(5));
        d2.setTipoHomenaje(null);

        Donacion d3 = new Donacion();
        d3.setIdDonante(donanteId1);
        d3.setMonto(100000); // 100.000 CLP
        d3.setMetodoPago("Transferencia Bancaria");
        d3.setFechaDonacion(LocalDateTime.now().minusDays(20));
        d3.setTipoHomenaje(null);

        Donacion d4 = new Donacion();
        d4.setIdDonante(donanteId3);
        d4.setMonto(250000); // 250.000 CLP
        d4.setMetodoPago("Tarjeta de Débito");
        d4.setFechaDonacion(LocalDateTime.now().minusDays(2));
        d4.setTipoHomenaje("En memoria de"); // Donación con homenaje
        d4.setDetalleHomenaje("Por el valiente trabajo de rescate.");

        Donacion d5 = new Donacion();
        d5.setIdDonante(donanteId2);
        d5.setMonto(5000); // 5.000 CLP
        d5.setMetodoPago("PayPal");
        d5.setFechaDonacion(LocalDateTime.now().minusHours(5));
        d5.setTipoHomenaje(null);

        List<Donacion> donaciones = Arrays.asList(d1, d2, d3, d4, d5);

        // Guardar todas las entidades
        donacionRepository.saveAll(donaciones);
    }
}