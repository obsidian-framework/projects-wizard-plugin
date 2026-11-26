package com.obsidian;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.io.HttpRequests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

final class TemplateDownloader
{
    private TemplateDownloader() { }

    static void download(Template template, Path targetDir, ProgressIndicator indicator) throws IOException
    {
        Path tempZip = Files.createTempFile("obsidian-template-", ".zip");
        try {
            indicator.setText("Downloading template...");
            HttpRequests.request(template.zipUrl())
                    .redirectLimit(5)
                    .saveToFile(tempZip.toFile(), indicator);

            indicator.setText("Extracting files...");
            unzipStrippingRootFolder(tempZip, targetDir);
        } finally {
            Files.deleteIfExists(tempZip);
        }
    }

    private static void unzipStrippingRootFolder(Path zipFile, Path destDir) throws IOException
    {
        Path root = destDir.toAbsolutePath().normalize();

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                int firstSlash = name.indexOf('/');
                if (firstSlash == -1 || firstSlash == name.length() - 1) {
                    continue;
                }

                Path out = root.resolve(name.substring(firstSlash + 1)).normalize();
                if (!out.startsWith(root)) {
                    throw new IOException("Invalid ZIP entry: " + name);
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(out);
                } else {
                    Files.createDirectories(out.getParent());
                    Files.copy(zis, out, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
