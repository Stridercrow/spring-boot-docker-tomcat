package com.iamvickyav.dockerdemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LibreOfficeRtfToHtmlService {

    private final String sofficePath;

    public LibreOfficeRtfToHtmlService(
            @Value("${libreoffice.path:soffice}") String sofficePath) {
        this.sofficePath = sofficePath;
    }

    /**
     * Converts the RTF from the given InputStream to HTML using LibreOffice.
     */
    public String convertRtfToHtml(InputStream rtfStream) {
        Path tempDir = null;
        Path rtfFile = null;
        Path htmlFile = null;

        try {
            tempDir = Files.createTempDirectory("rtf-convert-");
            String baseName = UUID.randomUUID().toString();
            rtfFile = tempDir.resolve(baseName + ".rtf");
            htmlFile = tempDir.resolve(baseName + ".html");

            // Save RTF to temp file
            Files.copy(rtfStream, rtfFile, StandardCopyOption.REPLACE_EXISTING);

            // Build LibreOffice command
            // Example:
            // soffice --headless --convert-to html --outdir /tmp rtfFile
            ProcessBuilder pb = new ProcessBuilder(
                    sofficePath,
                    "--headless",
                    "--convert-to",
                    "html:HTML:EmbedImages",
                    "--outdir",
                    tempDir.toAbsolutePath().toString(),
                    rtfFile.toAbsolutePath().toString()
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Wait for conversion to finish (with timeout)
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("LibreOffice conversion timed out");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new RuntimeException("LibreOffice conversion failed with exit code " + exitCode);
            }

            if (!Files.exists(htmlFile)) {
                // LibreOffice sometimes names output differently; try to find any .html
                try (var stream = Files.list(tempDir)) {
                    htmlFile = stream
                            .filter(p -> p.toString().toLowerCase().endsWith(".html"))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("No HTML file produced by LibreOffice"));
                }
            }

            // Read the HTML content
            return Files.readString(htmlFile);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to convert RTF to HTML using LibreOffice", e);
        } finally {
            // Cleanup temp files
            if (htmlFile != null) {
                try { Files.deleteIfExists(htmlFile); } catch (IOException ignored) {}
            }
            if (rtfFile != null) {
                try { Files.deleteIfExists(rtfFile); } catch (IOException ignored) {}
            }
            if (tempDir != null) {
                try { Files.deleteIfExists(tempDir); } catch (IOException ignored) {}
            }
        }
    }
}

