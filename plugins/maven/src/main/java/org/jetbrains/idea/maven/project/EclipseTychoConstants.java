// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.maven.project;

/**
 * Constants reserved for the Eclipse Tycho integration.
 */
public class EclipseTychoConstants {
  public static class Packaging {
    public static final String ECLIPSE_PLUGIN = "eclipse-plugin";
    public static final String ECLIPSE_TEST_PLUGIN = "eclipse-test-plugin";
    public static final String ECLIPSE_FEATURE = "eclipse-feature";
    public static final String ECLIPSE_REPOSITORY = "eclipse-repository";
    public static final String ECLIPSE_APPLICATION = "eclipse-application";
    public static final String ECLIPSE_UPDATE_SITE = "eclipse-update-site";
    public static final String ECLIPSE_TARGET_DEFINITION = "eclipse-target-definition";
    public static final String P2_INSTALLABLE_UNIT = "p2-installable-unit";
  }
}
