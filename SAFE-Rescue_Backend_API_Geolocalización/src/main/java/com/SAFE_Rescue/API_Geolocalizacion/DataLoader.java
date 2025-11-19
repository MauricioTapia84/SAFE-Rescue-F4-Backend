package com.SAFE_Rescue.API_Geolocalizacion;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Comuna;
import com.SAFE_Rescue.API_Geolocalizacion.modelo.Direccion;
import com.SAFE_Rescue.API_Geolocalizacion.modelo.Coordenadas; // Actualizado
import com.SAFE_Rescue.API_Geolocalizacion.modelo.Region;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.ComunaRepository;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.CoordenadasRepository;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.DireccionRepository;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.RegionRepository;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Clase encargada de cargar datos de prueba para las entidades de Coordenadas
 * al iniciar la aplicación en el perfil "dev".
 */
@Profile("dev")
@Component
public class DataLoader implements CommandLineRunner {

    // --- REPOSITORIOS DE COORDENADAS --- // Comentario actualizado
    @Autowired private RegionRepository regionRepository;
    @Autowired private ComunaRepository comunaRepository;
    @Autowired private CoordenadasRepository coordenadasRepository;
    @Autowired private DireccionRepository direccionRepository;

    private final Faker faker = new Faker(new Locale("es"));

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("Cargando datos de prueba de Coordenadas..."); // Mensaje actualizado

