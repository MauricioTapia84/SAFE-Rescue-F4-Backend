package com.SAFE_Rescue.API_Perfiles.modelo;

/**
 * Interfaz de marcador para asegurar que todos los DTOs externos
 * utilizados en el DataLoader tienen un método getId() para su uso como FK Lógica.
 */
public interface IHasId {
    Integer getId();
}