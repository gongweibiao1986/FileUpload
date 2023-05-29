package com.example.demo.config;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.net.URI;

@org.springframework.context.annotation.Configuration
public class HadoopConfig {

    @Value("${hadoop.hdfs.uri}")
    private String nameNodeUrl;

    @Bean
    public Configuration getConfiguration() {
        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", nameNodeUrl);
        return configuration;
    }

    @Bean
    public FileSystem getFileSystem() throws IOException {
        return FileSystem.get(URI.create(nameNodeUrl), getConfiguration());
    }
}
