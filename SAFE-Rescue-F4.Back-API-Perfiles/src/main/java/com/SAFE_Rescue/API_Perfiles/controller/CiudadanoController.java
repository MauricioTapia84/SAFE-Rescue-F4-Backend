package com.SAFE_Rescue.API_Perfiles.controller;

import com.SAFE_Rescue.API_Perfiles.modelo.Ciudadano;
import com.SAFE_Rescue.API_Perfiles.service.CiudadanoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de perfiles de Ciudadanos.
 * Rutas: /api/v1/ciudadanos
 */
@RestController
@RequestMapping("/api/v1/ciudadanos")
public class CiudadanoController {

    private final CiudadanoService ciudadanoService;

    // Inyección de dependencias por constructor (preferida por Spring)
    public CiudadanoController(CiudadanoService ciudadanoService) {
        this.ciudadanoService = ciudadanoService;
    }

    /**
     * Endpoint para crear un nuevo perfil de Ciudadano.
     * La creación de la dirección se maneja dentro del servicio.
     *
     * @param ciudadano Clase del ciudadano a crear.
     * @return El Ciudadano persistido con su ID, y estado HTTP 201 CREATED.
     */
    @PostMapping
    public ResponseEntity<Ciudadano> crearCiudadano(@Valid @RequestBody Ciudadano ciudadano) {
        // En una creación, el ID debería ser nulo.
        if (ciudadano.getIdUsuario() != null) {
            // Se puede lanzar una excepción o simplemente ignorar el ID.
            // Para simplicidad, se puede resetear o dejar que el servicio lo maneje.
            // Aquí asumimos que el servicio garantiza la creación de uno nuevo.
        }
        Ciudadano ciudadanoGuardado = ciudadanoService.save(ciudadano);
        return new ResponseEntity<>(ciudadanoGuardado, HttpStatus.CREATED);
    }

    /**
     * Endpoint para obtener un perfil de Ciudadano por su ID.
     *
     * @param id ID del ciudadano.
     * @return El Ciudadano correspondiente y estado HTTP 200 OK.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Ciudadano> getCiudadanoById(@PathVariable Integer id) {
        Ciudadano ciudadano = ciudadanoService.findById(id);
        return ResponseEntity.ok(ciudadano);
    }

    /**
     * Endpoint para actualizar completamente un perfil de Ciudadano existente.
     * El ID en la URL y en el cuerpo deben coincidir.
     *
     * @param id ID del ciudadano a actualizar (tomado de la URL).
     * @param ciudadano Clase del ciudadano con los nuevos datos.
     * @return El Ciudadano actualizado y estado HTTP 200 OK.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Ciudadano> actualizarCiudadano(@PathVariable Integer id, @Valid @RequestBody Ciudadano ciudadano) {
        // Asignar el ID de la URL al objeto para asegurar que el servicio actualice el correcto.
        ciudadano.setIdUsuario(id);

        // El servicio debe verificar que el ciudadano exista y luego llamar a save().
        Ciudadano ciudadanoActualizado = ciudadanoService.save(ciudadano);

        return ResponseEntity.ok(ciudadanoActualizado);
    }

    /**
     * Endpoint para eliminar un perfil de Ciudadano por su ID.
     *
     * @param id ID del ciudadano a eliminar.
     * @return Respuesta sin contenido y estado HTTP 204 NO CONTENT.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCiudadano(@PathVariable Integer id) {
        ciudadanoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}