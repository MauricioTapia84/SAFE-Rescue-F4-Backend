package com.SAFE_Rescue.API_Incidentes.modelo;

/**
 * Interfaz genérica que define un contrato para los DTOs que poseen un identificador Integer.
 * Útil para operaciones genéricas en la capa de servicio o controladores.
 */
public interface IHasId {
    /**
     * Retorna el identificador principal del objeto.
     * @return El ID como Integer.
     */
    Integer getId();
}