package org.eclipse.jdt.internal.compiler.tool;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.JavaFileObject.Kind;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.batch.ClasspathJsr199;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.util.Messages;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

public class EclipseCompilerImpl extends Main {
   private static final CompilationUnit[] NO_UNITS = new CompilationUnit[0];
   private HashMap<CompilationUnit, JavaFileObject> javaFileObjectMap;
   Iterable<? extends JavaFileObject> compilationUnits;
   public JavaFileManager fileManager;
   protected Processor[] processors;
   public DiagnosticListener<? super JavaFileObject> diagnosticListener;

   public EclipseCompilerImpl(PrintWriter out, PrintWriter err, boolean systemExitWhenFinished) {
      super(out, err, systemExitWhenFinished, null, null);
   }

   public boolean call() {
      try {
         if (this.proceed) {
            this.globalProblemsCount = 0;
            this.globalErrorsCount = 0;
            this.globalWarningsCount = 0;
            this.globalTasksCount = 0;
            this.exportedClassFilesCounter = 0;
            this.performCompilation();
         }

         return this.globalErrorsCount == 0;
      } catch (IllegalArgumentException var6) {
         this.logger.logException(var6);
         if (this.systemExitWhenFinished) {
            this.cleanup();
            System.exit(-1);
         }
      } catch (RuntimeException var7) {
         this.logger.logException(var7);
         return false;
      } finally {
         this.cleanup();
      }

      return false;
   }

   private void cleanup() {
      this.logger.flush();
      this.logger.close();
      this.processors = null;

      try {
         if (this.fileManager != null) {
            this.fileManager.flush();
         }
      } catch (IOException var1) {
      }
   }

   @Override
   public CompilationUnit[] getCompilationUnits() {
      if (this.compilationUnits == null) {
         return NO_UNITS;
      } else {
         ArrayList<CompilationUnit> units = new ArrayList<>();

         for(final JavaFileObject javaFileObject : this.compilationUnits) {
            if (javaFileObject.getKind() != Kind.SOURCE) {
               throw new IllegalArgumentException();
            }

            String name = javaFileObject.getName();
            name = name.replace('\\', '/');
            CompilationUnit compilationUnit = new CompilationUnit(null, name, null) {
               @Override
               public char[] getContents() {
                  try {
                     return javaFileObject.getCharContent(true).toString().toCharArray();
                  } catch (IOException var2) {
                     var2.printStackTrace();
                     throw new AbortCompilationUnit(null, var2, null);
                  }
               }
            };
            units.add(compilationUnit);
            this.javaFileObjectMap.put(compilationUnit, javaFileObject);
         }

         CompilationUnit[] result = new CompilationUnit[units.size()];
         units.toArray(result);
         return result;
      }
   }

   @Override
   public IErrorHandlingPolicy getHandlingPolicy() {
      return new IErrorHandlingPolicy() {
         @Override
         public boolean proceedOnErrors() {
            return false;
         }

         @Override
         public boolean stopOnFirstError() {
            return false;
         }

         @Override
         public boolean ignoreAllErrors() {
            return false;
         }
      };
   }

