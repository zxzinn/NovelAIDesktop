package com.zxzinn.novelai.utils;

import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
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
                clearLSB(file);
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

    private static void clearLSB(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("Unable to read image file: " + file.getName());
        }

        WritableRaster raster = image.getRaster();
        int[] pixels = new int[raster.getNumBands()];

        for (int y = 0; y < raster.getHeight(); y++) {
            for (int x = 0; x < raster.getWidth(); x++) {
                raster.getPixel(x, y, pixels);
                for (int i = 0; i < pixels.length; i++) {
                    pixels[i] = pixels[i] & 0xFE;
                }
                raster.setPixel(x, y, pixels);
            }
        }

        File cleanedDir = FileUtils.getOrCreateDirectory("cleaned");
        File outputFile = FileUtils.getFileInDirectory(cleanedDir, file.getName());
        ImageIO.write(image, "png", outputFile);

        log.info("Cleared LSB data for file: {}", file.getName());
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