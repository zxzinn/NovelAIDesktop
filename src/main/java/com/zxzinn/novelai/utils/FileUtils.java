package com.zxzinn.novelai.utils;

import java.io.File;

public class FileUtils {
    public static File getOrCreateDirectory(String directoryName) {
        File currentDir = new File(System.getProperty("user.dir"));
        File targetDir = new File(currentDir, directoryName);
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }

    public static File getFileInDirectory(File directory, String fileName) {
        return new File(directory, fileName);
    }

    public static File getFileInCurrentDirectory(String fileName) {
        return new File(System.getProperty("user.dir"), fileName);
    }
}