package com.example.online_egitim_sinav_kod.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface StorageService {
    void init() throws IOException;
    String store(MultipartFile file, String subFolder) throws IOException;
    Path load(String filename);
    Resource loadAsResource(String filename) throws IOException;
}

