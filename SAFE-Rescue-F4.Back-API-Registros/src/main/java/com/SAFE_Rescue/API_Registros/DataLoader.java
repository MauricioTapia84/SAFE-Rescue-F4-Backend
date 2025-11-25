package com.SAFE_Rescue.API_Registros;

import com.SAFE_Rescue.API_Registros.modelo.Estado;
import com.SAFE_Rescue.API_Registros.modelo.Foto;
import com.SAFE_Rescue.API_Registros.repository.EstadoRepository;
import com.SAFE_Rescue.API_Registros.repository.FotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

/**
 * Clase que carga datos iniciales de configuración (Estados y Fotos de ejemplo)
 * para la API de Registros.
 * Solo se ejecuta en el perfil 'dev'.
 */
@Profile("dev")
@Component
public class DataLoader implements CommandLineRunner {

    // INYECCIONES DE DEPENDENCIA (Solo las entidades restantes)
    @Autowired
    private EstadoRepository estadoRepository;

    @Autowired
    private FotoRepository fotoRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Cargando datos de registros iniciales...");

        // 1. Cargar los estados
        crearEstados();

        // 2. Cargar las fotos de ejemplo
        crearFotosDeEjemplo();

        System.out.println("Carga de datos de registros iniciales completada.");
    }

    // --- METODOS PRIVADOS DE CARGA DE DATOS ---

    /**
     * Crea y guarda los estados predefinidos del sistema.
     */
    private void crearEstados() {
        List<String> nombresEstados = Arrays.asList(
                "Activo", "En Proceso", "Localizado", "Cerrado",
                "Enviado", "Recibido", "Visto"
        );

        // Se verifica la existencia antes de guardar para evitar duplicados
        nombresEstados.forEach(nombre -> {
            if (estadoRepository.findByNombre(nombre).isEmpty()) {
                Estado estado = new Estado();
                estado.setNombre(nombre);
                estadoRepository.save(estado);
            }
        });
        System.out.println("Estados cargados: " + estadoRepository.count());
    }

    /**
     * Crea y guarda 10 fotos de ejemplo con URLs ficticias API.
     * La ruta es 'http://localhost:8080/api-registros/v1/fotos/{id}/archivo'.
     */
    private void crearFotosDeEjemplo() {
        // Simple verificación para no duplicar datos en cada reinicio (si no hay borrado previo)
        if (fotoRepository.count() == 0) {


            IntStream.rangeClosed(1, 10).forEach(i -> {
                Foto foto = new Foto();

                String urlSimulada = String.format("http://localhost:8080/api-registros/v1/fotos/%d/archivo", i);
                foto.setUrl(urlSimulada);

                foto.setDescripcion(String.format("Foto de ejemplo %d cargada.", i));
                foto.setFechaSubida(LocalDateTime.now());

                // Valores de ejemplo para campos opcionales/técnicos
                foto.setTipo("image/png");
                foto.setTamanio(102400 + i * 100);

                fotoRepository.save(foto);
            });
            System.out.println("Fotos de ejemplo cargadas: " + fotoRepository.count());
        }
    }
}