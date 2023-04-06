/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.maven.project.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.RunnerBundle;
import org.jetbrains.idea.maven.model.MavenExplicitProfiles;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.EclipseTychoSettings;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenDataKeys;
import org.jetbrains.idea.maven.utils.MavenUtil;
import org.jetbrains.idea.maven.utils.actions.MavenAction;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import java.util.List;
import java.util.Set;

public class RunBuildAction extends MavenAction {
  @Override
  protected boolean isAvailable(@NotNull AnActionEvent e) {
    return super.isAvailable(e) && checkOrPerform(e.getDataContext(), false);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    checkOrPerform(e.getDataContext(), true);
  }

  private static boolean checkOrPerform(DataContext context, boolean perform) {
    final List<String> goals = MavenDataKeys.MAVEN_GOALS.getData(context);
    if (goals == null || goals.isEmpty()) return false;

    final Project project = MavenActionUtil.getProject(context);
    if (project == null) return false;
    final MavenProject mavenProject = MavenActionUtil.getMavenProject(context);
    if (mavenProject == null) return false;

    if (!perform) return true;

    final MavenProjectsManager projectsManager = MavenActionUtil.getProjectsManager(context);
    if (projectsManager == null) return false;

    // noinspection deprecation
    if (!ExternalSystemUtil.confirmLoadingUntrustedProject(project, MavenUtil.SYSTEM_ID)) {
      MavenUtil.showError(
        project,
        RunnerBundle.message("notification.title.failed.to.execute.maven.goal"),
        RunnerBundle.message("notification.project.is.untrusted")
      );

      return true;
    }

    final MavenExplicitProfiles explicitProfiles = projectsManager.getExplicitProfiles();
    final MavenRunnerParameters params = new MavenRunnerParameters(
      true,
      mavenProject.getDirectory(),
      mavenProject.getFile().getName(),
      goals,
      explicitProfiles.getEnabledProfiles(),
      explicitProfiles.getDisabledProfiles()
    );

    final String name = MavenRunConfigurationType.generateName(project, params);

    if (EclipseTychoSettings.getInstance(project).isSupportEnabled()) {
      final MavenProject rootProject = projectsManager.getProjectsTree().findRootProject(mavenProject);

      if (!mavenProject.equals(rootProject)) {
        params.setWorkingDirPath(rootProject.getDirectory());
        params.setPomFileName(rootProject.getFile().getName());

        final MavenId id = mavenProject.getMavenId();
        params.setProjectsList(Set.of(id.getGroupId() + ":" + id.getArtifactId()));
        params.setAlsoMake(true);
      }
    }

    final RunnerAndConfigurationSettings settings =
      MavenRunConfigurationType.createRunnerAndConfigurationSettings(null, null, params, project, name, false);
    final Executor executor = DefaultRunExecutor.getRunExecutorInstance();
    final ProgramRunner<?> runner = DefaultJavaProgramRunner.getInstance();
    final ExecutionEnvironment environment = new ExecutionEnvironment(executor, runner, settings, project);

    try {
      runner.execute(environment);
    }
    catch (final ExecutionException e) {
      MavenUtil.showError(project, RunnerBundle.message("notification.title.failed.to.execute.maven.goal"), e);
    }

    return true;
  }
}
