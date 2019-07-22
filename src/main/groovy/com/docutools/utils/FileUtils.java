package com.docutools.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    public static void deleteTempFile(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn(String.format("Could not delete temporary file <%s>!", path), e);
        }
    }

    public static boolean isCsvFile(String contentType, String name) {
        return "csv".equalsIgnoreCase(StringUtils.getFilenameExtension(name)) ||
            "text/csv".equalsIgnoreCase(contentType);
    }

    public static String readSecret(String chargebeeApiKeyPath) {
        try (BufferedReader br = new BufferedReader(new FileReader(chargebeeApiKeyPath))) {
            return br.readLine();
        } catch (IOException e) {
            log.error("Could not read from file providing chargebee api key");
        }
        return "";
    }
}
