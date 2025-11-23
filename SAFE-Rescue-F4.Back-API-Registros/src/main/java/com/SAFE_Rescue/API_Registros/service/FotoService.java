package com.SAFE_Rescue.API_Registros.service;

import com.SAFE_Rescue.API_Registros.modelo.Foto;
import com.SAFE_Rescue.API_Registros.repository.FotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FotoService {

    @Autowired
    private FotoRepository fotoRepository;

    //  Carpeta donde se guardarán las fotos
    private static final String UPLOAD_DIR = "uploads/fotos";

    /**
     * Guarda el archivo físico en el servidor
     */
    public String guardarArchivoFisico(byte[] fileBytes, String originalFilename) throws IOException {
        // Crear directorio si no existe
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            System.out.println(" Directorio creado: " + UPLOAD_DIR);
        }

        // Generar nombre único para el archivo
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String fileExtension = getFileExtension(originalFilename);
        String savedFilename = timestamp + "_" + uniqueId + "." + fileExtension;

        // Ruta completa del archivo
        Path filePath = Paths.get(UPLOAD_DIR, savedFilename);

        // Guardar archivo
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(fileBytes);
            fos.flush();
            System.out.println(" Archivo guardado en: " + filePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println(" Error al guardar archivo: " + e.getMessage());
            throw new IOException("Error al guardar el archivo", e);
        }

        // Retornar ruta relativa para almacenar en BD
        return "uploads/fotos/" + savedFilename;
    }

    /**
     * Extrae la extensión del archivo
     */
    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        }
        return "jpg";
    }

    /**
     * Guardar foto en la BD
     */
    public Foto save(Foto foto) {
        System.out.println(" [FotoService] Guardando foto...");
        System.out.println("   URL: " + foto.getUrl());
        System.out.println("   Tipo: " + foto.getTipo());
        System.out.println("   Tamaño: " + foto.getTamanio() + " bytes");

        try {
            Foto fotoGuardada = fotoRepository.save(foto);
            System.out.println(" [FotoService] Foto guardada con ID: " + fotoGuardada.getIdFoto());
            return fotoGuardada;
        } catch (Exception e) {
            System.err.println(" [FotoService] Error al guardar: " + e.getMessage());
            throw new RuntimeException("Error al guardar la foto", e);
        }
    }

    /**
     * Obtener foto por ID
     */
    public Foto findById(int id) {
        Optional<Foto> foto = fotoRepository.findById(id);
        return foto.orElseThrow(() -> new RuntimeException("Foto no encontrada"));
    }

    /**
     * Obtener todas las fotos
     */
    public List<Foto> findAll() {
        return fotoRepository.findAll();
    }

    /**
     * Actualizar foto
     */
    public void update(Foto foto, Integer id) {
        Optional<Foto> fotoExistente = fotoRepository.findById(id);
        if (fotoExistente.isPresent()) {
            Foto f = fotoExistente.get();
            f.setUrl(foto.getUrl());
            f.setTipo(foto.getTipo());
            f.setDescripcion(foto.getDescripcion());
            fotoRepository.save(f);
        } else {
            throw new RuntimeException("Foto no encontrada");
        }
    }

    /**
     * Eliminar foto
     */
    public void delete(Integer id) {
        Optional<Foto> foto = fotoRepository.findById(id);
        if (foto.isPresent()) {

            String filePath = foto.get().getUrl();
            File file = new File(filePath);
            if (file.exists() && file.delete()) {
                System.out.println(" Archivo eliminado: " + filePath);
            } else {
                System.out.println("⚠ No se pudo eliminar el archivo: " + filePath);
            }
            // Eliminar de BD
            fotoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Foto no encontrada");
        }
    }
}