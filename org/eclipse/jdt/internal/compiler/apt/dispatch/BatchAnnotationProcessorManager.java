package org.eclipse.jdt.internal.compiler.apt.dispatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import javax.annotation.processing.Processor;
import javax.tools.StandardLocation;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

public class BatchAnnotationProcessorManager extends BaseAnnotationProcessorManager {
   private List<Processor> _setProcessors = null;
   private Iterator<Processor> _setProcessorIter = null;
   private List<String> _commandLineProcessors;
   private Iterator<String> _commandLineProcessorIter = null;
   private ServiceLoader<Processor> _serviceLoader = null;
   private Iterator<Processor> _serviceLoaderIter;
   private ClassLoader _procLoader;
   private static final boolean VERBOSE_PROCESSOR_DISCOVERY = true;
   private boolean _printProcessorDiscovery = false;

   @Override
   public void configure(Object batchCompiler, String[] commandLineArguments) {
      if (this._processingEnv != null) {
         throw new IllegalStateException("Calling configure() more than once on an AnnotationProcessorManager is not supported");
      } else {
         BatchProcessingEnvImpl processingEnv = new BatchProcessingEnvImpl(this, (Main)batchCompiler, commandLineArguments);
         this._processingEnv = processingEnv;
         this._procLoader = processingEnv.getFileManager().getClassLoader(StandardLocation.ANNOTATION_PROCESSOR_PATH);
         this.parseCommandLine(commandLineArguments);
         this._round = 0;
      }
   }

   private void parseCommandLine(String[] commandLineArguments) {
      List<String> commandLineProcessors = null;

      for(int i = 0; i < commandLineArguments.length; ++i) {
         String option = commandLineArguments[i];
         if ("-XprintProcessorInfo".equals(option)) {
            this._printProcessorInfo = true;
            this._printProcessorDiscovery = true;
         } else if ("-XprintRounds".equals(option)) {
            this._printRounds = true;
         } else if ("-processor".equals(option)) {
            commandLineProcessors = new ArrayList<>();
            String procs = commandLineArguments[++i];

            String[] var9;
            for(String proc : var9 = procs.split(",")) {
               commandLineProcessors.add(proc);
            }
            break;
         }
      }

      this._commandLineProcessors = commandLineProcessors;
      if (this._commandLineProcessors != null) {
         this._commandLineProcessorIter = this._commandLineProcessors.iterator();
      }
   }

   @Override
   public ProcessorInfo discoverNextProcessor() {
      if (this._setProcessors != null) {
         if (this._setProcessorIter.hasNext()) {
            Processor p = this._setProcessorIter.next();
            p.init(this._processingEnv);
            ProcessorInfo pi = new ProcessorInfo(p);
            this._processors.add(pi);
            if (this._printProcessorDiscovery && this._out != null) {
               this._out.println("API specified processor: " + pi);
            }

            return pi;
         } else {
            return null;
         }
      } else if (this._commandLineProcessors != null) {
         if (this._commandLineProcessorIter.hasNext()) {
            String proc = this._commandLineProcessorIter.next();

            try {
               Class<?> clazz = this._procLoader.loadClass(proc);
               Object o = clazz.newInstance();
               Processor p = (Processor)o;
               p.init(this._processingEnv);
               ProcessorInfo pi = new ProcessorInfo(p);
               this._processors.add(pi);
               if (this._printProcessorDiscovery && this._out != null) {
                  this._out.println("Command line specified processor: " + pi);
               }

               return pi;
            } catch (Exception var6) {
               throw new AbortCompilation(null, var6);
            }
         } else {
            return null;
         }
      } else {
         if (this._serviceLoader == null) {
            this._serviceLoader = ServiceLoader.load(Processor.class, this._procLoader);
            this._serviceLoaderIter = this._serviceLoader.iterator();
         }

         try {
            if (this._serviceLoaderIter.hasNext()) {
               Processor p = this._serviceLoaderIter.next();
               p.init(this._processingEnv);
               ProcessorInfo pi = new ProcessorInfo(p);
               this._processors.add(pi);
               if (this._printProcessorDiscovery && this._out != null) {
                  StringBuilder sb = new StringBuilder();
                  sb.append("Discovered processor service ");
                  sb.append(pi);
                  sb.append("\n  supporting ");
                  sb.append(pi.getSupportedAnnotationTypesAsString());
                  sb.append("\n  in ");
                  sb.append(this.getProcessorLocation(p));
                  this._out.println(sb.toString());
               }

               return pi;
            } else {
               return null;
            }
         } catch (ServiceConfigurationError var7) {
            throw new AbortCompilation(null, var7);
         }
      }
   }

   private String getProcessorLocation(Processor p) {
      boolean isMember = false;
      Class<?> outerClass = p.getClass();

      StringBuilder innerName;
      for(innerName = new StringBuilder(); outerClass.isMemberClass(); outerClass = outerClass.getEnclosingClass()) {
         innerName.insert(0, outerClass.getSimpleName());
         innerName.insert(0, '$');
         isMember = true;
      }

      String path = outerClass.getName();
      path = path.replace('.', '/');
      if (isMember) {
         path = path + innerName;
      }

      path = path + ".class";
      String location = this._procLoader.getResource(path).toString();
      if (location.endsWith(path)) {
         location = location.substring(0, location.length() - path.length());
      }

      return location;
   }

   @Override
   public void reportProcessorException(Processor p, Exception e) {
      throw new AbortCompilation(null, e);
   }

   @Override
   public void setProcessors(Object[] processors) {
      if (!this._isFirstRound) {
         throw new IllegalStateException("setProcessors() cannot be called after processing has begun");
      } else {
         this._setProcessors = new ArrayList<>(processors.length);

         for(Object o : processors) {
            Processor p = (Processor)o;
            this._setProcessors.add(p);
         }

         this._setProcessorIter = this._setProcessors.iterator();
         this._commandLineProcessors = null;
         this._commandLineProcessorIter = null;
      }
   }
}
