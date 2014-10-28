package com.goide.jps;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.util.containers.MultiMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.impl.logging.ProjectBuilderLoggerBase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.intellij.util.ObjectUtils.assertNotNull;

public class TestProjectBuilderLogger extends ProjectBuilderLoggerBase {
  @NotNull private final MultiMap<String, File> myCompiledFiles = new MultiMap<String, File>();
  @NotNull private final Set<File> myDeletedFiles = new THashSet<File>(FileUtil.FILE_HASHING_STRATEGY);
  
  @Override
  public void logDeletedFiles(@NotNull Collection<String> paths) {
    for (String path : paths) {
      myDeletedFiles.add(new File(path));
    }
  }

  @Override
  public void logCompiledFiles(Collection<File> files, String builderName, String description) throws IOException {
    myCompiledFiles.putValues(builderName, files);
  }

  public void clear() {
    myCompiledFiles.clear();
    myDeletedFiles.clear();
  }
  
  public void assertCompiled(String builderName, @NotNull File[] baseDirs, String... paths) {
    assertRelativePaths(baseDirs, myCompiledFiles.get(builderName), paths);
  }

  public void assertDeleted(@NotNull File[] baseDirs, String... paths) {
    assertRelativePaths(baseDirs, myDeletedFiles, paths);
  }

  private static void assertRelativePaths(@NotNull File[] baseDirs, @NotNull Collection<File> files, String[] expected) {
    List<String> relativePaths = new ArrayList<String>();
    for (File file : files) {
      String path = file.getAbsolutePath();
      for (File baseDir : baseDirs) {
        if (baseDir != null && FileUtil.isAncestor(baseDir, file, false)) {
          path = FileUtil.getRelativePath(baseDir, file);
          break;
        }
      }
      path = assertNotNull(path);
      relativePaths.add(FileUtil.toSystemIndependentName(path));
    }
    UsefulTestCase.assertSameElements(relativePaths, expected);
  }

  @Override
  protected void logLine(String message) {
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
