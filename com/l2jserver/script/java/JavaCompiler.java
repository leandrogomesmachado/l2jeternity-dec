package com.l2jserver.script.java;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.JavaCompiler.CompilationTask;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

public class JavaCompiler {
   private final javax.tools.JavaCompiler tool = new EclipseCompiler();

   public Map<String, byte[]> compile(String source, String fileName) {
      PrintWriter err = new PrintWriter(System.err);
      return this.compile(source, fileName, err, null, null);
   }

   public Map<String, byte[]> compile(String fileName, String source, Writer err) {
      return this.compile(fileName, source, err, null, null);
   }

   public Map<String, byte[]> compile(String fileName, String source, Writer err, String sourcePath) {
      return this.compile(fileName, source, err, sourcePath, null);
   }

   public Map<String, byte[]> compile(String fileName, String source, Writer err, String sourcePath, String classPath) {
      DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
      MemoryJavaFileManager manager = new MemoryJavaFileManager();
      List<JavaFileObject> compUnits = new ArrayList<>(1);
      compUnits.add(MemoryJavaFileManager.makeStringSource(fileName, source));
      List<String> options = new ArrayList<>();
      options.add("-warn:-enumSwitch");
      options.add("-g");
      options.add("-deprecation");
      options.add("-1.8");
      if (sourcePath != null) {
         options.add("-sourcepath");
         options.add(sourcePath);
      }

      if (classPath != null) {
         options.add("-classpath");
         options.add(classPath);
      }

      CompilationTask task = this.tool.getTask(err, manager, diagnostics, options, null, compUnits);
      if (task.call()) {
         Map<String, byte[]> classBytes = manager.getClassBytes();
         manager.close();
         return classBytes;
      } else {
         PrintWriter perr = new PrintWriter(err);

         for(Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
            perr.println(diagnostic.getMessage(Locale.getDefault()));
         }

         perr.flush();
         return null;
      }
   }
}
