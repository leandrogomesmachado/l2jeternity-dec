package org.eclipse.jdt.core.compiler.batch;

import java.io.PrintWriter;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.internal.compiler.batch.Main;

public final class BatchCompiler {
   public static boolean compile(String commandLine, PrintWriter outWriter, PrintWriter errWriter, CompilationProgress progress) {
      return compile(Main.tokenize(commandLine), outWriter, errWriter, progress);
   }

   public static boolean compile(String[] commandLineArguments, PrintWriter outWriter, PrintWriter errWriter, CompilationProgress progress) {
      return Main.compile(commandLineArguments, outWriter, errWriter, progress);
   }

   private BatchCompiler() {
   }
}
