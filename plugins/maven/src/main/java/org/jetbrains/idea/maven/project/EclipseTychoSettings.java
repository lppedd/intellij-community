// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.maven.project;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Edoardo Luppi
 */
@State(name = "EclipseTycho")
public class EclipseTychoSettings implements PersistentStateComponent<EclipseTychoSettings> {
  private boolean isSupportEnabled;

  /**
   * Returns {@code true} if Eclipse Tycho support is enabled
   * for the project, {@code false} otherwise.
   */
  public boolean isSupportEnabled() {
    return isSupportEnabled;
  }

  /**
   * Enables or disables Eclipse Tycho support for the project.
   */
  public void setSupportEnabled(final boolean isSupportEnabled) {
    this.isSupportEnabled = isSupportEnabled;
  }

  @NotNull
  @Override
  public EclipseTychoSettings getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull final EclipseTychoSettings state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}
