package com.zxzinn.novelai.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class EnviromentUtils {
    public static final String CURRENT_WORKING_DIRECTORY = System.getProperty("user.dir");

    public static File create(String path) throws IOException {
        Files.createDirectories(Paths.get(CURRENT_WORKING_DIRECTORY + path));
        return new File(CURRENT_WORKING_DIRECTORY, path);
    }
}
