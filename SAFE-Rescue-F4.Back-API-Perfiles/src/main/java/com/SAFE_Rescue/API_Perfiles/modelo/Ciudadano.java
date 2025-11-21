package com.SAFE_Rescue.API_Perfiles.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Entidad Ciudadano que hereda de Usuario.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@PrimaryKeyJoinColumn(name = "id_ciudadano")
public class Ciudadano extends Usuario {

    /**
     * ID de la Dirección, referenciando la entidad Direccion en API_Geolocalizacion.
     * Es una clave foránea lógica.
     */
    @Column(name = "id_direccion", nullable = false)
    @NotNull(message = "El ID de la dirección es obligatorio")
    @Schema(description = "ID de la Dirección (clave foránea a la API externa de Geolocalización)", required = true, example = "100")
    private Integer idDireccion;

    /**
     * Método para asignar manualmente los datos del usuario al ciudadano.
     * Esto evita tener que cambiar la estructura de herencia.
     */
    public void setUsuarioData(String run, String dv, String nombre, String aPaterno,
                               String aMaterno, String telefono, String correo,
                               String contrasenia, TipoUsuario tipoUsuario,
                               LocalDateTime fechaRegistro, Integer idEstado) {
        this.setRun(run);
        this.setDv(dv);
        this.setNombre(nombre);
        this.setAPaterno(aPaterno);
        this.setAMaterno(aMaterno);
        this.setTelefono(telefono);
        this.setCorreo(correo);
        this.setContrasenia(contrasenia);
        this.setTipoUsuario(tipoUsuario);
        this.setFechaRegistro(fechaRegistro);
        this.setIdEstado(idEstado);
        this.setIntentosFallidos(0); // Siempre 0 para nuevos usuarios
        this.setIdFoto(null); // Sin foto inicial
    }

    /**
     * Método sobrecargado para usar valores por defecto comunes
     */
    public void setUsuarioData(String run, String dv, String nombre, String aPaterno,
                               String aMaterno, String telefono, String correo,
                               String contrasenia, TipoUsuario tipoUsuario) {
        setUsuarioData(run, dv, nombre, aPaterno, aMaterno, telefono, correo,
                contrasenia, tipoUsuario, LocalDateTime.now(), 1); // 1 = Activo
    }
}