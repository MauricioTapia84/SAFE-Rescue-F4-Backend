package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.config.EstadoClient;
import com.SAFE_Rescue.API_Perfiles.config.FotoClient;
import com.SAFE_Rescue.API_Perfiles.modelo.Usuario;
import com.SAFE_Rescue.API_Perfiles.repository.UsuarioRepository;
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

    @Autowired
    private EstadoClient estadoClient;

    @Autowired
    private FotoClient fotoClient;

    /**
     * Obtiene todos los usuarios registrados en el sistema.
     */
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    /**
     * Busca un usuario por su ID único.
     */
    public Usuario findById(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));
    }

    /**
     * NUEVO: Busca un usuario por su Nombre de Usuario (Nick).
     */
    public Usuario findByNombreUsuario(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con el nombre de usuario: " + nombreUsuario));
    }

    /**
     * Guarda un nuevo usuario en la base de datos.
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
            throw new IllegalArgumentException("Error de integridad de datos. El RUN, nombre de usuario o correo electrónico ya existen.");
        }
    }

    /**
     * Actualiza los datos de un usuario existente.
     */
    public Usuario update(Usuario usuario, Integer id) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario a actualizar no puede ser nulo.");
        }

        validarAtributosUsuario(usuario);
        validarExistencia(usuario);

        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));

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

        // Actualizar también el nombre de usuario si viene
        if (usuario.getNombreUsuario() != null) {
            usuarioExistente.setNombreUsuario(usuario.getNombreUsuario());
        }

        usuarioExistente.setIdEstado(usuario.getIdEstado());
        usuarioExistente.setIdFoto(usuario.getIdFoto());
        usuarioExistente.setTipoUsuario(usuario.getTipoUsuario());

        try {
            return usuarioRepository.save(usuarioExistente);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error de integridad de datos. El RUN, nombre de usuario, teléfono o correo ya existen.");
        }
    }

    /**
     * Elimina un usuario por su ID.
     */
    public void delete(Integer id) {
        Usuario usuario = findById(id);

        try {
            usuarioRepository.delete(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("No se puede eliminar el usuario porque tiene referencias activas.", e);
        }
    }

    // Métodos de validación y utilidades

    public void validarAtributosUsuario(Usuario usuario) {
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

    void validarExistencia(Usuario usuario) {
        if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().getIdTipoUsuario() != null) {
            try {
                tipoUsuarioService.findById(usuario.getTipoUsuario().getIdTipoUsuario());
            } catch (NoSuchElementException e) {
                throw new IllegalArgumentException("El tipo de usuario asociado no existe.");
            }
        } else {
            throw new IllegalArgumentException("El tipo de usuario es un campo obligatorio.");
        }

        if (usuario.getIdEstado() != null) {
            try {
                estadoClient.getEstadoById(usuario.getIdEstado());
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("El ID de estado asociado no es válido o el servicio externo falló. Detalle: " + e.getMessage(), e);
            }
        } else {
            throw new IllegalArgumentException("El ID de estado es un campo obligatorio.");
        }
    }

    public Usuario subirYActualizarFotoUsuario(int idUsuario, MultipartFile fotoBinaria) {
        System.out.println(" [UsuarioService] Subiendo foto para userId: " + idUsuario);
        System.out.println("   Nombre archivo: " + fotoBinaria.getOriginalFilename());
        System.out.println("   Tamaño: " + fotoBinaria.getSize() + " bytes");

        try {
            // 1. Obtener usuario
            System.out.println(" Buscando usuario con ID: " + idUsuario);
            Usuario usuario = usuarioRepository.findById(idUsuario)
                    .orElseThrow(() -> {
                        System.err.println(" Usuario no encontrado con ID: " + idUsuario);
                        return new RuntimeException("Usuario no encontrado con ID: " + idUsuario);
                    });
            System.out.println(" Usuario encontrado: " + usuario.getNombre());

            // 2. Subir foto a API de Registros
            System.out.println(" Subiendo foto a API de Registros...");
            byte[] fotobytes = fotoBinaria.getBytes();

            //  Obtener ID de la foto
            Integer idFotoGuardada = fotoClient.uploadFoto(fotobytes, fotoBinaria.getOriginalFilename());

            System.out.println(" Foto subida exitosamente - ID: " + idFotoGuardada);

            // 3. Actualizar usuario con el ID de la foto
            System.out.println(" Actualizando usuario con ID de foto: " + idFotoGuardada);
            usuario.setIdFoto(idFotoGuardada);
            Usuario usuarioActualizado = usuarioRepository.save(usuario);

            System.out.println(" Usuario actualizado con foto - ID: " + idFotoGuardada);
            System.out.println("═══════════════════════════════════════════");

            //  RETORNAR el usuario actualizado
            return usuarioActualizado;

        } catch (RuntimeException e) {
            System.err.println(" Error RuntimeException: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println(" Error general en subirYActualizarFotoUsuario: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al subir la foto: " + e.getMessage(), e);
        }
    }

    /**
     * Actualización parcial de usuario usando PATCH
     * Solo actualiza los campos que se proporcionan en el request
     */
    public Usuario actualizarParcialmente(Integer id, Usuario usuarioParcial) {
        System.out.println("🔄 [UsuarioService] Actualizando parcialmente usuario ID: " + id);
        System.out.println("   Datos recibidos: " + usuarioParcial);

        // Buscar el usuario existente
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));

        // Aplicar los cambios solo a los campos proporcionados (no null)
        aplicarCambiosParciales(usuarioExistente, usuarioParcial);

        // Validar el usuario actualizado
        validarAtributosUsuario(usuarioExistente);

        // Guardar los cambios
        try {
            Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
            System.out.println("✅ [UsuarioService] Usuario actualizado parcialmente - ID: " + id);
            return usuarioActualizado;

        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error de integridad de datos. El RUN, nombre de usuario o correo electrónico ya existen.");
        }
    }

    /**
     * Aplica los cambios parciales solo a los campos proporcionados en el PATCH
     */
    private void aplicarCambiosParciales(Usuario usuarioExistente, Usuario usuarioParcial) {
        // Actualizar solo los campos que vienen en el request (no null)

        if (usuarioParcial.getIdFoto() != null) {
            System.out.println("   📸 Actualizando idFoto: " + usuarioExistente.getIdFoto() + " -> " + usuarioParcial.getIdFoto());
            usuarioExistente.setIdFoto(usuarioParcial.getIdFoto());
        }

        if (usuarioParcial.getNombre() != null && !usuarioParcial.getNombre().trim().isEmpty()) {
            System.out.println("   👤 Actualizando nombre: " + usuarioExistente.getNombre() + " -> " + usuarioParcial.getNombre());
            usuarioExistente.setNombre(usuarioParcial.getNombre());
        }

        if (usuarioParcial.getAPaterno() != null && !usuarioParcial.getAPaterno().trim().isEmpty()) {
            System.out.println("   📝 Actualizando aPaterno: " + usuarioExistente.getAPaterno() + " -> " + usuarioParcial.getAPaterno());
            usuarioExistente.setAPaterno(usuarioParcial.getAPaterno());
        }

        if (usuarioParcial.getAMaterno() != null && !usuarioParcial.getAMaterno().trim().isEmpty()) {
            System.out.println("   📝 Actualizando aMaterno: " + usuarioExistente.getAMaterno() + " -> " + usuarioParcial.getAMaterno());
            usuarioExistente.setAMaterno(usuarioParcial.getAMaterno());
        }

        if (usuarioParcial.getTelefono() != null && !usuarioParcial.getTelefono().trim().isEmpty()) {
            System.out.println("   📞 Actualizando telefono: " + usuarioExistente.getTelefono() + " -> " + usuarioParcial.getTelefono());
            usuarioExistente.setTelefono(usuarioParcial.getTelefono());
        }

        if (usuarioParcial.getCorreo() != null && !usuarioParcial.getCorreo().trim().isEmpty()) {
            System.out.println("   📧 Actualizando correo: " + usuarioExistente.getCorreo() + " -> " + usuarioParcial.getCorreo());

            // Validar que el correo no esté en uso por otro usuario
            if (!usuarioExistente.getCorreo().equals(usuarioParcial.getCorreo())) {
                validarCorreoUnico(usuarioParcial.getCorreo(), usuarioExistente.getIdUsuario());
            }
            usuarioExistente.setCorreo(usuarioParcial.getCorreo());
        }

        if (usuarioParcial.getNombreUsuario() != null && !usuarioParcial.getNombreUsuario().trim().isEmpty()) {
            System.out.println("   🔑 Actualizando nombreUsuario: " + usuarioExistente.getNombreUsuario() + " -> " + usuarioParcial.getNombreUsuario());

            // Validar que el nombre de usuario no esté en uso por otro usuario
            if (!usuarioExistente.getNombreUsuario().equals(usuarioParcial.getNombreUsuario())) {
                validarNombreUsuarioUnico(usuarioParcial.getNombreUsuario(), usuarioExistente.getIdUsuario());
            }
            usuarioExistente.setNombreUsuario(usuarioParcial.getNombreUsuario());
        }

    }

    /**
     * Método específico para actualizar solo la foto
     */
    public Usuario actualizarSoloFoto(Integer id, Integer idFoto) {
        System.out.println(" [UsuarioService] Actualizando solo foto para usuario ID: " + id + " con idFoto: " + idFoto);

        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));

        usuarioExistente.setIdFoto(idFoto);

        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
        System.out.println(" [UsuarioService] Foto actualizada para usuario: " + id);

        return usuarioActualizado;
    }

    /**
     * Valida que el correo sea único (excepto para el mismo usuario)
     */
    private void validarCorreoUnico(String correo, Integer usuarioId) {
        usuarioRepository.findByCorreo(correo)
                .ifPresent(usuarioExistente -> {
                    if (!usuarioExistente.getIdUsuario().equals(usuarioId)) {
                        throw new IllegalArgumentException("El correo electrónico ya está en uso por otro usuario");
                    }
                });
    }

    /**
     * Valida que el nombre de usuario sea único (excepto para el mismo usuario)
     */
    private void validarNombreUsuarioUnico(String nombreUsuario, Integer usuarioId) {
        usuarioRepository.findByNombreUsuario(nombreUsuario)
                .ifPresent(usuarioExistente -> {
                    if (!usuarioExistente.getIdUsuario().equals(usuarioId)) {
                        throw new IllegalArgumentException("El nombre de usuario ya está en uso por otro usuario");
                    }
                });
    }
}
