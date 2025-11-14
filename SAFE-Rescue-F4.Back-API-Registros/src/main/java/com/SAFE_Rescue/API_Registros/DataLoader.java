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

        System.out.println("Carga de datos de registros finalizada.");
    }

    /**
     * Crea y guarda los estados predefinidos.
     * Estos estados pueden ser usados por otras entidades como Usuarios o Incidentes.
     */
    private void crearEstados() {
        List<String> nombresEstados = Arrays.asList(
                "Activo", "Baneado", "Inactivo",
                "En Proceso", "Localizado", "Cerrado",
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
     * Crea y guarda fotos de ejemplo con URLs ficticias.
     */
    private void crearFotosDeEjemplo() {
        // Simple verificación para no duplicar datos en cada reinicio (si no hay borrado previo)
        if (fotoRepository.count() == 0) {
            List<String> urlsDeEjemplo = Arrays.asList(
                    "http://api.ejemplo.com/fotos/1.jpg",
                    "http://api.ejemplo.com/fotos/2.jpg",
                    "http://api.ejemplo.com/fotos/3.jpg"
            );
            urlsDeEjemplo.forEach(url -> {
                Foto foto = new Foto();
                foto.setUrl(url);
                foto.setDescripcion("Foto de ejemplo para DataLoader");
                foto.setFechaSubida(LocalDateTime.now());
                fotoRepository.save(foto);
            });
            System.out.println("Fotos de ejemplo cargadas: " + fotoRepository.count());
        }
    }
}