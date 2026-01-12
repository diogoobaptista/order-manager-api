package com.diogobaptista.order_manager_api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
public class FileLogService {

    private final Logger log = LoggerFactory.getLogger(FileLogService.class);
    private final Path logFile = Paths.get("orders.log");

    public void appendLine(String message) {
        try {
            if (!Files.exists(logFile)) {
                Files.createFile(logFile);
            }
            Files.write(logFile, (message + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("Failed to write to log file: {}", e.getMessage());
        }
    }
}