package com.example.demo.service;

import com.example.demo.entity.FileUploadEntity;
import com.example.demo.repository.FileUploadRepository;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.tomcat.jni.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileUploadService {
    @Autowired
    private FileSystem fileSystem;
    @Autowired
    private FileUploadRepository fileUploadRepository;
    @Value("${hadoop.hdfs.uri}")
    private String HDFS_URI;

    private void saveFileToHDFS(MultipartFile file) throws IOException, URISyntaxException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }
        Configuration configuration = new Configuration();
        String fileName = URLDecoder.decode(file.getOriginalFilename(), StandardCharsets.UTF_8.toString());
        FileSystem hdfs = FileSystem.get(new URI(HDFS_URI), configuration);
        String fileNameWithoutExtension = URLDecoder.decode(fileName.substring(0, fileName.lastIndexOf(".")), StandardCharsets.UTF_8.toString());
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

        FileUploadEntity fileUploadEntity = new FileUploadEntity();
        fileUploadEntity.setFileName(fileName);
        fileUploadEntity.setFileExtension(fileExtension);
        fileUploadEntity.setUploadTime(LocalDateTime.now());


        FileUploadEntity save = fileUploadRepository.save(fileUploadEntity);

        Long fileId = save.getId();
        String hdfsFileName = fileId + "_" + fileNameWithoutExtension + "." + fileExtension;
        String hdfsFilePath = "/uploaded_files/" + hdfsFileName;
        save.setHdfsPath(hdfsFilePath);
        fileUploadRepository.save(save);

        Path filePath = new Path(HDFS_URI + hdfsFilePath);


        FSDataOutputStream outputStream = hdfs.create(filePath);
        InputStream inputStream = file.getInputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();
    }

    public void storeFile(MultipartFile[] files) throws IOException, URISyntaxException {
        List<FileUploadEntity> entities = new ArrayList<>();
        for (MultipartFile file : files) {
            saveFileToHDFS(file);
        }

    }

    public ResponseEntity<ByteArrayResource> downloadFile(Long id) {
        FileUploadEntity fileInfo = fileUploadRepository.findById(id).orElse(null);
        if (fileInfo == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path path = new Path(fileInfo.getHdfsPath());
            InputStream inputStream = fileSystem.open(path);
            InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
            ByteArrayResource resource = new ByteArrayResource(IOUtils.toByteArray(inputStream));



            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="  + URLEncoder.encode(fileInfo.getFileName(), "UTF-8"));
            headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(resource.contentLength()));

            return ResponseEntity.ok()
                    .headers(headers)
//                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<InputStreamResource> previewFile(Long id) {
        FileUploadEntity fileInfo = fileUploadRepository.findById(id).orElse(null);
        if (fileInfo == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path path = new Path(fileInfo.getHdfsPath());
            InputStream inputStream = fileSystem.open(path);
            InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(inputStreamResource);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<List<FileUploadEntity>> getFiles() {
        List<FileUploadEntity> fileInfo = fileUploadRepository.findAll();
        return ResponseEntity.ok().body(fileInfo);
    }
}
