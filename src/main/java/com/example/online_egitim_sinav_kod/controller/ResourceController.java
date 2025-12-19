package com.example.online_egitim_sinav_kod.controller;

import com.example.online_egitim_sinav_kod.model.ResourceFile;
import com.example.online_egitim_sinav_kod.service.ResourceService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ResourceFile> upload(@RequestParam(required = false) Long courseId,
                                               @RequestParam("file") MultipartFile file,
                                               @RequestParam(defaultValue = "MATERIAL") ResourceFile.ResourceType type,
                                               Principal principal) throws Exception {
        ResourceFile rf = resourceService.storeResource(courseId, file, type, principal);
        return ResponseEntity.ok(rf);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMeta(@PathVariable Long id) {
        return resourceService.getResourceMeta(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> listByCourse(@PathVariable Long courseId, Principal principal) {
        return ResponseEntity.ok(resourceService.listCourseResources(courseId, principal));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws Exception {
        ResourceFile meta = resourceService.getResourceMeta(id).orElseThrow(() -> new IllegalArgumentException("Not found"));
        Resource res = resourceService.loadAsResource(meta.getPath());
        String filename = meta.getOriginalFilename() == null ? meta.getFilename() : meta.getOriginalFilename();
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType(meta.getContentType() == null ? "application/octet-stream" : meta.getContentType()))
                .body(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            resourceService.deleteResource(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Doküman silinirken hata oluştu: " + e.getMessage());
        }
    }
}

