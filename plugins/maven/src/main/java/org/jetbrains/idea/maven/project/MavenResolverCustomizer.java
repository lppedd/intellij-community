// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.maven.project;

import com.intellij.openapi.extensions.ProjectExtensionPointName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.utils.MavenProgressIndicator;

import java.util.Collection;

/**
 * @author Edoardo Luppi
 */
public interface MavenResolverCustomizer {
  ProjectExtensionPointName<MavenResolverCustomizer> EP_NAME =
    new ProjectExtensionPointName<>("org.jetbrains.idea.maven.resolverCustomizer");

  boolean isEnabled();

  @NotNull
  Collection<MavenProject> customizeProjects(
    @NotNull final Collection<MavenProject> projects,
    @NotNull final MavenProgressIndicator indicator);
}
