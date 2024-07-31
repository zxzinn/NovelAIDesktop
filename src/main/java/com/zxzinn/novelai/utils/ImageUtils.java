package com.zxzinn.novelai.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipInputStream;

public class ImageUtils {
    public BufferedImage getImageFromZip(byte[] zipData) throws IOException {
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

    public File saveImage(BufferedImage image, File outputFile, boolean createTimestamp) throws IOException {
        File directory = outputFile.getParentFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }

        if (createTimestamp) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = outputFile.getName();
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            outputFile = new File(directory, baseName + "_" + timestamp + extension);
        }

        ImageIO.write(image, "png", outputFile);
        return outputFile;
    }

    public File saveImage(BufferedImage image, String outputDir) throws IOException {
        File directory = new File(outputDir.trim());
        File outputFile = new File(directory, "generated_image.png");
        return saveImage(image, outputFile, true);
    }
}