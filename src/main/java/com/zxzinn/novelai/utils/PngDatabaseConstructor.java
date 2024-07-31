package com.zxzinn.novelai.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@Log4j2
public class PngDatabaseConstructor extends DatabaseConstructor {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected Set<String> extractTags(File file) throws IOException {
        Set<String> tags = new TreeSet<>();
        Optional<String> metadataOpt = MetadataReader.extractMetadata(file);

        if (metadataOpt.isPresent()) {
            String metadata = metadataOpt.get();
            Optional<String> commentJsonOpt = MetadataReader.formatCommentJson(metadata);

            if (commentJsonOpt.isPresent()) {
                String commentJson = commentJsonOpt.get();
                try {
                    JsonNode jsonNode = objectMapper.readTree(commentJson);

                    if (jsonNode.has("prompt")) {
                        String prompt = jsonNode.get("prompt").asText();
                        String[] tagArray = prompt.split(",");
                        for (String tag : tagArray) {
                            String cleanedTag = cleanTag(tag);
                            if (!cleanedTag.isEmpty()) {
                                tags.add(cleanedTag);
                            }
                        }
                    } else {
                        log.warn("No 'prompt' field found in JSON for file: {}", file.getName());
                    }
                } catch (IOException e) {
                    log.error("Error parsing JSON for file: {}. Error: {}", file.getName(), e.getMessage());
                }
            } else {
                log.warn("No comment JSON found for file: {}", file.getName());
            }
        } else {
            log.warn("No metadata found for file: {}", file.getName());
        }

        return tags;
    }
}