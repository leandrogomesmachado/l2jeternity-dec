package org.eclipse.jdt.internal.compiler.tool;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.JavaCompiler.CompilationTask;
import org.eclipse.jdt.internal.compiler.batch.Main;

public class EclipseCompiler implements JavaCompiler {
   private static Set<SourceVersion> SupportedSourceVersions;
   WeakHashMap<Thread, EclipseCompilerImpl> threadCache = new WeakHashMap<>();
   public DiagnosticListener<? super JavaFileObject> diagnosticListener;

   static {
      EnumSet<SourceVersion> enumSet = EnumSet.range(SourceVersion.RELEASE_0, SourceVersion.RELEASE_6);
      SupportedSourceVersions = Collections.unmodifiableSet(enumSet);
   }

   @Override
   public Set<SourceVersion> getSourceVersions() {
      return SupportedSourceVersions;
   }

   @Override
   public StandardJavaFileManager getStandardFileManager(DiagnosticListener<? super JavaFileObject> someDiagnosticListener, Locale locale, Charset charset) {
      this.diagnosticListener = someDiagnosticListener;
      return new EclipseFileManager(locale, charset);
   }

   @Override
   public CompilationTask getTask(
      Writer out,
      JavaFileManager fileManager,
      DiagnosticListener<? super JavaFileObject> someDiagnosticListener,
      Iterable<String> options,
      Iterable<String> classes,
      Iterable<? extends JavaFileObject> compilationUnits
   ) {
      PrintWriter writerOut = null;
      PrintWriter writerErr = null;
      if (out == null) {
         writerOut = new PrintWriter(System.err);
         writerErr = new PrintWriter(System.err);
      } else {
         writerOut = new PrintWriter(out);
         writerErr = new PrintWriter(out);
      }

      Thread currentThread = Thread.currentThread();
      EclipseCompilerImpl eclipseCompiler = this.threadCache.get(currentThread);
      if (eclipseCompiler == null) {
         eclipseCompiler = new EclipseCompilerImpl(writerOut, writerErr, false);
         this.threadCache.put(currentThread, eclipseCompiler);
      } else {
         eclipseCompiler.initialize(writerOut, writerErr, false, null, null);
      }

      final EclipseCompilerImpl eclipseCompiler2 = new EclipseCompilerImpl(writerOut, writerErr, false);
      eclipseCompiler2.compilationUnits = compilationUnits;
      eclipseCompiler2.diagnosticListener = someDiagnosticListener;
      if (fileManager != null) {
         eclipseCompiler2.fileManager = fileManager;
      } else {
         eclipseCompiler2.fileManager = this.getStandardFileManager(someDiagnosticListener, null, null);
      }

      eclipseCompiler2.options.put("org.eclipse.jdt.core.compiler.compliance", "1.6");
      eclipseCompiler2.options.put("org.eclipse.jdt.core.compiler.source", "1.6");
      eclipseCompiler2.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.6");
      ArrayList<String> allOptions = new ArrayList<>();
      if (options != null) {
         Iterator<String> iterator = options.iterator();

         while(iterator.hasNext()) {
            eclipseCompiler2.fileManager.handleOption(iterator.next(), iterator);
         }

         for(String option : options) {
            allOptions.add(option);
         }
      }

      if (compilationUnits != null) {
         for(JavaFileObject javaFileObject : compilationUnits) {
            URI uri = javaFileObject.toUri();
            if (!uri.isAbsolute()) {
               uri = URI.create("file://" + uri.toString());
            }

            if (uri.getScheme().equals("file")) {
               allOptions.add(new File(uri).getAbsolutePath());
            } else {
               allOptions.add(uri.toString());
            }
         }
      }

      if (classes != null) {
         allOptions.add("-classNames");
         StringBuilder builder = new StringBuilder();
         int i = 0;

         for(String className : classes) {
            if (i != 0) {
               builder.append(',');
            }

            builder.append(className);
            ++i;
         }

         allOptions.add(String.valueOf(builder));
      }

      String[] optionsToProcess = new String[allOptions.size()];
      allOptions.toArray(optionsToProcess);

      try {
         eclipseCompiler2.configure(optionsToProcess);
      } catch (IllegalArgumentException var17) {
         throw var17;
      }

      if (eclipseCompiler2.fileManager instanceof StandardJavaFileManager) {
         StandardJavaFileManager javaFileManager = (StandardJavaFileManager)eclipseCompiler2.fileManager;
         Iterable<? extends File> location = javaFileManager.getLocation(StandardLocation.CLASS_OUTPUT);
         if (location != null) {
            eclipseCompiler2.setDestinationPath(location.iterator().next().getAbsolutePath());
         }
      }

      return new CompilationTask() {
         private boolean hasRun = false;

         @Override
         public Boolean call() {
            if (this.hasRun) {
               throw new IllegalStateException("This task has already been run");
            } else {
               Boolean value = eclipseCompiler2.call() ? Boolean.TRUE : Boolean.FALSE;
               this.hasRun = true;
               return value;
            }
         }

         @Override
         public void setLocale(Locale locale) {
            eclipseCompiler2.setLocale(locale);
         }

         @Override
         public void setProcessors(Iterable<? extends Processor> processors) {
            ArrayList<Processor> temp = new ArrayList<>();

            for(Processor processor : processors) {
               temp.add(processor);
            }

            Processor[] processors2 = new Processor[temp.size()];
            temp.toArray(processors2);
            eclipseCompiler2.processors = processors2;
         }
      };
   }

   @Override
   public int isSupportedOption(String option) {
      return Options.processOptions(option);
   }

   @Override
   public int run(InputStream in, OutputStream out, OutputStream err, String... arguments) {
      boolean succeed = new Main(
            new PrintWriter(new OutputStreamWriter((OutputStream)(out != null ? out : System.out))),
            new PrintWriter(new OutputStreamWriter((OutputStream)(err != null ? err : System.err))),
            true,
            null,
            null
         )
         .compile(arguments);
      return succeed ? 0 : -1;
   }
}
