package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.config.EstadoClient; // NUEVA INYECCIÓN
import com.SAFE_Rescue.API_Perfiles.config.FotoClient;   // NUEVA INYECCIÓN
import com.SAFE_Rescue.API_Perfiles.modelo.Usuario;
import com.SAFE_Rescue.API_Perfiles.repositoy.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Servicio para la gestión de usuarios.
 * Maneja operaciones CRUD y validaciones de negocio.
 * Este servicio interactúa con APIs externas (Estado y Foto) a través de Clientes WebClient dedicados.
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoUsuarioService tipoUsuarioService;

    // INYECCIONES CORREGIDAS: Inyectamos los clientes dedicados en lugar de WebClientConfig
    @Autowired
    private EstadoClient estadoClient;

    @Autowired
    private FotoClient fotoClient;

    /**
     * Obtiene todos los usuarios registrados en el sistema.
     *
     * @return Lista de todos los usuarios.
     */
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    /**
     * Busca un usuario por su ID único.
     *
     * @param id El ID del usuario.
     * @return El usuario encontrado.
     * @throws NoSuchElementException Si el usuario no es encontrado.
     */
    public Usuario findById(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));
    }

    /**
     * Guarda un nuevo usuario en la base de datos.
     *
     * @param usuario El objeto Usuario a guardar.
     * @return El usuario guardado.
     * @throws IllegalArgumentException Si el usuario no cumple con las validaciones o si las entidades relacionadas no existen.
     */
    public Usuario save(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }

        validarAtributosUsuario(usuario);
        validarExistencia(usuario);

        try {
            return usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error de integridad de datos. El RUN o correo electrónico ya existen.");
        }
    }

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param usuario El objeto Usuario con los datos actualizados.
     * @param id      El ID del usuario a actualizar.
     * @return El usuario actualizado.
     * @throws IllegalArgumentException Si los datos del usuario son inválidos o si las entidades relacionadas no existen.
     * @throws NoSuchElementException   Si el usuario a actualizar no es encontrado.
     */
    public Usuario update(Usuario usuario, Integer id) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario a actualizar no puede ser nulo.");
        }

        // Se valida el objeto usuario, incluyendo la existencia de sus relaciones
        validarAtributosUsuario(usuario);
        validarExistencia(usuario);

        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));

        // Actualizar los campos del usuario existente con los nuevos valores
        usuarioExistente.setRun(usuario.getRun());
        usuarioExistente.setDv(usuario.getDv());
        usuarioExistente.setNombre(usuario.getNombre());
        usuarioExistente.setAPaterno(usuario.getAPaterno());
        usuarioExistente.setAMaterno(usuario.getAMaterno());
        usuarioExistente.setFechaRegistro(usuario.getFechaRegistro());
        usuarioExistente.setTelefono(usuario.getTelefono());
        usuarioExistente.setCorreo(usuario.getCorreo());
        usuarioExistente.setContrasenia(usuario.getContrasenia());
        usuarioExistente.setIntentosFallidos(usuario.getIntentosFallidos());
        usuarioExistente.setRazonBaneo(usuario.getRazonBaneo());
        usuarioExistente.setDiasBaneo(usuario.getDiasBaneo());

        // --- AJUSTE DE CLAVES FORÁNEAS LÓGICAS (Microservicios) ---
        usuarioExistente.setIdEstado(usuario.getIdEstado());
        usuarioExistente.setIdFoto(usuario.getIdFoto());

        // La relación TipoUsuario es la única que queda como ManyToOne (asumimos que es local)
        usuarioExistente.setTipoUsuario(usuario.getTipoUsuario());
        // --- FIN AJUSTE ---

        try {
            return usuarioRepository.save(usuarioExistente);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error de integridad de datos. El RUN, teléfono o correo ya existen.");
        }
    }

    /**
     * Elimina un usuario por su ID.
     *
     * @param id El ID del usuario a eliminar.
     * @throws NoSuchElementException Si el usuario no es encontrado.
     * @throws IllegalStateException Si el usuario tiene referencias activas (ej. líder de equipo).
     */
    public void delete(Integer id) {
        Usuario usuario = findById(id); // Reusamos findById para verificar existencia

        try {
            usuarioRepository.delete(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("No se puede eliminar el usuario porque tiene referencias activas (ej. es líder de un equipo).", e);
        }
    }

    // Métodos de validación y utilidades

    /**
     * Valida los atributos obligatorios del usuario.
     *
     * @param usuario El objeto Usuario a validar.
     * @throws IllegalArgumentException Si algún atributo es nulo o no cumple las reglas de negocio.
     */
    public void validarAtributosUsuario(Usuario usuario) {
        // Se mantiene la validación de atributos.
        if (usuario.getRun() == null || usuario.getRun().trim().isEmpty() ||
                usuario.getDv() == null || usuario.getDv().trim().isEmpty() ||
                usuario.getNombre() == null || usuario.getNombre().trim().isEmpty() ||
                usuario.getAPaterno() == null || usuario.getAPaterno().trim().isEmpty() ||
                usuario.getAMaterno() == null || usuario.getAMaterno().trim().isEmpty() ||
                usuario.getFechaRegistro() == null ||
                usuario.getTelefono() == null || usuario.getTelefono().trim().isEmpty() ||
                usuario.getCorreo() == null || usuario.getCorreo().trim().isEmpty() ||
                usuario.getContrasenia() == null || usuario.getContrasenia().trim().isEmpty()) {
            throw new IllegalArgumentException("Todos los campos obligatorios del usuario deben ser proporcionados.");
        }
    }

    /**
     * Valida que las entidades relacionadas (Estado y TipoUsuario) existan.
     * Se comunica con la API externa para validar la existencia del estado.
     *
     * @param usuario El objeto Usuario a validar.
     * @throws IllegalArgumentException Si alguna de las entidades relacionadas no existe.
     */
    void validarExistencia(Usuario usuario) {
        // Valida la existencia del Tipo de Usuario (local)
        if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().getIdTipoUsuario() != null) {
            try {
                tipoUsuarioService.findById(usuario.getTipoUsuario().getIdTipoUsuario());
            } catch (NoSuchElementException e) {
                throw new IllegalArgumentException("El tipo de usuario asociado no existe.");
            }
        } else {
            throw new IllegalArgumentException("El tipo de usuario es un campo obligatorio.");
        }

        // Valida la existencia del Estado (externo)
        if (usuario.getIdEstado() != null) {
            try {
                // CORRECCIÓN: Usamos el cliente dedicado EstadoClient
                estadoClient.getEstadoById(usuario.getIdEstado());
            } catch (RuntimeException e) { // Captura las RuntimeException lanzadas por el cliente (4xx, 5xx, u otros)
                // Se lanza una excepción de negocio con un mensaje claro
                throw new IllegalArgumentException("El ID de estado asociado no es válido o el servicio externo falló. Detalle: " + e.getMessage(), e);
            }
        } else {
            throw new IllegalArgumentException("El ID de estado es un campo obligatorio.");
        }

        // Nota: La validación de idFoto no es obligatoria, ya que el campo puede ser nulo.
    }

    /**
     * Sube un archivo de foto a la API de fotos y actualiza el perfil del usuario.
     *
     * @param id El ID del usuario al que se le asociará la foto.
     * @param archivo El archivo de la foto a subir.
     * @return El objeto **Usuario actualizado** con el nuevo ID de foto.
     * @throws IllegalArgumentException si el archivo está vacío.
     * @throws NoSuchElementException si el usuario no es encontrado.
     * @throws RuntimeException si la API externa devuelve un formato de ID incorrecto o falla la comunicación.
     */
    public Usuario subirYActualizarFotoUsuario(Integer id, MultipartFile archivo) {

        // 1. Lógica para subir el archivo a la otra API.
        // CORRECCIÓN: Usamos el cliente dedicado FotoClient
        String fotoIdString = fotoClient.uploadFoto(archivo);

        Integer idFoto;
        try {
            // Se valida que la respuesta de la API externa sea un ID numérico válido.
            idFoto = Integer.parseInt(fotoIdString);
        } catch (NumberFormatException e) {
            // Manejamos el caso si la API externa no devolvió un ID numérico
            throw new RuntimeException("La API externa de fotos no devolvió un ID de foto válido (Integer). Se recibió: " + fotoIdString, e);
        } catch (RuntimeException e) {
            // Captura si el FotoClient falló (4xx, 5xx, IO)
            throw new RuntimeException("Error al subir la foto a la API externa. Detalle: " + e.getMessage(), e);
        }

        // 2. Buscar al usuario y actualizar su ID de foto
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));

        usuario.setIdFoto(idFoto); // Actualizamos el ID de la clave foránea lógica

        // 3. Guardar el usuario actualizado y retornarlo
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        // Se retorna el objeto Usuario actualizado, cumpliendo con la expectativa del controlador.
        return usuarioActualizado;
    }
}