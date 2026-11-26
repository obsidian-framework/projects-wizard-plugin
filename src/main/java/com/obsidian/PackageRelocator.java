package com.obsidian;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Renames the root package of a freshly extracted template:
 * rewrites package/import lines, then moves the source directories.
 */
final class PackageRelocator
{
    private static final Pattern VALID_PACKAGE = Pattern.compile("[a-zA-Z_]\\w*(\\.[a-zA-Z_]\\w*)*");
    private static final Pattern PACKAGE_DECL = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;");
    private static final Pattern IMPORT_DECL = Pattern.compile("^\\s*import\\s+(?:static\\s+)?([\\w.]+?)(\\.\\*)?\\s*;");

    private PackageRelocator() {
    }

    static boolean isValidPackageName(String pkg) {
        return pkg != null && VALID_PACKAGE.matcher(pkg).matches();
    }

    /**
     * @return the old root package if something was relocated, empty otherwise
     */
    static Optional<String> relocate(Path projectRoot, String newPkg) throws IOException
    {
        Path src = projectRoot.resolve("src");
        Path mainJava = src.resolve("main/java");
        if (!Files.isDirectory(mainJava)) {
            return Optional.empty();
        }

        Optional<String> detected = detectRootPackage(mainJava);
        if (detected.isEmpty() || detected.get().equals(newPkg)) {
            return Optional.empty();
        }
        String oldPkg = detected.get();

        List<Path> javaFiles = listJavaFiles(src);
        LocalIndex index = buildIndex(javaFiles);
        for (Path file : javaFiles) {
            rewrite(file, oldPkg, newPkg, index);
        }

        moveRoot(mainJava, oldPkg, newPkg);
        moveRoot(src.resolve("test/java"), oldPkg, newPkg);
        return Optional.of(oldPkg);
    }

    private static Optional<String> detectRootPackage(Path javaRoot) throws IOException
    {
        String[] common = null;
        for (Path file : listJavaFiles(javaRoot)) {
            Optional<String> pkg = readPackage(file);
            if (pkg.isEmpty()) continue;
            String[] parts = pkg.get().split("\\.");
            common = (common == null) ? parts : commonPrefix(common, parts);
        }
        if (common == null || common.length == 0) {
            return Optional.empty();
        }
        return Optional.of(String.join(".", common));
    }

    private static String[] commonPrefix(String[] a, String[] b)
    {
        int max = Math.min(a.length, b.length);
        int i = 0;
        while (i < max && a[i].equals(b[i])) i++;
        return Arrays.copyOf(a, i);
    }

    private record LocalIndex(Set<String> packages, Set<String> types)
    {
        boolean contains(String importName, boolean wildcard) {
            if (wildcard && packages.contains(importName)) {
                return true;
            }
            // covers class imports, nested classes and static members
            for (String name = importName; name != null; name = parent(name))
            {
                if (types.contains(name)) return true;
            }
            return false;
        }
    }

    private static LocalIndex buildIndex(List<Path> javaFiles) throws IOException
    {
        Set<String> packages = new HashSet<>();
        Set<String> types = new HashSet<>();
        for (Path file : javaFiles) {
            Optional<String> pkg = readPackage(file);
            if (pkg.isEmpty()) continue;
            String fileName = file.getFileName().toString();
            packages.add(pkg.get());
            types.add(pkg.get() + "." + fileName.substring(0, fileName.length() - ".java".length()));
        }
        return new LocalIndex(packages, types);
    }

    private static Optional<String> readPackage(Path file) throws IOException
    {
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            Matcher m = PACKAGE_DECL.matcher(line);
            if (m.find()) return Optional.of(m.group(1));
        }
        return Optional.empty();
    }

    private static List<Path> listJavaFiles(Path root) throws IOException
    {
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    private static String parent(String name)
    {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? null : name.substring(0, dot);
    }

    private static void rewrite(Path file, String oldPkg, String newPkg, LocalIndex index) throws IOException
    {
        List<String> lines = new ArrayList<>(Files.readAllLines(file, StandardCharsets.UTF_8));
        boolean changed = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (needsRewrite(line, oldPkg, index)) {
                lines.set(i, line.replaceFirst(Pattern.quote(oldPkg), Matcher.quoteReplacement(newPkg)));
                changed = true;
            }
        }

        if (changed) {
            Files.write(file, lines, StandardCharsets.UTF_8);
        }
    }

    private static boolean needsRewrite(String line, String oldPkg, LocalIndex index)
    {
        Matcher pkg = PACKAGE_DECL.matcher(line);
        if (pkg.find()) {
            return isUnder(pkg.group(1), oldPkg);
        }
        Matcher imp = IMPORT_DECL.matcher(line);
        return imp.find()
                && isUnder(imp.group(1), oldPkg)
                && index.contains(imp.group(1), imp.group(2) != null);
    }

    private static boolean isUnder(String name, String root)
    {
        return name.equals(root) || name.startsWith(root + ".");
    }

    private static void moveRoot(Path sourceRoot, String oldPkg, String newPkg) throws IOException
    {
        Path oldDir = sourceRoot.resolve(oldPkg.replace('.', '/'));

        if (!Files.isDirectory(oldDir)) {  return; }

        Path newDir = sourceRoot.resolve(newPkg.replace('.', '/'));

        Path tmp = Files.createTempDirectory(sourceRoot, "relocate-");
        Path parked = tmp.resolve("pkg");
        Files.move(oldDir, parked);
        deleteEmptyParents(oldDir.getParent(), sourceRoot);

        Files.createDirectories(newDir.getParent());
        Files.move(parked, newDir);
        Files.delete(tmp);
    }

    private static void deleteEmptyParents(Path current, Path stopAt) throws IOException
    {
        while (current != null && !current.equals(stopAt) && current.startsWith(stopAt)) {
            try (Stream<Path> entries = Files.list(current)) {
                if (entries.findAny().isPresent()) {
                    return;
                }
            }
            Files.delete(current);
            current = current.getParent();
        }
    }
}
