package com.Flyway.server.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller for serving static files in development mode
 * In production, files are served directly from S3
 */
@RestController
@RequestMapping("/files")
@Profile("dev")
public class FileController {
    
    private final Path storageLocation = Paths.get("./storage").toAbsolutePath().normalize();
    
    @GetMapping("/**")
    public ResponseEntity<Resource> serveFile() throws Exception {
        String filePath = Paths.get("").toAbsolutePath().toString();
        Path file = storageLocation.resolve(filePath.replace("/files/", "")).normalize();
        Resource resource = new UrlResource(file.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

