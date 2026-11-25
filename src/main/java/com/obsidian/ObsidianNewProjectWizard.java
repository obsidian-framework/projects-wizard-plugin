package com.obsidian;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ide.wizard.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.dsl.builder.AlignX;
import com.intellij.ui.dsl.builder.Panel;
import kotlin.Unit;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class ObsidianNewProjectWizard implements GeneratorNewProjectWizard {

    private static final Logger LOG = Logger.getInstance(ObsidianNewProjectWizard.class);
    private static final Icon OBSIDIAN_ICON = IconLoader.getIcon("/icons/obsidianLogo.svg", ObsidianNewProjectWizard.class);

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
            private final ComboBox<Template> templateCombo = new ComboBox<>(Template.values());
            private final JBTextField packageField = new JBTextField("com.example.app");

            @Override
            public void setupUI(@NonNull Panel builder) {
                baseStep.setupUI(builder);

                builder.row("Template:", row -> {
                    row.cell(templateCombo).align(AlignX.FILL);
                    return Unit.INSTANCE;
                });

                builder.row("Package:", row -> {
                    row.cell(packageField).align(AlignX.FILL);
                    return Unit.INSTANCE;
                });
            }

            @Override
            public void setupProject(@NonNull Project project) {
                baseStep.setupProject(project);

                Template template = (Template) templateCombo.getSelectedItem();
                String customPackage = packageField.getText().trim();
                String projectName = baseStep.getName();
                String basePath = project.getBasePath();
                Path targetDir = (basePath != null)
                        ? Path.of(basePath)
                        : Path.of(baseStep.getPath(), projectName);

                ProgressManager.getInstance().run(new Task.Modal(project, "Creating Obsidian project...", true) {
                    @Override
                    public void run(@NonNull ProgressIndicator indicator) {
                        try {
                            generate(template, targetDir, customPackage, projectName, indicator);
                        } catch (IOException ex) {
                            LOG.warn("Obsidian project creation failed", ex);
                            ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(
                                    "Project creation failed (" + template.zipUrl() + "): " + ex.getMessage(),
                                    "Obsidian Error"
                            ));
                        }
                    }
                });
            }
        };
    }

    private static void generate(Template template, Path targetDir, String pkg, String projectName,
                                 ProgressIndicator indicator) throws IOException {
        indicator.setIndeterminate(true);
        TemplateDownloader.download(template, targetDir, indicator);

        if (PackageRelocator.isValidPackageName(pkg)) {
            indicator.setText("Configuring package " + pkg + "...");
            Optional<String> oldPkg = PackageRelocator.relocate(targetDir, pkg);
            if (oldPkg.isPresent()) {
                PomCustomizer.apply(targetDir, oldPkg.get(), pkg, projectName);
            }
        }

        VirtualFile root = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(targetDir.toFile());
        if (root != null) {
            root.refresh(false, true);
        }
    }
}
