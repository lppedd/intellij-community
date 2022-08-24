// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.maven.project;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.utils.MavenProgressIndicator;

import java.util.Collection;

/**
 * @author Edoardo Luppi
 */
public class EclipseTychoResolverCustomizer implements MavenResolverCustomizer {
  private final EclipseTychoSettings settings;
  private final MavenProjectsManager manager;

  EclipseTychoResolverCustomizer(@NotNull final Project project) {
    settings = project.getService(EclipseTychoSettings.class);
    manager = project.getService(MavenProjectsManager.class);
  }

  @Override
  public boolean isEnabled() {
    return settings.isSupportEnabled();
  }

  @NotNull
  @Override
  public Collection<MavenProject> customizeProjects(
    @NotNull final Collection<MavenProject> projects,
    @NotNull final MavenProgressIndicator indicator
  ) {
    if (!settings.isSupportEnabled()) {
      return projects;
    }

    // TODO: filter out projects that don't require reloading
    return manager.getProjects();
  }
}