   @Override
   public IProblemFactory getProblemFactory() {
      return new DefaultProblemFactory() {
         @Override
         public CategorizedProblem createProblem(
            final char[] originatingFileName,
            final int problemId,
            final String[] problemArguments,
            String[] messageArguments,
            final int severity,
            final int startPosition,
            final int endPosition,
            final int lineNumber,
            final int columnNumber
         ) {
            DiagnosticListener<? super JavaFileObject> diagListener = EclipseCompilerImpl.this.diagnosticListener;
            if (diagListener != null) {
               diagListener.report(new Diagnostic<JavaFileObject>() {
                  @Override
                  public String getCode() {
                     return Integer.toString(problemId);
                  }

                  @Override
                  public long getColumnNumber() {
                     return (long)columnNumber;
                  }

                  @Override
                  public long getEndPosition() {
                     return (long)endPosition;
                  }

                  @Override
                  public javax.tools.Diagnostic.Kind getKind() {
                     if ((severity & 1) != 0) {
                        return javax.tools.Diagnostic.Kind.ERROR;
                     } else if ((severity & 32) != 0) {
                        return javax.tools.Diagnostic.Kind.WARNING;
                     } else {
                        return false ? javax.tools.Diagnostic.Kind.MANDATORY_WARNING : javax.tools.Diagnostic.Kind.OTHER;
                     }
                  }

                  @Override
                  public long getLineNumber() {
                     return (long)lineNumber;
                  }

                  @Override
                  public String getMessage(Locale locale) {
                     if (locale != null) {
                        setLocale(locale);
                     }

                     return getLocalizedMessage(problemId, problemArguments);
                  }

                  @Override
                  public long getPosition() {
                     return (long)startPosition;
                  }

                  public JavaFileObject getSource() {
                     File f = new File(new String(originatingFileName));
                     return f.exists() ? new EclipseFileObject(null, f.toURI(), JavaFileObject.Kind.SOURCE, null) : null;
                  }

                  @Override
                  public long getStartPosition() {
                     return (long)startPosition;
                  }
               });
            }

            return super.createProblem(
               originatingFileName, problemId, problemArguments, messageArguments, severity, startPosition, endPosition, lineNumber, columnNumber
            );
         }

         @Override
         public CategorizedProblem createProblem(
            final char[] originatingFileName,
            final int problemId,
            final String[] problemArguments,
            int elaborationID,
            String[] messageArguments,
            final int severity,
            final int startPosition,
            final int endPosition,
            final int lineNumber,
            final int columnNumber
         ) {
            DiagnosticListener<? super JavaFileObject> diagListener = EclipseCompilerImpl.this.diagnosticListener;
            if (diagListener != null) {
               diagListener.report(new Diagnostic<JavaFileObject>() {
                  @Override
                  public String getCode() {
                     return Integer.toString(problemId);
                  }

                  @Override
                  public long getColumnNumber() {
                     return (long)columnNumber;
                  }

                  @Override
                  public long getEndPosition() {
                     return (long)endPosition;
                  }

                  @Override
                  public javax.tools.Diagnostic.Kind getKind() {
                     if ((severity & 1) != 0) {
                        return javax.tools.Diagnostic.Kind.ERROR;
                     } else if ((severity & 1024) != 0) {
                        return javax.tools.Diagnostic.Kind.NOTE;
                     } else if ((severity & 32) != 0) {
                        return javax.tools.Diagnostic.Kind.WARNING;
                     } else {
                        return false ? javax.tools.Diagnostic.Kind.MANDATORY_WARNING : javax.tools.Diagnostic.Kind.OTHER;
                     }
                  }

                  @Override
                  public long getLineNumber() {
                     return (long)lineNumber;
                  }

                  @Override
                  public String getMessage(Locale locale) {
                     if (locale != null) {
                        setLocale(locale);
                     }

                     return getLocalizedMessage(problemId, problemArguments);
                  }

                  @Override
                  public long getPosition() {
                     return (long)startPosition;
                  }

                  public JavaFileObject getSource() {
                     File f = new File(new String(originatingFileName));
                     return f.exists() ? new EclipseFileObject(null, f.toURI(), JavaFileObject.Kind.SOURCE, null) : null;
                  }

                  @Override
                  public long getStartPosition() {
                     return (long)startPosition;
                  }
               });
            }

            return super.createProblem(
               originatingFileName,
               problemId,
               problemArguments,
               elaborationID,
               messageArguments,
               severity,
               startPosition,
               endPosition,
               lineNumber,
               columnNumber
            );
         }
      };
   }

   @Override
   protected void initialize(
      PrintWriter outWriter, PrintWriter errWriter, boolean systemExit, Map<String, String> customDefaultOptions, CompilationProgress compilationProgress
   ) {
      super.initialize(outWriter, errWriter, systemExit, customDefaultOptions, compilationProgress);
      this.javaFileObjectMap = new HashMap<>();
   }

   @Override
   protected void initializeAnnotationProcessorManager() {
      super.initializeAnnotationProcessorManager();
      if (this.batchCompiler.annotationProcessorManager != null && this.processors != null) {
         this.batchCompiler.annotationProcessorManager.setProcessors(this.processors);
      } else if (this.processors != null) {
         throw new UnsupportedOperationException("Cannot handle annotation processing");
      }
   }

