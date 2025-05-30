package md.ctif.recipes_app.api;

import md.ctif.recipes_app.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/recipe")
public class ImageController {
    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/images/{filename}")
    public Mono<Resource> getImage(@PathVariable String filename) {
        return Mono.fromCallable(() -> fileStorageService.loadAsResource(filename))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(resource -> {
                    if (resource != null && resource.exists() && resource.isReadable()) {
                        return Mono.just(resource);
                    } else {
                        return Mono.error(new RuntimeException("File not found or not readable"));
                    }
                });
    }
    @GetMapping("/images/v2/{filename}")
    public Mono<ResponseEntity<byte[]>> getImageV2(@PathVariable String filename) {
        byte[] fileContent = fileStorageService.loadAsBytes(filename);

        return Mono.just(
                ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(fileContent)
        );
    }

    @PostMapping(path="/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> saveImage(
            @RequestPart("image") FilePart filePart
    ) {
        return fileStorageService.saveFile(filePart);
    }
}
