package md.ctif.recipes_app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class FileStorageService {
    private final Path rootLocation;

    public FileStorageService(@Value("${storage}") String storage) {
        this.rootLocation = Paths.get(storage);
    }

    public Mono<String> saveFile(FilePart filePart) {
        String originalFilename = filePart.filename();
        int dotIndex = originalFilename.lastIndexOf(".");

        String baseName = (dotIndex != -1) ? originalFilename.substring(0, dotIndex) : originalFilename;
        String extension = (dotIndex != -1) ? originalFilename.substring(dotIndex) : "";

        if (!extension.matches("\\.(jpg|jpeg|png|gif)")) {
            return Mono.error(new IllegalArgumentException("Invalid file type: " + extension));
        }

        String newFileName = "img_" + baseName + "_" + System.currentTimeMillis() + extension;
        Path path = rootLocation.resolve(newFileName);

        return filePart.transferTo(path)
                .then(Mono.just(newFileName));
    }

    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        } catch (IOException e) {
            System.out.println("Failed to read stored files: " + e);
        }
        return Stream.empty();
    }

    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                System.out.println("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            System.out.println("Could not read file: " + filename + e);
        }
        return null;
    }


    public byte[] loadAsBytes(String filename) {
        try {
            Path file = load(filename);
            return Files.readAllBytes(load(filename));
        } catch (MalformedURLException e) {
            System.out.println("Could not read file: " + filename + e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean delete(String filename) {
        try {
            Path filePath = load(filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.out.println("Could not delete file: " + filename + " - " + e.getMessage());
            return false;
        }
    }

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }
}
