package com.zxzinn.novelai.utils;

import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

@Log4j2
public class TxtDatabaseConstructor extends DatabaseConstructor {

    @Override
    protected Set<String> extractTags(File file) throws IOException {
        Set<String> tags = new TreeSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tagArray = line.split(",");
                for (String tag : tagArray) {
                    String cleanedTag = cleanTag(tag);
                    if (!cleanedTag.isEmpty()) {
                        tags.add(cleanedTag);
                    }
                }
            }
        }

        return tags;
    }
}