package com.zxzinn.novelai.utils;

import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Log4j2
public class MetadataCleaner {

    public static List<File> clearMetadataForFiles(List<File> files) {
        List<File> unprocessedFiles = new ArrayList<>();
        for (File file : files) {
            try {
                clearMetadataForFile(file);
            } catch (IOException e) {
                log.warn("Unable to clear metadata for file: {}. Error: {}", file.getName(), e.getMessage());
                unprocessedFiles.add(file);
            }
        }
        return unprocessedFiles;
    }

    private static void clearMetadataForFile(File file) throws IOException {
        File cleanedDir = FileUtils.getOrCreateDirectory("cleaned");
        File outputFile = FileUtils.getFileInDirectory(cleanedDir, file.getName());

        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("Unable to read image file: " + file.getName());
        }

        String formatName = getImageFormatName(file);
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
        if (!writers.hasNext()) {
            throw new IOException("No ImageWriter found for format: " + formatName);
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
            writer.setOutput(ios);
            writer.write(image);
        } finally {
            writer.dispose();
        }

        log.info("Cleared metadata for file: {}", file.getName());
    }

    private static String getImageFormatName(File file) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                return reader.getFormatName();
            }
        }
        throw new IOException("Unable to determine image format for file: " + file.getName());
    }
}