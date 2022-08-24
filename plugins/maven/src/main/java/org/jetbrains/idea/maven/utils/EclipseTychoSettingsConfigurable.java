// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.maven.utils;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.EclipseTychoConstants;
import org.jetbrains.idea.maven.project.EclipseTychoSettings;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import javax.swing.*;

/**
 * @author Edoardo Luppi
 */
public class EclipseTychoSettingsConfigurable implements SearchableConfigurable {
  private final EclipseTychoSettingsPanel ui = new EclipseTychoSettingsPanel();
  private final EclipseTychoSettings settings;
  private final MavenProjectsManager manager;

  EclipseTychoSettingsConfigurable(@NotNull final Project project) {
    settings = project.getService(EclipseTychoSettings.class);
    manager = project.getService(MavenProjectsManager.class);
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Eclipse Tycho";
  }

  @NotNull
  @Override
  public String getId() {
    return "com.lppedd.idea.tycho.EclipseTychoSettingsConfigurable";
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    ui.setSupportEnabled(settings.isSupportEnabled());
    return ui;
  }

  @Override
  public boolean isModified() {
    return settings.isSupportEnabled() != ui.isSupportEnabled();
  }

  @Override
  public void apply() throws ConfigurationException {
    final var isSupportEnabled = ui.isSupportEnabled();
    settings.setSupportEnabled(isSupportEnabled);

    if (isSupportEnabled) {
      final var importingSettings = manager.getImportingSettings();
      final var dependencyTypes = importingSettings.getDependencyTypesAsSet();
      dependencyTypes.add(EclipseTychoConstants.Packaging.ECLIPSE_PLUGIN);
      dependencyTypes.add(EclipseTychoConstants.Packaging.ECLIPSE_TEST_PLUGIN);
      importingSettings.setDependencyTypes(String.join(", ", dependencyTypes));
    }
  }

  @Override
  public void reset() {
    ui.setSupportEnabled(settings.isSupportEnabled());
  }
}
