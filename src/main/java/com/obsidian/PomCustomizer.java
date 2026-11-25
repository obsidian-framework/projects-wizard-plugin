package com.obsidian;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;

final class PomCustomizer {

    private PomCustomizer() {
    }

    static void apply(Path projectRoot, String oldPkg, String newPkg, String projectName) throws IOException {
        Path pomFile = projectRoot.resolve("pom.xml");
        if (!Files.isRegularFile(pomFile)) {
            return;
        }

        String artifactId = projectName.toLowerCase().replaceAll("[^a-z0-9_-]", "-");
        String pom = Files.readString(pomFile, StandardCharsets.UTF_8);

        // Project coordinates live before <dependencies>; don't touch dependency groupIds.
        int depsIndex = pom.indexOf("<dependencies>");
        String header = depsIndex == -1 ? pom : pom.substring(0, depsIndex);
        String rest = depsIndex == -1 ? "" : pom.substring(depsIndex);

        header = replaceFirstTag(header, "groupId", newPkg);
        header = replaceFirstTag(header, "artifactId", artifactId);

        pom = (header + rest).replace("<mainClass>" + oldPkg + ".", "<mainClass>" + newPkg + ".");
        Files.writeString(pomFile, pom, StandardCharsets.UTF_8);
    }

    private static String replaceFirstTag(String xml, String tag, String value) {
        String replacement = "<" + tag + ">" + value + "</" + tag + ">";
        return xml.replaceFirst("<" + tag + ">[^<]+</" + tag + ">", Matcher.quoteReplacement(replacement));
    }
}
