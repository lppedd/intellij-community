// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.maven.utils;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UI;

import java.awt.*;

/**
 * @author Edoardo Luppi
 */
public class EclipseTychoSettingsPanel extends JBPanel<EclipseTychoSettingsPanel> {
  @SuppressWarnings("DialogTitleCapitalization")
  private final JBCheckBox supportEnabled = new JBCheckBox("Enable Eclipse Tycho support");

  public EclipseTychoSettingsPanel() {
    super(new BorderLayout(0, JBUI.scale(5)));
    initComponents();
  }

  public boolean isSupportEnabled() {
    return supportEnabled.isSelected();
  }

  public void setSupportEnabled(final boolean isSupportEnabled) {
    supportEnabled.setSelected(isSupportEnabled);
  }

  private void initComponents() {
    final var supportEnabledPanel = UI.PanelFactory.panel(supportEnabled)
      .withComment("Maven projects importing follows Eclipse Tycho rules")
      .createPanel();

    add(supportEnabledPanel, BorderLayout.PAGE_START);
  }
}