   // $VF: Could not inline inconsistent finally blocks
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   @Override
   public void outputClassFiles(CompilationResult unitResult) {
      if (unitResult != null && (!unitResult.hasErrors() || this.proceedOnError)) {
         ClassFile[] classFiles = unitResult.getClassFiles();
         boolean generateClasspathStructure = this.fileManager.hasLocation(StandardLocation.CLASS_OUTPUT);
         String currentDestinationPath = this.destinationPath;
         File outputLocation = null;
         if (currentDestinationPath != null) {
            outputLocation = new File(currentDestinationPath);
            outputLocation.mkdirs();
         }

         int i = 0;

         for(int fileCount = classFiles.length; i < fileCount; ++i) {
            ClassFile classFile = classFiles[i];
            char[] filename = classFile.fileName();
            int length = filename.length;
            char[] relativeName = new char[length + 6];
            System.arraycopy(filename, 0, relativeName, 0, length);
            System.arraycopy(SuffixConstants.SUFFIX_class, 0, relativeName, length, 6);
            CharOperation.replace(relativeName, '/', File.separatorChar);
            String relativeStringName = new String(relativeName);
            if (this.compilerOptions.verbose) {
               this.out
                  .println(Messages.bind(Messages.compilation_write, new String[]{String.valueOf(this.exportedClassFilesCounter + 1), relativeStringName}));
            }

            try {
               JavaFileObject javaFileForOutput = this.fileManager
                  .getJavaFileForOutput(
                     StandardLocation.CLASS_OUTPUT, new String(filename), Kind.CLASS, this.javaFileObjectMap.get(unitResult.compilationUnit)
                  );
               if (generateClasspathStructure) {
                  if (currentDestinationPath != null) {
                     int index = CharOperation.lastIndexOf(File.separatorChar, relativeName);
                     if (index != -1) {
                        File currentFolder = new File(currentDestinationPath, relativeStringName.substring(0, index));
                        currentFolder.mkdirs();
                     }
                  } else {
                     String path = javaFileForOutput.toUri().getPath();
                     int index = path.lastIndexOf(47);
                     if (index != -1) {
                        File file = new File(path.substring(0, index));
                        file.mkdirs();
                     }
                  }
               }

               Throwable var36 = null;
               Object var38 = null;

               try {
                  OutputStream openOutputStream = javaFileForOutput.openOutputStream();

                  try {
                     BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(openOutputStream);

                     try {
                        bufferedOutputStream.write(classFile.header, 0, classFile.headerOffset);
                        bufferedOutputStream.write(classFile.contents, 0, classFile.contentsOffset);
                        bufferedOutputStream.flush();
                     } finally {
                        if (bufferedOutputStream != null) {
                           bufferedOutputStream.close();
                        }
                     }
                  } catch (Throwable var31) {
                     if (var36 == null) {
                        var36 = var31;
                     } else if (var36 != var31) {
                        var36.addSuppressed(var31);
                     }

                     if (openOutputStream != null) {
                        openOutputStream.close();
                     }

                     throw var36;
                  }

                  if (openOutputStream != null) {
                     openOutputStream.close();
                  }
               } catch (Throwable var32) {
                  if (var36 == null) {
                     var36 = var32;
                  } else if (var36 != var32) {
                     var36.addSuppressed(var32);
                  }

                  throw var36;
               }
            } catch (IOException var33) {
               this.logger.logNoClassFileCreated(currentDestinationPath, relativeStringName, var33);
            }

            this.logger.logClassFile(generateClasspathStructure, currentDestinationPath, relativeStringName);
            ++this.exportedClassFilesCounter;
         }

         this.batchCompiler.lookupEnvironment.releaseClassFiles(classFiles);
      }
   }

