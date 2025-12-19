package com.example.online_egitim_sinav_kod.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

@Service
public class StorageServiceImpl implements StorageService {

    private final Path rootLocation;

    public StorageServiceImpl(@Value("${app.storage.location:./filestorage}") String storageLocation) {
        this.rootLocation = Paths.get(storageLocation).toAbsolutePath().normalize();
    }

    @Override
    public void init() throws IOException {
        Files.createDirectories(rootLocation);
    }

    @Override
    public String store(MultipartFile file, String subFolder) throws IOException {
        String original = file == null ? "file" : file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            original = "file";
        }

        // Dosya adını güvenli hale getir - StringUtils.cleanPath kullanmıyoruz
        original = sanitizeFilename(original);

        String ext = "";
        int i = original.lastIndexOf('.');
        if (i >= 0) ext = original.substring(i);
        String filename = Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + ext;
        Path targetDir = rootLocation;
        if (subFolder != null && !subFolder.isBlank()) {
            targetDir = rootLocation.resolve(subFolder);
            Files.createDirectories(targetDir);
        }
        Path destination = targetDir.resolve(filename);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        return rootLocation.relativize(destination).toString();
    }

    /**
     * Dosya adını güvenli hale getirir
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file";
        }

        try {
            // Türkçe karakterleri İngilizce karşılıklarıyla değiştir
            filename = filename
                    .replace("ğ", "g").replace("Ğ", "G")
                    .replace("ü", "u").replace("Ü", "U")
                    .replace("ş", "s").replace("Ş", "S")
                    .replace("ı", "i").replace("İ", "I")
                    .replace("ö", "o").replace("Ö", "O")
                    .replace("ç", "c").replace("Ç", "C");

            // Güvenli olmayan karakterleri tek tek kontrol ederek değiştir
            StringBuilder result = new StringBuilder();
            for (char c : filename.toCharArray()) {
                if (Character.isLetterOrDigit(c) || c == '.' || c == '_' || c == '-') {
                    result.append(c);
                } else {
                    result.append('_');
                }
            }
            filename = result.toString();

            // Birden fazla alt çizgiyi teke indir
            while (filename.contains("__")) {
                filename = filename.replace("__", "_");
            }

            // Başında ve sonunda alt çizgi varsa kaldır
            while (filename.startsWith("_")) {
                filename = filename.substring(1);
            }
            while (filename.endsWith("_")) {
                filename = filename.substring(0, filename.length() - 1);
            }

            // Eğer dosya adı boş kaldıysa varsayılan isim ver
            if (filename.isBlank()) {
                filename = "file";
            }

            return filename;
        } catch (Exception e) {
            // Herhangi bir hata olursa basit bir isim dön
            return "file_" + System.currentTimeMillis();
        }
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename).normalize();
    }

    @Override
    public Resource loadAsResource(String filename) throws IOException {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new IOException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new IOException("Malformed URL for file: " + filename, e);
        }
    }
}
