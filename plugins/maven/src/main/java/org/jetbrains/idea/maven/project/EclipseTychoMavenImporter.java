// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.maven.project;

import com.intellij.openapi.project.Project;
import org.jetbrains.idea.maven.importing.MavenImporter;

import java.util.Properties;

/**
 * @author Edoardo Luppi
 */
public class EclipseTychoMavenImporter extends MavenImporter {
  EclipseTychoMavenImporter() {
    super("", "");
  }

  @Override
  public boolean isApplicable(final MavenProject mavenProject) {
    return mavenProject.findPlugin("org.eclipse.tycho", "tycho-maven-plugin") != null;
  }

  @Override
  public void customizeUserProperties(
    final Project project,
    final MavenProject mavenProject,
    final Properties properties
  ) {
    // Tycho offers two ways to resolve dependencies:
    //  - eager: resolves everything before processing projects
    //  - lazy:  resolves before processing on each project
    // By default it now builds using lazy resolution, but that means nothing
    // is actually resolved when Maven lifecycle listeners run, so here
    // we are setting it back to eager
    properties.setProperty("tycho.target.eager", "true");
  }
}