   @Override
   protected void setPaths(
      ArrayList bootclasspaths,
      String sourcepathClasspathArg,
      ArrayList sourcepathClasspaths,
      ArrayList classpaths,
      ArrayList extdirsClasspaths,
      ArrayList endorsedDirClasspaths,
      String customEncoding
   ) {
      ArrayList<FileSystem.Classpath> fileSystemClasspaths = new ArrayList<>();
      EclipseFileManager eclipseJavaFileManager = null;
      StandardJavaFileManager standardJavaFileManager = null;
      JavaFileManager javaFileManager = null;
      boolean havePlatformPaths = false;
      boolean haveClassPaths = false;
      if (this.fileManager instanceof EclipseFileManager) {
         eclipseJavaFileManager = (EclipseFileManager)this.fileManager;
      }

      if (this.fileManager instanceof StandardJavaFileManager) {
         standardJavaFileManager = (StandardJavaFileManager)this.fileManager;
      }

      javaFileManager = this.fileManager;
      if (eclipseJavaFileManager != null && (eclipseJavaFileManager.flags & 4) == 0 && (eclipseJavaFileManager.flags & 2) != 0) {
         fileSystemClasspaths.addAll(this.handleEndorseddirs(null));
      }

      Iterable<? extends File> location = null;
      if (standardJavaFileManager != null) {
         location = standardJavaFileManager.getLocation(StandardLocation.PLATFORM_CLASS_PATH);
         if (location != null) {
            for(File file : location) {
               FileSystem.Classpath classpath = FileSystem.getClasspath(file.getAbsolutePath(), null, null, this.options);
               if (classpath != null) {
                  fileSystemClasspaths.add(classpath);
                  havePlatformPaths = true;
               }
            }
         }
      } else if (javaFileManager != null) {
         FileSystem.Classpath classpath = new ClasspathJsr199(this.fileManager, StandardLocation.PLATFORM_CLASS_PATH);
         fileSystemClasspaths.add(classpath);
         havePlatformPaths = true;
      }

      if (eclipseJavaFileManager != null && (eclipseJavaFileManager.flags & 1) == 0 && (eclipseJavaFileManager.flags & 2) != 0) {
         fileSystemClasspaths.addAll(this.handleExtdirs(null));
      }

      if (standardJavaFileManager != null) {
         location = standardJavaFileManager.getLocation(StandardLocation.SOURCE_PATH);
         if (location != null) {
            for(File file : location) {
               FileSystem.Classpath classpath = FileSystem.getClasspath(file.getAbsolutePath(), null, null, this.options);
               if (classpath != null) {
                  fileSystemClasspaths.add(classpath);
               }
            }
         }

         location = standardJavaFileManager.getLocation(StandardLocation.CLASS_PATH);
         if (location != null) {
            for(File file : location) {
               FileSystem.Classpath classpath = FileSystem.getClasspath(file.getAbsolutePath(), null, null, this.options);
               if (classpath != null) {
                  fileSystemClasspaths.add(classpath);
                  haveClassPaths = true;
               }
            }
         }
      } else if (javaFileManager != null) {
         FileSystem.Classpath classpath = null;
         if (this.fileManager.hasLocation(StandardLocation.SOURCE_PATH)) {
            FileSystem.Classpath var28 = new ClasspathJsr199(this.fileManager, StandardLocation.SOURCE_PATH);
            fileSystemClasspaths.add(var28);
         }

         FileSystem.Classpath var29 = new ClasspathJsr199(this.fileManager, StandardLocation.CLASS_PATH);
         fileSystemClasspaths.add(var29);
         haveClassPaths = true;
      }

      if (this.checkedClasspaths == null) {
         if (!havePlatformPaths) {
            fileSystemClasspaths.addAll(this.handleBootclasspath(null, null));
         }

         if (!haveClassPaths) {
            fileSystemClasspaths.addAll(this.handleClasspath(null, null));
         }
      }

      fileSystemClasspaths = FileSystem.ClasspathNormalizer.normalize(fileSystemClasspaths);
      int size = fileSystemClasspaths.size();
      if (size != 0) {
         this.checkedClasspaths = new FileSystem.Classpath[size];
         int i = 0;

         for(FileSystem.Classpath classpath : fileSystemClasspaths) {
            this.checkedClasspaths[i++] = classpath;
         }
      }
   }

   @Override
   protected void loggingExtraProblems() {
      super.loggingExtraProblems();

      for(final CategorizedProblem problem : this.extraProblems) {
         if (this.diagnosticListener != null) {
            this.diagnosticListener
               .report(
                  new Diagnostic<JavaFileObject>() {
                     @Override
                     public String getCode() {
                        return null;
                     }
      
                     @Override
                     public long getColumnNumber() {
                        return problem instanceof DefaultProblem ? (long)((DefaultProblem)problem).column : -1L;
                     }
      
                     @Override
                     public long getEndPosition() {
                        return problem instanceof DefaultProblem ? (long)((DefaultProblem)problem).getSourceEnd() : -1L;
                     }
      
                     @Override
                     public javax.tools.Diagnostic.Kind getKind() {
                        if (problem.isError()) {
                           return javax.tools.Diagnostic.Kind.ERROR;
                        } else if (problem.isWarning()) {
                           return javax.tools.Diagnostic.Kind.WARNING;
                        } else {
                           return problem instanceof DefaultProblem && ((DefaultProblem)problem).isInfo()
                              ? javax.tools.Diagnostic.Kind.NOTE
                              : javax.tools.Diagnostic.Kind.OTHER;
                        }
                     }
      
                     @Override
                     public long getLineNumber() {
                        return problem instanceof DefaultProblem ? (long)((DefaultProblem)problem).getSourceLineNumber() : -1L;
                     }
      
                     @Override
                     public String getMessage(Locale locale) {
                        return problem.getMessage();
                     }
      
                     @Override
                     public long getPosition() {
                        return problem instanceof DefaultProblem ? (long)((DefaultProblem)problem).getSourceStart() : -1L;
                     }
      
                     public JavaFileObject getSource() {
                        if (problem instanceof DefaultProblem) {
                           File f = new File(new String(((DefaultProblem)problem).getOriginatingFileName()));
                           if (f.exists()) {
                              Charset charset = EclipseCompilerImpl.this.fileManager instanceof EclipseFileManager
                                 ? ((EclipseFileManager)EclipseCompilerImpl.this.fileManager).charset
                                 : Charset.defaultCharset();
                              return new EclipseFileObject(null, f.toURI(), JavaFileObject.Kind.SOURCE, charset);
                           } else {
                              return null;
                           }
                        } else {
                           return null;
                        }
                     }
      
                     @Override
                     public long getStartPosition() {
                        return this.getPosition();
                     }
                  }
               );
         }
      }
   }
}
