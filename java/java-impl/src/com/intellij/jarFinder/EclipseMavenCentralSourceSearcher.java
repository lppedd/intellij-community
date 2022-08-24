// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.jarFinder;

import com.intellij.ide.IdeCoreBundle;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static com.intellij.jarFinder.MavenSourceSearcherUtils.findElements;
import static com.intellij.jarFinder.MavenSourceSearcherUtils.readElementCancelable;

/**
 * @author Edoardo Luppi
 */
@SuppressWarnings("MethodMayBeStatic")
public class EclipseMavenCentralSourceSearcher implements SourceSearcher {
  @Nullable
  @Override
  public String findSourceJar(
    @NotNull final ProgressIndicator indicator,
    @NotNull final String ignoredArtifactId,
    @NotNull final String ignoredVersion,
    @NotNull final VirtualFile classesJar) throws SourceSearchException {
    try {
      final var file = VfsUtilCore.virtualToIoFile(classesJar);
      final var artifactIdAndVersion = getArtifactIdAndVersion(file);

      if (artifactIdAndVersion == null) {
        return null;
      }

      final var artifactId = artifactIdAndVersion.getFirst();
      var version = artifactIdAndVersion.getSecond();

      indicator.setText(IdeCoreBundle.message("progress.message.connecting.to", "https://search.maven.org"));
      indicator.setText2(artifactId + ":" + version);
      indicator.checkCanceled();

      var url = getUrl(artifactId, version);
      var artifactList = findElements("./result/doc/str[@name='g']", readElementCancelable(indicator, url));

      if (artifactList.isEmpty()) {
        var alternativeVersion = getAlternativeVersion(version);

        if (alternativeVersion != null) {
          indicator.setText2(artifactId + ":" + alternativeVersion);
          url = getUrl(artifactId, alternativeVersion);
          artifactList = findElements("./result/doc/str[@name='g']", readElementCancelable(indicator, url));

          if (!artifactList.isEmpty()) {
            version = alternativeVersion;
          }
        }

        var newVersion = version;

        while (artifactList.isEmpty()) {
          newVersion = getIncrementedVersion(newVersion, -1);

          if (newVersion == null) {
            break;
          }

          indicator.checkCanceled();
          indicator.setText2(artifactId + ":" + newVersion);

          url = getUrl(artifactId, newVersion);
          artifactList = findElements("./result/doc/str[@name='g']", readElementCancelable(indicator, url));

          if (!artifactList.isEmpty()) {
            version = newVersion;
            break;
          }
        }
      }

      if (artifactList.size() == 1) {
        return "https://search.maven.org/remotecontent?filepath=" +
               artifactList.get(0).getValue().replace('.', '/') + '/' +
               artifactId + '/' +
               version + '/' +
               artifactId + '-' +
               version + "-sources.jar";
      }
      else {
        // TODO handle
        return null;
      }
    }
    catch (final IOException e) {
      indicator.checkCanceled();
      throw new SourceSearchException("Connection problem. See log for more details.");
    }
  }

  @NotNull
  private static String getUrl(@NotNull final String artifactId, @NotNull final String version) {
    final var groupId = "org.eclipse.platform";

    // noinspection StringBufferReplaceableByString
    final var sb = new StringBuilder(240);
    sb.append("https://search.maven.org/solrsearch/select?rows=3&wt=xml&q=");
    sb.append("g:%22");
    sb.append(groupId);
    sb.append("%22%20AND%20");
    sb.append("a:%22");
    sb.append(artifactId);
    sb.append("%22%20AND%20v:%22");
    sb.append(version);
    sb.append("%22%20AND%20l:%22sources%22");

    return sb.toString();
  }

  @Nullable
  private Pair<String, String> getArtifactIdAndVersion(final File file) throws IOException {
    try (final var jarFile = new JarFile(file)) {
      final var entries = jarFile.entries();

      while (entries.hasMoreElements()) {
        final var entry = entries.nextElement();

        if (entry.getName().equals("META-INF/MANIFEST.MF")) {
          final var manifest = new Manifest(jarFile.getInputStream(entry));
          final var attributes = manifest.getMainAttributes();
          final var symbolicName = normalizeSymbolicName(attributes.getValue("Bundle-SymbolicName"));
          final var version = normalizeVersion(attributes.getValue("Bundle-Version"));
          return Pair.create(symbolicName, version);
        }
      }
    }

    return null;
  }

  @NotNull
  private String normalizeSymbolicName(@NotNull final String value) {
    final var separatorIndex = value.indexOf(';');
    return separatorIndex > -1
           ? value.substring(0, separatorIndex)
           : value;
  }

  @NotNull
  private String normalizeVersion(@NotNull final String value) {
    final var lastDotIndex = value.lastIndexOf('.');
    return value.substring(0, lastDotIndex);
  }

  @Nullable
  private String getAlternativeVersion(@NotNull final String version) {
    final var lastDotIndex = version.lastIndexOf('.');

    if (lastDotIndex < 0) {
      return null;
    }

    final var patchVersion = version.substring(lastDotIndex + 1);
    final var newPatchVersion = String.format("%-3s", patchVersion).replace(' ', '0');
    return version.substring(0, lastDotIndex + 1) + newPatchVersion;
  }

  @Nullable
  @SuppressWarnings("SameParameterValue")
  private String getIncrementedVersion(@NotNull final String version, final int increment) {
    final var lastDotIndex = version.lastIndexOf('.');

    if (lastDotIndex < 0) {
      return null;
    }

    final var patchVersion = Integer.parseInt(version.substring(lastDotIndex + 1));
    final var newPatchVersion = patchVersion + increment;
    return newPatchVersion < 0
           ? null
           : version.substring(0, lastDotIndex + 1) + newPatchVersion;
  }
}
