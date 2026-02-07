package com.example.yoyo_data.util;

import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtils {

    private static final int BUFFER_SIZE = 8192;

    public static boolean exists(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        return Files.exists(Paths.get(path));
    }

    public static boolean isFile(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        return Files.isRegularFile(Paths.get(path));
    }

    public static boolean isDirectory(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        return Files.isDirectory(Paths.get(path));
    }

    public static long getSize(String path) {
        if (!exists(path)) {
            return 0;
        }
        try {
            return Files.size(Paths.get(path));
        } catch (IOException e) {
            return 0;
        }
    }

    public static String getExtension(String filename) {
        if (StringUtils.isEmpty(filename)) {
            return "";
        }
        int index = filename.lastIndexOf('.');
        if (index == -1 || index == filename.length() - 1) {
            return "";
        }
        return filename.substring(index + 1).toLowerCase();
    }

    public static String getFileName(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        return Paths.get(path).getFileName().toString();
    }

    public static String getFileNameWithoutExtension(String filename) {
        if (StringUtils.isEmpty(filename)) {
            return "";
        }
        String name = getFileName(filename);
        int index = name.lastIndexOf('.');
        if (index == -1) {
            return name;
        }
        return name.substring(0, index);
    }

    public static String getParentPath(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        Path parent = Paths.get(path).getParent();
        return parent == null ? "" : parent.toString();
    }

    public static boolean createFile(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        try {
            Path filePath = Paths.get(path);
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean createDirectory(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        try {
            Path dirPath = Paths.get(path);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean delete(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        try {
            return Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean deleteDirectory(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        try {
            Path dirPath = Paths.get(path);
            if (Files.exists(dirPath)) {
                Files.walk(dirPath)
                        .sorted((a, b) -> -a.compareTo(b))
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException ignored) {
                            }
                        });
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean copy(String source, String target) {
        if (StringUtils.isEmpty(source) || StringUtils.isEmpty(target)) {
            return false;
        }
        try {
            Files.copy(Paths.get(source), Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean move(String source, String target) {
        if (StringUtils.isEmpty(source) || StringUtils.isEmpty(target)) {
            return false;
        }
        try {
            Files.move(Paths.get(source), Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean rename(String source, String newName) {
        if (StringUtils.isEmpty(source) || StringUtils.isEmpty(newName)) {
            return false;
        }
        try {
            Path sourcePath = Paths.get(source);
            Path targetPath = sourcePath.resolveSibling(newName);
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String readToString(String path) {
        if (!exists(path)) {
            return null;
        }
        try {
            return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    public static List<String> readLines(String path) {
        if (!exists(path)) {
            return new ArrayList<>();
        }
        try {
            return Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static byte[] readBytes(String path) {
        if (!exists(path)) {
            return new byte[0];
        }
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public static boolean writeString(String path, String content) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        try {
            Files.write(Paths.get(path), content.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean writeLines(String path, List<String> lines) {
        if (StringUtils.isEmpty(path) || lines == null) {
            return false;
        }
        try {
            Files.write(Paths.get(path), lines, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean writeBytes(String path, byte[] bytes) {
        if (StringUtils.isEmpty(path) || bytes == null) {
            return false;
        }
        try {
            Files.write(Paths.get(path), bytes);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean appendString(String path, String content) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        try {
            Files.write(Paths.get(path), content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static List<String> listFiles(String directory) {
        if (!isDirectory(directory)) {
            return new ArrayList<>();
        }
        try (Stream<Path> stream = Files.list(Paths.get(directory))) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static List<String> listDirectories(String directory) {
        if (!isDirectory(directory)) {
            return new ArrayList<>();
        }
        try (Stream<Path> stream = Files.list(Paths.get(directory))) {
            return stream
                    .filter(Files::isDirectory)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static List<String> listAll(String directory) {
        if (!isDirectory(directory)) {
            return new ArrayList<>();
        }
        try (Stream<Path> stream = Files.walk(Paths.get(directory))) {
            return stream
                    .filter(p -> !p.equals(Paths.get(directory)))
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static String formatSize(long size) {
        if (size < 0) {
            return "0 B";
        }
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    public static boolean isImage(String filename) {
        String ext = getExtension(filename);
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || ext.equals("gif") || ext.equals("bmp") || ext.equals("webp");
    }

    public static boolean isVideo(String filename) {
        String ext = getExtension(filename);
        return ext.equals("mp4") || ext.equals("avi") || ext.equals("mov") || ext.equals("wmv") || ext.equals("flv") || ext.equals("mkv");
    }

    public static boolean isAudio(String filename) {
        String ext = getExtension(filename);
        return ext.equals("mp3") || ext.equals("wav") || ext.equals("flac") || ext.equals("aac") || ext.equals("ogg");
    }

    public static boolean isDocument(String filename) {
        String ext = getExtension(filename);
        return ext.equals("doc") || ext.equals("docx") || ext.equals("pdf") || ext.equals("txt") || ext.equals("xls") || ext.equals("xlsx") || ext.equals("ppt") || ext.equals("pptx");
    }

    public static boolean isArchive(String filename) {
        String ext = getExtension(filename);
        return ext.equals("zip") || ext.equals("rar") || ext.equals("7z") || ext.equals("tar") || ext.equals("gz");
    }

    public static String getMimeType(String filename) {
        String ext = getExtension(filename);
        switch (ext) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "pdf":
                return "application/pdf";
            case "txt":
                return "text/plain";
            case "html":
                return "text/html";
            case "json":
                return "application/json";
            case "xml":
                return "application/xml";
            case "zip":
                return "application/zip";
            case "mp4":
                return "video/mp4";
            case "mp3":
                return "audio/mpeg";
            default:
                return "application/octet-stream";
        }
    }

    public static boolean compress(String sourceDir, String zipFile) {
        if (!isDirectory(sourceDir)) {
            return false;
        }
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Path sourcePath = Paths.get(sourceDir);
            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString());
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean decompress(String zipFile, String targetDir) {
        if (!isFile(zipFile)) {
            return false;
        }
        createDirectory(targetDir);
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((entry = zis.getNextEntry()) != null) {
                Path filePath = Paths.get(targetDir, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String normalizePath(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        return Paths.get(path).normalize().toString();
    }

    public static boolean isAbsolutePath(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        return Paths.get(path).isAbsolute();
    }

    public static String joinPath(String... paths) {
        if (paths == null || paths.length == 0) {
            return "";
        }
        Path result = Paths.get(paths[0]);
        for (int i = 1; i < paths.length; i++) {
            result = result.resolve(paths[i]);
        }
        return result.toString();
    }

    public static String getRelativePath(String base, String path) {
        if (StringUtils.isEmpty(base) || StringUtils.isEmpty(path)) {
            return "";
        }
        try {
            return Paths.get(base).relativize(Paths.get(path)).toString();
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    public static String toUnixPath(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        return path.replace("\\", "/");
    }

    public static String toWindowsPath(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        return path.replace("/", "\\");
    }
}
