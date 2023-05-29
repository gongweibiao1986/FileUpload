package com.example.demo.controller;

import com.example.demo.entity.FileUploadEntity;
import com.example.demo.service.FileUploadService;
import org.apache.tomcat.jni.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@RestController
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("files") MultipartFile[] files) throws URISyntaxException {
        try {
            fileUploadService.storeFile(files);
            return ResponseEntity.ok("文件上传成功");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("文件上传失败");
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long id) {
        return fileUploadService.downloadFile(id);
    }

    @GetMapping("/preview/{id}")
    public ResponseEntity<InputStreamResource> previewFile(@PathVariable Long id) {
        return fileUploadService.previewFile(id);
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileUploadEntity>> getFiles() {
        return fileUploadService.getFiles();
    }

}
