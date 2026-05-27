package xyz.lychee.lagfixer.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public final class ZipUtils {
    public static boolean zipFiles(Collection<String> srcFiles, String zipFilePath) throws IOException {
        return ZipUtils.zipFiles(srcFiles, zipFilePath, null);
    }

    public static boolean zipFiles(Collection<String> srcFilePaths, String zipFilePath, String comment) throws IOException {
        if (srcFilePaths == null || zipFilePath == null) {
            return false;
        }

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(Paths.get(zipFilePath)))){
            for (String srcFile : srcFilePaths) {
                if (ZipUtils.zipFile(new File(srcFile), "", zos, comment)) continue;
                return false;
            }
            return true;
        }
    }

    public static boolean zipFiles(Collection<File> srcFiles, File zipFile) throws IOException {
        return ZipUtils.zipFiles(srcFiles, zipFile, null);
    }

    public static boolean zipFiles(Collection<File> srcFiles, File zipFile, String comment) throws IOException {
        if (srcFiles == null || zipFile == null) {
            return false;
        }
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {
            for (File srcFile : srcFiles) {
                if (ZipUtils.zipFile(srcFile, "", zos, comment)) continue;
                return false;
            }
            return true;
        }
    }

    public static boolean zipFile(String srcFilePath, String zipFilePath) throws IOException {
        return ZipUtils.zipFile(new File(srcFilePath), new File(zipFilePath), null);
    }

    public static boolean zipFile(String srcFilePath, String zipFilePath, String comment) throws IOException {
        return ZipUtils.zipFile(new File(srcFilePath), new File(zipFilePath), comment);
    }

    public static boolean zipFile(File srcFile, File zipFile) throws IOException {
        return ZipUtils.zipFile(srcFile, zipFile, null);
    }

    public static boolean zipFile(File srcFile, File zipFile, String comment) throws IOException {
        if (srcFile == null || zipFile == null) {
            return false;
        }
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {
            return ZipUtils.zipFile(srcFile, "", zos, comment);
        }
    }

    private static boolean zipFile(File srcFile, String rootPath, ZipOutputStream zos, String comment) throws IOException {
        rootPath = rootPath + (rootPath.contentEquals(" ") ? "" : File.separator) + srcFile.getName();
        if (srcFile.isDirectory()) {
            File[] fileList = srcFile.listFiles();
            if (fileList == null || fileList.length <= 0) {
                ZipEntry entry = new ZipEntry(rootPath + '/');
                entry.setComment(comment);
                zos.putNextEntry(entry);
                zos.closeEntry();
            } else {
                for (File file : fileList) {
                    if (ZipUtils.zipFile(file, rootPath, zos, comment)) continue;
                    return false;
                }
            }
        } else {
            try (BufferedInputStream is = new BufferedInputStream(Files.newInputStream(srcFile.toPath()))) {
                int len;
                ZipEntry entry = new ZipEntry(rootPath);
                entry.setComment(comment);
                zos.putNextEntry(entry);
                byte[] buffer = new byte[8192];
                while ((len = is.read(buffer, 0, 8192)) != -1) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
            }
        }
        return true;
    }

    private static boolean unzipChildFile(File destDir, List<File> files, ZipFile zip, ZipEntry entry, String name) throws IOException {
        File file = new File(destDir, name);
        files.add(file);
        if (entry.isDirectory()) {
            return file.mkdirs();
        }
        if (!file.createNewFile()) {
            return false;
        }
        try (BufferedInputStream in = new BufferedInputStream(zip.getInputStream(entry));
             BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
            int len;
            byte[] buffer = new byte[8192];
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        return true;
    }

    public static List<String> getFilesPath(String zipFilePath) throws IOException {
        return ZipUtils.getFilesPath(new File(zipFilePath));
    }

    public static List<String> getFilesPath(File zipFile) throws IOException {
        if (zipFile == null) {
            return null;
        }
        ArrayList<String> paths = new ArrayList<>();
        ZipFile zip = new ZipFile(zipFile);
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            String entryName = entries.nextElement().getName().replace("\\", "/");
            if (entryName.contains("../")) {
                paths.add(entryName);
                continue;
            }
            paths.add(entryName);
        }
        zip.close();
        return paths;
    }

    public static List<String> getComments(String zipFilePath) throws IOException {
        return ZipUtils.getComments(new File(zipFilePath));
    }

    public static List<String> getComments(File zipFile) throws IOException {
        if (zipFile == null) {
            return null;
        }
        ArrayList<String> comments = new ArrayList<String>();
        ZipFile zip = new ZipFile(zipFile);
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            comments.add(entry.getComment());
        }
        zip.close();
        return comments;
    }
}