        try {
            // 0. LIMPIEZA DE DATOS (Para evitar Duplicate Entry)
            limpiarDatosExistentes();

            // 1. GENERAR DATOS GEOGRÁFICOS
            List<Region> regiones = crearRegiones();

            // Buscar la Región Metropolitana para la carga de comunas y direcciones
            Region regionMetropolitana = regiones.stream()
                    .filter(r -> "RM".equalsIgnoreCase(r.getIdentificacion()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Error: No se encontró la Región Metropolitana (RM)."));

            List<Comuna> comunasRM = crearComunasRegionMetropolitana(regionMetropolitana);

            // 2. CREAR COORDENADAS Y DIRECCIONES EN SANTIAGO // Comentario actualizado
            crearDireccionesYCoordenadas(comunasRM); // Nombre del método actualizado

            System.out.println("Carga de datos de Coordenadas finalizada con éxito."); // Mensaje actualizado
        } catch (Exception e) {
            System.err.println("Un error inesperado ocurrió durante la carga de datos:");
            e.printStackTrace();
        }
    }

    // --- NUEVO MÉTODO PARA LIMPIEZA DE DATOS ---
    // El orden inverso es necesario para las restricciones de clave externa.
    private void limpiarDatosExistentes() {
        System.out.println("-> Limpiando datos existentes...");
        // Borrar dependientes primero
        direccionRepository.deleteAllInBatch();
        coordenadasRepository.deleteAllInBatch();
        comunaRepository.deleteAllInBatch();
        regionRepository.deleteAllInBatch();
        // Borrar entidades base al final
        System.out.println("-> Limpieza completada.");
    }

    // --- MÉTODOS PARA CREAR ENTIDADES GEOGRÁFICAS ---

    private List<Region> crearRegiones() {

        // Definición de las 16 regiones de Chile
        Map<String, String> regionesData = new LinkedHashMap<>();
        regionesData.put("Región de Arica y Parinacota", "XV");
        regionesData.put("Región de Tarapacá", "I");
        regionesData.put("Región de Antofagasta", "II");
        regionesData.put("Región de Atacama", "III");
        regionesData.put("Región de Coquimbo", "IV");
        regionesData.put("Región de Valparaíso", "V");
        regionesData.put("Región Metropolitana de Santiago", "RM");
        regionesData.put("Región del Libertador Gral. Bernardo O'Higgins", "VI");
        regionesData.put("Región del Maule", "VII");
        regionesData.put("Región de Ñuble", "XVI");
        regionesData.put("Región del Biobío", "VIII");
        regionesData.put("Región de La Araucanía", "IX");
        regionesData.put("Región de Los Ríos", "XIV");
        regionesData.put("Región de Los Lagos", "X");
        regionesData.put("Región Aisén del Gral. Carlos Ibáñez del Campo", "XI");
        regionesData.put("Región de Magallanes y de la Antártica Chilena", "XII");

        List<Region> regiones = new ArrayList<>();
        for (Map.Entry<String, String> entry : regionesData.entrySet()) {
            Region region = new Region();
            region.setNombre(entry.getKey());
            region.setIdentificacion(entry.getValue());
            regiones.add(regionRepository.save(region));
        }

        System.out.println("-> Creadas " + regiones.size() + " regiones chilenas.");
        return regiones;
    }

    private List<Comuna> crearComunasRegionMetropolitana(Region regionMetropolitana) {
        // Comunas de la Región Metropolitana (RM)
        List<String> comunasRM = Arrays.asList(
                "Santiago", "Providencia", "Las Condes", "Vitacura", "Ñuñoa",
                "La Florida", "Puente Alto", "Maipú", "San Bernardo", "Quilicura"
        );

        List<Comuna> comunas = new ArrayList<>();
        int codigoPostalBase = 7500000;
        for (String nombre : comunasRM) {
            Comuna comuna = new Comuna();
            comuna.setNombre(nombre);
            comuna.setRegion(regionMetropolitana);
            comuna.setCodigoPostal(String.valueOf(codigoPostalBase++));

            comunas.add(comunaRepository.save(comuna));
        }

        System.out.println("-> Creadas " + comunas.size() + " comunas de la Región Metropolitana.");
        return comunas;
    }

    /**
     * Crea direcciones y asigna coordenadas únicas a cada una. // Comentario actualizado
     */
    private List<Direccion> crearDireccionesYCoordenadas(List<Comuna> comunasRM) { // Nombre del método actualizado
        List<Direccion> direcciones = new ArrayList<>();
        final int NUM_DIRECCIONES = 15;

        for (int i = 0; i < NUM_DIRECCIONES; i++) {

            // 1. Seleccionar una Comuna al azar
            Comuna comunaSeleccionada = comunasRM.get(faker.random().nextInt(comunasRM.size()));

            // 2. Crear Coordenadas (Estado Transient) // Comentario actualizado
            Coordenadas coordenadas = new Coordenadas(); // Variable actualizada

            // Latitud: Rango de Santiago central (-33.3 a -33.5)
            // Longitud: Rango de Santiago central (-70.5 a -70.8)
            coordenadas.setLatitud( // Variable actualizada
                    (float) faker.number().randomDouble(6,(long) -33.5,(long) -33.3)
            );

            coordenadas.setLongitud( // Variable actualizada
                    (float) faker.number().randomDouble(6,(long) -70.8,(long) -70.5)
            );

            // NOTA: ¡No se llama a coordenadasRepository.save()!
            // Esto evita el error "detached entity passed to persist".

            // 3. Crear Direccion
            Direccion direccion = new Direccion();
            direccion.setCalle(faker.address().streetName());
            direccion.setNumero(faker.address().buildingNumber());

            // Complemento y Villa son opcionales
            if (faker.random().nextBoolean()) {
                direccion.setComplemento("Depto " + faker.number().digits(2));
            }
            if (faker.random().nextBoolean()) {
                direccion.setVilla("Villa " + faker.artist().name());
            }

            direccion.setComuna(comunaSeleccionada);
            // Establecer la relación
            direccion.setCoordenadas(coordenadas); // Método actualizado

            // 4. Guardar SOLO la Direccion. Si Direccion tiene CascadeType.PERSIST,
            // las Coordenadas se guardarán automáticamente. // Comentario actualizado
            direcciones.add(direccionRepository.save(direccion));
        }

        System.out.println("-> Creadas " + direcciones.size() + " direcciones y coordenadas en Santiago."); // Mensaje actualizado
        return direcciones;
    }
}