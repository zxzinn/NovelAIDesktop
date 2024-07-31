package com.zxzinn.novelai.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

@Log4j2
public class MetadataReader {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Optional<String> extractMetadata(File file) {
        log.debug("Extracting metadata from file: {}", file.getAbsolutePath());
        StringBuilder metadata = new StringBuilder();

        try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(iis);
                    IIOMetadata imageMetadata = reader.getImageMetadata(0);
                    String[] names = imageMetadata.getMetadataFormatNames();
                    for (String name : names) {
                        Node root = imageMetadata.getAsTree(name);
                        processNode(root, "", metadata);
                    }
                } finally {
                    reader.dispose();
                }
            }
        } catch (IOException e) {
            log.warn("Unable to extract metadata from file: {}. Error: {}", file.getName(), e.getMessage());
            return Optional.empty();
        }

        return Optional.of(metadata.toString());
    }

    private static void processNode(Node node, String indent, StringBuilder metadata) {
        metadata.append(indent).append(node.getNodeName());
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attr = attributes.item(i);
                metadata.append(" ").append(attr.getNodeName()).append("=\"").append(attr.getNodeValue()).append("\"");
            }
        }
        metadata.append("\n");

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            processNode(children.item(i), indent + "  ", metadata);
        }
    }

    public static Optional<String> formatCommentJson(String metadata) {
        String commentLine = metadata.lines()
                .filter(line -> line.contains("keyword=\"Comment\""))
                .findFirst()
                .orElse(null);

        if (commentLine == null) {
            return Optional.empty();
        }

        try {
            int startIndex = commentLine.indexOf("value=\"") + 7;
            int endIndex = commentLine.lastIndexOf("\"");
            String jsonString = commentLine.substring(startIndex, endIndex);

            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return Optional.of(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
        } catch (IOException e) {
            log.warn("Unable to format comment JSON. Error: {}", e.getMessage());
            return Optional.empty();
        }
    }
}