package com.company.vzvod.view.shift;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Production bootJar: Shift.detail units remove")
class ShiftDetailViewBootJarTest {

  private static final String VIEW_ENTRY =
          "BOOT-INF/classes/com/company/vzvod/view/shift/shift-detail-view.xml";

  @Test
  @DisplayName("в собранном bootJar units remove — list_exclude")
  @EnabledIf("bootJarExists")
  void bootJar_unitsRemove_isListExclude() throws IOException {
    Path jarPath = Path.of("build", "libs").resolve(findBootJarName());
    try (JarFile jar = new JarFile(jarFile(jarPath))) {
      ZipEntry entry = jar.getEntry(VIEW_ENTRY);
      assertTrue(entry != null, () -> VIEW_ENTRY + " missing in " + jarPath);

      String xml;
      try (InputStream is = jar.getInputStream(entry)) {
        xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      }

      int unitsGridPos = xml.indexOf("id=\"unitsDataGrid\"");
      assertTrue(unitsGridPos >= 0);

      int unitsActionsPos = xml.indexOf("<actions>", unitsGridPos);
      int nextGridPos = xml.indexOf("<dataGrid", unitsGridPos + 1);
      String unitsActionsBlock = nextGridPos > 0
              ? xml.substring(unitsActionsPos, nextGridPos)
              : xml.substring(unitsActionsPos, unitsActionsPos + 500);

      assertTrue(unitsActionsBlock.contains("type=\"list_exclude\""));
      assertFalse(unitsActionsBlock.contains("type=\"list_remove\""));
    }
  }

  static boolean bootJarExists() {
    Path libs = Path.of("build", "libs");
    if (!Files.isDirectory(libs)) {
      return false;
    }
    try (var stream = Files.list(libs)) {
      return stream.anyMatch(p -> p.getFileName().toString().endsWith(".jar")
              && !p.getFileName().toString().endsWith("-plain.jar"));
    } catch (IOException e) {
      return false;
    }
  }

  private static String findBootJarName() throws IOException {
    try (var stream = Files.list(Path.of("build", "libs"))) {
      return stream
              .map(p -> p.getFileName().toString())
              .filter(name -> name.endsWith(".jar") && !name.endsWith("-plain.jar"))
              .findFirst()
              .orElseThrow(() -> new IOException("bootJar not found in build/libs"));
    }
  }

  private static String jarFile(Path jarPath) {
    return jarPath.toAbsolutePath().toString();
  }
}
