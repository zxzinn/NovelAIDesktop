package com.zxzinn.novelai.utils;

import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public abstract class DatabaseConstructor {

    protected static final String DATABASE_FOLDER = "database";
    protected static final String DATABASE_FILE = "tag_database.txt";

    public static void constructDatabase(List<File> files) throws IOException {
        Set<String> allTags = new TreeSet<>();
        List<File> unsupportedFiles = new ArrayList<>();

        for (File file : files) {
            try {
                log.info("Processing file: {}", file.getName());
                DatabaseConstructor constructor = getConstructor(file);
                Set<String> tags = constructor.extractTags(file);
                log.debug("Extracted {} tags from file: {}", tags.size(), file.getName());
                allTags.addAll(tags);
            } catch (UnsupportedOperationException e) {
                unsupportedFiles.add(file);
                log.warn("Skipping unsupported file: {}", file.getName());
            } catch (IOException e) {
                log.error("Error processing file: {}. Error: {}", file.getName(), e.getMessage());
            }
        }

        if (!allTags.isEmpty()) {
            log.info("Writing {} tags to database", allTags.size());
            writeToDatabase(allTags);
        } else {
            log.warn("No valid tags found in the selected files.");
        }

        if (!unsupportedFiles.isEmpty()) {
            String unsupportedFileNames = unsupportedFiles.stream()
                    .map(File::getName)
                    .collect(Collectors.joining(", "));
            log.warn("The following files were skipped due to unsupported format: {}", unsupportedFileNames);
        }
    }

    private static DatabaseConstructor getConstructor(File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".png")) {
            return new PngDatabaseConstructor();
        } else if (fileName.endsWith(".txt")) {
            return new TxtDatabaseConstructor();
        } else {
            throw new UnsupportedOperationException("Unsupported file type: " + fileName);
        }
    }

    protected abstract Set<String> extractTags(File file) throws IOException;

    protected static String cleanTag(String tag) {
        tag = tag.trim().replaceAll("^[\\[{(]|[]})]]$", "");
        tag = tag.replaceAll("[\\[\\]{}()]", "");
        tag = tag.replace("'", "");
        return tag.trim();
    }

    private static void writeToDatabase(Set<String> tags) throws IOException {
        File databaseDir = FileUtils.getOrCreateDirectory(DATABASE_FOLDER);
        File databaseFile = FileUtils.getFileInDirectory(databaseDir, DATABASE_FILE);
        Set<String> existingTags = new TreeSet<>();

        if (databaseFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(databaseFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    existingTags.add(line.trim());
                }
            }
            log.info("Loaded {} existing tags from database", existingTags.size());
        }

        tags.addAll(existingTags);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(databaseFile))) {
            for (String tag : tags) {
                writer.write(tag);
                writer.newLine();
            }
        }

        log.info("Database updated: {}. Total tags: {}", databaseFile.getAbsolutePath(), tags.size());
    }
}