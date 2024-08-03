package com.zxzinn.novelai.utils;

import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipInputStream;

@Log4j2
public class ImageProcessor {

    public BufferedImage extractImageFromZip(byte[] zipData) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            while (zis.getNextEntry() != null) {
                BufferedImage image = ImageIO.read(zis);
                if (image != null) {
                    return image;
                }
            }
        }
        throw new IOException("No image found in the zip data");
    }

    public File saveImage(BufferedImage image, String outputDir) throws IOException {
        File directory = new File(outputDir.trim());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File outputFile = new File(directory, "generated_image_" + timestamp + ".png");

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create output directory: " + directory.getAbsolutePath());
        }

        if (!ImageIO.write(image, "png", outputFile)) {
            throw new IOException("Failed to save image: No appropriate writer found");
        }

        log.info("Image saved to: {}", outputFile.getAbsolutePath());
        return outputFile;
    }
}