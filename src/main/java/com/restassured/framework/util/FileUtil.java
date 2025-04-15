package com.restassured.framework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
    
    public static String readFile(String filePath) {
        try {
            return Files.readString(Paths.get(filePath));
        } catch (IOException e) {
            logger.error("Error reading file: {}", filePath, e);
            return null;
        }
    }
    
    public static void writeFile(String filePath, String content) {
        try {
            Files.writeString(Paths.get(filePath), content);
        } catch (IOException e) {
            logger.error("Error writing file: {}", filePath, e);
        }
    }
    
    public static void appendToFile(String filePath, String content) {
        try {
            Files.writeString(Paths.get(filePath), content, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.error("Error appending to file: {}", filePath, e);
        }
    }
    
    public static void createDirectory(String directoryPath) {
        try {
            Files.createDirectories(Paths.get(directoryPath));
        } catch (IOException e) {
            logger.error("Error creating directory: {}", directoryPath, e);
        }
    }
    
    public static void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            logger.error("Error deleting file: {}", filePath, e);
        }
    }
    
    public static void deleteDirectory(String directoryPath) {
        try {
            Files.walk(Paths.get(directoryPath))
                .sorted(java.util.Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        } catch (IOException e) {
            logger.error("Error deleting directory: {}", directoryPath, e);
        }
    }
    
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    public static boolean directoryExists(String directoryPath) {
        return Files.isDirectory(Paths.get(directoryPath));
    }
    
    public static String getFileExtension(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        return lastDotIndex > 0 ? filePath.substring(lastDotIndex + 1) : "";
    }
    
    public static String getFileNameWithoutExtension(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        int lastSeparatorIndex = filePath.lastIndexOf(File.separator);
        return filePath.substring(lastSeparatorIndex + 1, lastDotIndex > 0 ? lastDotIndex : filePath.length());
    }
    
    public static void copyFile(String sourcePath, String destinationPath) {
        try {
            Files.copy(Paths.get(sourcePath), Paths.get(destinationPath));
        } catch (IOException e) {
            logger.error("Error copying file from {} to {}", sourcePath, destinationPath, e);
        }
    }
    
    public static void moveFile(String sourcePath, String destinationPath) {
        try {
            Files.move(Paths.get(sourcePath), Paths.get(destinationPath));
        } catch (IOException e) {
            logger.error("Error moving file from {} to {}", sourcePath, destinationPath, e);
        }
    }
    
    public static String[] listFiles(String directoryPath) {
        try (Stream<Path> paths = Files.list(Paths.get(directoryPath))) {
            return paths
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .toArray(String[]::new);
        } catch (IOException e) {
            logger.error("Error listing files in directory: {}", directoryPath, e);
            return new String[0];
        }
    }
    
    public static String[] listDirectories(String directoryPath) {
        try (Stream<Path> paths = Files.list(Paths.get(directoryPath))) {
            return paths
                .filter(Files::isDirectory)
                .map(Path::toString)
                .toArray(String[]::new);
        } catch (IOException e) {
            logger.error("Error listing directories in: {}", directoryPath, e);
            return new String[0];
        }
    }
    
    public static long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            logger.error("Error getting file size: {}", filePath, e);
            return -1;
        }
    }
    
    public static String getFileContentType(String filePath) {
        try {
            return Files.probeContentType(Paths.get(filePath));
        } catch (IOException e) {
            logger.error("Error getting file content type: {}", filePath, e);
            return null;
        }
    }
} 