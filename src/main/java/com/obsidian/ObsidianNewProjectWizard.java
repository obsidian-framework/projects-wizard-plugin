package com.obsidian;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ide.wizard.*;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.dsl.builder.AlignX;
import com.intellij.ui.dsl.builder.Panel;
import com.intellij.util.io.HttpRequests;
import kotlin.Unit;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ObsidianNewProjectWizard implements GeneratorNewProjectWizard {

    private static final Icon OBSIDIAN_ICON = IconLoader.getIcon("/icons/obsidianLogo.svg", ObsidianNewProjectWizard.class);
    private static final String GITHUB_BASE = "https://github." + "com/obsidian-framework/";

    private static final Map<String, String> TEMPLATES = new LinkedHashMap<>() {{
        put("Default App (Base Obsidian)", "obsidian");
        put("Flint (Starter Kit avec Auth)", "flint");
        put("LiveComponents Examples", "livecomponents-examples");
        put("Realtime Examples (SSE / Live feeds)", "realtime-examples");
    }};

    @Override
    public @NonNull String getId() {
        return "obsidian.project.wizard";
    }

    @Override
    public @NonNull String getName() {
        return "Obsidian";
    }

    @Override
    public @NonNull Icon getIcon() {
        return OBSIDIAN_ICON;
    }

    @Override
    public @NonNull NewProjectWizardStep createStep(@NonNull WizardContext wizardContext) {
        RootNewProjectWizardStep rootStep = new RootNewProjectWizardStep(wizardContext);
        NewProjectWizardBaseStep baseStep = new NewProjectWizardBaseStep(rootStep);

        return new AbstractNewProjectWizardStep(baseStep) {
            private final ComboBox<String> templateCombo = new ComboBox<>();

            {
                for (String key : TEMPLATES.keySet()) {
                    templateCombo.addItem(key);
                }
            }

            @Override
            public void setupUI(@NonNull Panel builder) {
                baseStep.setupUI(builder);
                builder.row("Template:", row -> {
                    row.cell(templateCombo).align(AlignX.FILL);
                    return Unit.INSTANCE;
                });
            }

            @Override
            public void setupProject(@NonNull Project project) {
                baseStep.setupProject(project);
                String selectedTemplate = (String) templateCombo.getSelectedItem();
                String repoName = TEMPLATES.get(selectedTemplate);
                String zipUrl = GITHUB_BASE + repoName + "/archive/HEAD.zip";

                String basePath = project.getBasePath();
                File targetDir = (basePath != null)
                        ? new File(basePath)
                        : new File(baseStep.getPath(), baseStep.getName());

                ProgressManager.getInstance().run(new Task.Modal(project, "Choose a template", true) {
                    @Override
                    public void run(@NonNull ProgressIndicator indicator) {
                        try {
                            indicator.setIndeterminate(true);
                            File tempZip = Files.createTempFile("obsidian-template-", ".zip").toFile();

                            HttpRequests.request(zipUrl)
                                    .redirectLimit(5)
                                    .saveToFile(tempZip, indicator);

                            unzipStrippingRootFolder(tempZip, targetDir);
                            tempZip.delete();

                            VirtualFile vFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(targetDir);
                            if (vFile != null) {
                                vFile.refresh(false, true);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            SwingUtilities.invokeLater(() -> Messages.showErrorDialog(
                                    "Erreur lors du téléchargement (" + zipUrl + ") : " + ex.getMessage(),
                                    "Erreur Obsidian"
                            ));
                        }
                    }
                });
            }
        };
    }

    private void unzipStrippingRootFolder(File zipFile, File destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                int firstSlash = name.indexOf('/');
                if (firstSlash == -1 || firstSlash == name.length() - 1) {
                    continue;
                }

                String relativePath = name.substring(firstSlash + 1);
                File outFile = new File(destDir, relativePath);

                if (!outFile.toPath().normalize().startsWith(destDir.toPath().normalize())) {
                    throw new IOException("Entrée ZIP invalide : " + name);
                }

                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    outFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        zis.transferTo(fos);
                    }
                }
                zis.closeEntry();
            }
        }
    }
}
